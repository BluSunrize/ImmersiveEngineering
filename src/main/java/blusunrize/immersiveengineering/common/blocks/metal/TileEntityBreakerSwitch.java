/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Optional;
import java.util.Set;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityBreakerSwitch extends TileEntityImmersiveConnectable implements IBlockBounds, IAdvancedDirectionalTile, IActiveState, IHammerInteraction, IPlayerInteraction, IRedstoneOutput, IOBJModelCallback<IBlockState>
{
	public int rotation = 0;
	public EnumFacing facing = EnumFacing.NORTH;
	public int wires = 0;
	public boolean active = false;
	public boolean inverted = false;
	public BlockPos endOfLeftConnection = null;

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	protected boolean canTakeMV()
	{
		return true;
	}

	@Override
	protected boolean canTakeHV()
	{
		return false;
	}

	@Override
	public boolean allowEnergyToPass(Connection con)
	{
		return active;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
	{
		if(!cableType.isEnergyWire()&&!REDSTONE_CATEGORY.equals(cableType.getCategory()))
			return false;
		if(HV_CATEGORY.equals(cableType.getCategory())&&!canTakeHV())
			return false;
		if(wires >= 2)
			return false;
		return limitType==null||WireApi.canMix(cableType, limitType);
	}

	@Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other)
	{
		if(this.limitType==null)
			this.limitType = cableType;
		wires++;
		onConnectionChange();
	}

	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		return limitType;
	}

	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.cableType: null;
		if(type==null)
			wires = 0;
		else
			wires--;
		if(wires <= 0)
			limitType = null;
		onConnectionChange();
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setInteger("rotation", rotation);
		nbt.setInteger("wires", wires);
		nbt.setBoolean("active", active);
		nbt.setBoolean("inverted", inverted);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		rotation = nbt.getInteger("rotation");
		wires = nbt.getInteger("wires");
		active = nbt.getBoolean("active");
		inverted = nbt.getBoolean("inverted");
		onConnectionChange();
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		Matrix4 mat = new Matrix4(facing);
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		if(endOfLeftConnection==null)
			calculateLeftConn(mat);
		boolean isLeft = con.end.equals(endOfLeftConnection)||con.start.equals(endOfLeftConnection);
		Vec3d ret = mat.apply(new Vec3d(isLeft?.25: .75, .5, .125));
		return ret;
	}

	protected void calculateLeftConn(Matrix4 transform)
	{
		Vec3d leftVec = transform.apply(new Vec3d(-1, .5, .5)).subtract(0, .5, .5);
		EnumFacing dir = EnumFacing.getFacingFromVector((float)leftVec.x, (float)leftVec.y, (float)leftVec.z);
		int maxDiff = Integer.MIN_VALUE;
		Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
		if(conns!=null)
			for(Connection c : conns)
			{
				Vec3i diff = pos.equals(c.start)?c.end.subtract(pos): c.start.subtract(pos);
				int val = 0;
				switch(dir.getAxis())
				{
					case X:
						val = diff.getX();
						break;
					case Y:
						val = diff.getY();
						break;
					case Z:
						val = diff.getZ();
				}
				val *= dir.getAxisDirection().getOffset();
				if(val > maxDiff)
				{
					maxDiff = val;
					endOfLeftConnection = pos==c.end?c.start: c.end;
				}
			}
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			inverted = !inverted;
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"rsSignal."+(inverted?"invertedOn": "invertedOff")));
			if(wires > 1)
				ImmersiveNetHandler.INSTANCE.resetCachedIndirectConnections(world, pos);
			notifyNeighbours();
		}
		else
		{
			rotation = (rotation+3)%4;
			onConnectionChange();
		}
		return true;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!Utils.isHammer(heldItem))
		{
			active = !active;
			world.playSound(null, getPos(), IESounds.direSwitch, SoundCategory.BLOCKS, 2.5F, 1);
			if(wires > 1)
				ImmersiveNetHandler.INSTANCE.resetCachedIndirectConnections(world, pos);
			world.addBlockEvent(getPos(), getBlockType(), active?1: 0, 0);
			notifyNeighbours();
		}
		return true;
	}

	public void notifyNeighbours()
	{
		markDirty();
		world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
		for(EnumFacing f : EnumFacing.VALUES)
			world.notifyNeighborsOfStateChange(getPos().offset(f), getBlockType(), true);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(super.receiveClientEvent(id, arg))
			return true;
		this.active = id==1;
		this.markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return IEProperties.BOOLEANS[0];
	}

	@Override
	public boolean getIsActive()
	{
		return active;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		Vec3d start = new Vec3d(.25, .1875, 0);
		Vec3d end = new Vec3d(.75, .8125, .5);
		Matrix4 mat = new Matrix4(facing);
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		start = mat.apply(start);
		end = mat.apply(end);
		return new float[]{(float)start.x, (float)start.y, (float)start.z,
				(float)end.x, (float)end.y, (float)end.z};
	}

	@Override
	public int getWeakRSOutput(IBlockState state, EnumFacing side)
	{
		return (active^inverted)?15: 0;
	}

	@Override
	public int getStrongRSOutput(IBlockState state, EnumFacing side)
	{
		return side.getOpposite()==facing&&(active^inverted)?15: 0;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, EnumFacing side)
	{
		return true;
	}

	@Override
	public Optional<TRSRTransformation> applyTransformations(IBlockState object, String group, Optional<TRSRTransformation> transform)
	{
		Matrix4 mat = transform.isPresent()?new Matrix4(transform.get().getMatrix()): new Matrix4();
		mat = mat.translate(.5, 0, .5).rotate(Math.PI/2*rotation, 0, 1, 0).translate(-.5, 0, -.5);
		transform = Optional.of(new TRSRTransformation(mat.toMatrix4f()));
		return transform;
	}

	@Override
	public String getCacheKey(IBlockState object)
	{
		return rotation+","+facing.getIndex()+","+active;
	}

	@Override
	public void onDirectionalPlacement(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer)
	{
		EnumFacing f = EnumFacing.SOUTH;
		if(side.getAxis()==Axis.Y)
		{
			float xFromMid = hitX-.5f;
			float zFromMid = hitZ-.5f;
			float max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
			if(max==Math.abs(xFromMid))
				f = xFromMid < 0?EnumFacing.WEST: EnumFacing.EAST;
			else
				f = zFromMid < 0?EnumFacing.NORTH: EnumFacing.SOUTH;
			if((side==EnumFacing.UP&&f.getAxis()==Axis.Z)||side==EnumFacing.DOWN)
				f = f.getOpposite();
		}
		rotation = f.getHorizontalIndex();
	}

	protected void onConnectionChange()
	{
		if(world!=null&&world.isRemote)
		{
			endOfLeftConnection = null;
			ImmersiveEngineering.proxy.clearConnectionModelCache();
			// reset cached connection vertices
			Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
			if(conns!=null)
				for(Connection c : conns)
				{
					c.catenaryVertices = null;
					world.markBlockRangeForRenderUpdate(c.end, c.end);
					Set<Connection> connsThere = ImmersiveNetHandler.INSTANCE.getConnections(world, c.end);
					if(connsThere!=null)
						for(Connection c2 : connsThere)
							if(c2.end.equals(pos))
								c2.catenaryVertices = null;
				}
		}
		if(world!=null)
			markContainingBlockForUpdate(null);
	}

	@Override
	public boolean moveConnectionTo(Connection c, BlockPos newEnd)
	{
		if(c.end.equals(endOfLeftConnection))
			endOfLeftConnection = newEnd;
		return true;
	}
}