/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;

//TODO ConnectionPoints for opening/closing
public class TileEntityBreakerSwitch extends TileEntityImmersiveConnectable implements IBlockBounds, IAdvancedDirectionalTile, IActiveState, IHammerInteraction, IPlayerInteraction, IRedstoneOutput, IOBJModelCallback<IBlockState>
{
	public static final int LEFT_INDEX = 0;
	public static final int RIGHT_INDEX = 1;
	public int rotation = 0;
	public EnumFacing facing = EnumFacing.NORTH;
	public int wires = 0;
	public boolean active = false;
	public boolean inverted = false;

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

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		Matrix4 mat = new Matrix4(facing);
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		//TODO what is Matrix(facing)^-1?
		mat.invert();
		Vec3d transformedHit = mat.apply(new Vec3d(info.hitX, info.hitY, info.hitZ));
		IELogger.logger.info("Transformed hit: {}, original: {}", transformedHit,
				new Vec3d(info.hitX, info.hitY, info.hitZ));
		return new ConnectionPoint(pos, transformedHit.x > 0.5?RIGHT_INDEX: LEFT_INDEX);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(HV_CATEGORY.equals(cableType.getCategory())&&!canTakeHV())
			return false;
		//TODO
		return true;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		wires++;
	}

	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.type: null;
		if(type==null)
			wires = 0;
		else
			wires--;
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
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Matrix4 mat = new Matrix4(facing);
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		boolean isLeft = here.getIndex()==LEFT_INDEX;
		return mat.apply(new Vec3d(isLeft?.25: .75, .5, .125));
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			inverted = !inverted;
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"rsSignal."+(inverted?"invertedOn": "invertedOff")));
			notifyNeighbours();
			updateConductivity();
		}
		else
			rotation = (rotation+3)%4;
		return true;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!Utils.isHammer(heldItem))
		{
			active = !active;
			world.playSound(null, getPos(), IESounds.direSwitch, SoundCategory.BLOCKS, 2.5F, 1);
			world.addBlockEvent(getPos(), getBlockType(), active?1: 0, 0);
			notifyNeighbours();
			updateConductivity();
		}
		return true;
	}

	protected void updateConductivity()
	{
		if(allowEnergyToPass())
			globalNet.addConnection(new Connection(pos, LEFT_INDEX, RIGHT_INDEX));
		else
			globalNet.removeConnection(new Connection(pos, LEFT_INDEX, RIGHT_INDEX));
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
		Matrix4 mat = transform.map(trsrTransformation -> new Matrix4(trsrTransformation.getMatrix())).orElseGet(Matrix4::new);
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

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, LEFT_INDEX), new ConnectionPoint(pos, RIGHT_INDEX));
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		if(allowEnergyToPass())
			return ImmutableList.of(new Connection(pos, LEFT_INDEX, RIGHT_INDEX));
		else
			return ImmutableList.of();
	}

	protected boolean allowEnergyToPass()
	{
		return active;
	}
}