/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.MV_CATEGORY;

public class TileEntityTransformer extends TileEntityImmersiveConnectable implements IDirectionalTile, IMirrorAble, IHasDummyBlocks, IAdvancedSelectionBounds, IDualState
{
	private static final int RIGHT_INDEX = 0;
	private static final int LEFT_INDEX = 1;
	private WireType leftType;
	private WireType rightType;
	public EnumFacing facing = EnumFacing.NORTH;
	public int dummy = 0;
	public boolean onPost = false;
	protected Set<String> acceptableLowerWires = ImmutableSet.of(WireType.LV_CATEGORY);

	public static boolean _Immovable()
	{
		return true;
	}

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
	public boolean canConnect()
	{
		return true;
	}

	//	@Override
	//	public boolean canUpdate()
	//	{
	//		return false;
	//	}
	@Override
	public boolean isEnergyOutput()
	{
		return false;
	}

	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		return 0;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
		if(leftType!=null)
			nbt.setString("leftType", leftType.getUniqueName());
		if(rightType!=null)
			nbt.setString("rightType", rightType.getUniqueName());
		nbt.setInteger("dummy", dummy);
		nbt.setBoolean("postAttached", onPost);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		if(nbt.hasKey("leftType"))
			leftType = ApiUtils.getWireTypeFromNBT(nbt, "leftType");
		else
			leftType = null;
		if(nbt.hasKey("rightType"))
			rightType = ApiUtils.getWireTypeFromNBT(nbt, "rightType");
		else
			rightType = null;
		dummy = nbt.getInteger("dummy");
		onPost = nbt.getBoolean("postAttached");
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return getPos().add(0, -dummy, 0);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(dummy!=0)
		{
			TileEntity master = world.getTileEntity(getPos().add(0, -dummy, 0));
			return master instanceof TileEntityTransformer&&((TileEntityTransformer)master).canConnectCable(cableType, target, offset);
		}
		switch(target.getIndex())
		{
			case LEFT_INDEX:
				return canAttach(cableType, leftType, rightType);
			case RIGHT_INDEX:
				return canAttach(cableType, rightType, leftType);
		}
		return false;
	}

	private boolean canAttach(WireType toAttach, @Nullable WireType atConn, @Nullable WireType other)
	{
		if(atConn!=null)
			return false;
		String higherCat = getHigherWiretype();
		String attachCat = toAttach.getCategory();
		if(other==null)
			return higherCat.equals(attachCat)||acceptableLowerWires.contains(attachCat);
		boolean isHigher = higherCat.equals(toAttach.getCategory());
		boolean isOtherHigher = higherCat.equals(other.getCategory());
		if(isHigher^isOtherHigher)
		{
			if(isHigher)
				return true;
			else
				return acceptableLowerWires.contains(attachCat);
		}
		else
			return false;
	}

	boolean isConnectedToRight(Connection c)
	{
		return c.getEndFor(pos).getIndex()==RIGHT_INDEX;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		if(dummy!=0)
		{
			TileEntity master = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(master instanceof TileEntityTransformer)
				((TileEntityTransformer)master).connectCable(cableType, target, other, otherTarget);
			return;
		}
		switch(target.getIndex())
		{
			case LEFT_INDEX:
				this.leftType = cableType;
				break;
			case RIGHT_INDEX:
				this.rightType = cableType;
				break;
		}
	}

	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.type: null;
		if(type==null)
			leftType = rightType = null;
		else
		{
			switch(connection.getEndFor(pos).getIndex())
			{
				case LEFT_INDEX:
					leftType = null;
					break;
				case RIGHT_INDEX:
					rightType = null;
					break;
			}
		}
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return getConnectionOffset(con, con.getEndFor(pos).getIndex()==RIGHT_INDEX);
	}

	private Vec3d getConnectionOffset(Connection con, boolean right)
	{
		if(onPost)
		{
			if(right)
				return new Vec3d(.5+(facing==EnumFacing.EAST?.4375: facing==EnumFacing.WEST?-.4375: 0), 1.4375, .5+(facing==EnumFacing.SOUTH?.4375: facing==EnumFacing.NORTH?-.4375: 0));
			else
				return new Vec3d(.5+(facing==EnumFacing.EAST?-.0625: facing==EnumFacing.WEST?.0625: 0), .25, .5+(facing==EnumFacing.SOUTH?-.0625: facing==EnumFacing.NORTH?.0625: 0));
		}
		else
		{
			double conRadius = con.type.getRenderDiameter()/2;
			double offset = getHigherWiretype().equals(con.type.getCategory())?getHigherOffset(): getLowerOffset();
			if(facing==EnumFacing.NORTH)
				return new Vec3d(right?.8125: .1875, 2+offset-conRadius, .5);
			if(facing==EnumFacing.SOUTH)
				return new Vec3d(right?.1875: .8125, 2+offset-conRadius, .5);
			if(facing==EnumFacing.WEST)
				return new Vec3d(.5, 2+offset-conRadius, right?.1875: .8125);
			if(facing==EnumFacing.EAST)
				return new Vec3d(.5, 2+offset-conRadius, right?.8125: .1875);
		}
		return new Vec3d(.5, .5, .5);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo target, Vec3i offset)
	{
		if(onPost)
		{
			if(target.hitY >= .5)
				return new ConnectionPoint(pos, RIGHT_INDEX);
			else
				return new ConnectionPoint(pos, LEFT_INDEX);
		}
		else
		{
			if(offset.getY()!=2)
				return null;
			if(facing==EnumFacing.NORTH)
				if(target.hitX < .5)
					return new ConnectionPoint(pos, LEFT_INDEX);
				else
					return new ConnectionPoint(pos, RIGHT_INDEX);
			else if(facing==EnumFacing.SOUTH)
				if(target.hitX < .5)
					return new ConnectionPoint(pos, RIGHT_INDEX);
				else
					return new ConnectionPoint(pos, LEFT_INDEX);
			else if(facing==EnumFacing.WEST)
				if(target.hitZ < .5)
					return new ConnectionPoint(pos, RIGHT_INDEX);
				else
					return new ConnectionPoint(pos, LEFT_INDEX);
			else if(facing==EnumFacing.EAST)
				if(target.hitZ < .5)
					return new ConnectionPoint(pos, LEFT_INDEX);
				else
					return new ConnectionPoint(pos, RIGHT_INDEX);
		}
		return null;
	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return inf==IMirrorAble.class?IEProperties.BOOLEANS[0]: inf==IDualState.class?IEProperties.BOOLEANS[1]: null;
	}

	@Override
	public boolean getIsMirrored()
	{
		if(onPost)
			return false;
		if(dummy!=0)
		{
			TileEntity master = world.getTileEntity(pos.down(dummy));
			return master instanceof TileEntityTransformer&&((TileEntityTransformer)master).getIsMirrored();
		}
		else
		{
			if(rightType==null&&leftType==null)
				return false;
			String higher = getHigherWiretype();
			return (rightType!=null&&higher.equals(rightType.getCategory()))||
					(leftType!=null&&!higher.equals(leftType.getCategory()));
		}
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
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
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
	public boolean isDummy()
	{
		return dummy!=0;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(state.getValue(IEProperties.BOOLEANS[1]))
		{
			onPost = true;
			facing = side.getOpposite();
			markDirty();
			this.markContainingBlockForUpdate(null);
		}
		else
			for(int i = 1; i <= 2; i++)
			{
				world.setBlockState(pos.add(0, i, 0), state);
				((TileEntityTransformer)world.getTileEntity(pos.add(0, i, 0))).dummy = i;
				((TileEntityTransformer)world.getTileEntity(pos.add(0, i, 0))).facing = this.facing;
			}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		if(onPost)
			return;
		for(int i = 0; i <= 2; i++)
			world.setBlockToAir(getPos().add(0, -dummy, 0).add(0, i, 0));
	}

	@Override
	public float[] getBlockBounds()
	{
		if(dummy==2)
			return new float[]{facing.getAxis()==Axis.Z?0: .3125f, 0, facing.getAxis()==Axis.X?0: .3125f, facing.getAxis()==Axis.Z?1: .6875f, this instanceof TileEntityTransformerHV?.75f: .5625f, facing.getAxis()==Axis.X?1: .6875f};
		if(onPost)
			return new float[]{facing.getAxis()==Axis.Z?.25F: facing==EnumFacing.WEST?-.375F: .6875F,
					0,
					facing.getAxis()==Axis.X?.25F: facing==EnumFacing.NORTH?-.375F: .6875F,
					facing.getAxis()==Axis.Z?.75F: facing==EnumFacing.EAST?1.375F: .3125F,
					1,
					facing.getAxis()==Axis.X?.75F: facing==EnumFacing.SOUTH?1.375F: .3125F};
		return null;
	}

	boolean cachedMirrored = false;
	private List<AxisAlignedBB> advSelectionBoxes = null;

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		boolean mirrored = getIsMirrored();
		if(dummy==2&&(advSelectionBoxes==null||cachedMirrored!=mirrored))
		{
			double offsetA = mirrored?getHigherOffset(): getLowerOffset();
			double offsetB = mirrored?getLowerOffset(): getHigherOffset();
			if(facing==EnumFacing.NORTH)
				advSelectionBoxes = Lists.newArrayList(new AxisAlignedBB(0, 0, .3125, .375, offsetB, .6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()), new AxisAlignedBB(.625, 0, .3125, 1, offsetA, .6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(facing==EnumFacing.SOUTH)
				advSelectionBoxes = Lists.newArrayList(new AxisAlignedBB(0, 0, .3125, .375, offsetA, .6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()), new AxisAlignedBB(.625, 0, .3125, 1, offsetB, .6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(facing==EnumFacing.WEST)
				advSelectionBoxes = Lists.newArrayList(new AxisAlignedBB(.3125, 0, 0, .6875, offsetA, .375).offset(getPos().getX(), getPos().getY(), getPos().getZ()), new AxisAlignedBB(.3125, 0, .625, .6875, offsetB, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(facing==EnumFacing.EAST)
				advSelectionBoxes = Lists.newArrayList(new AxisAlignedBB(.3125, 0, 0, .6875, offsetB, .375).offset(getPos().getX(), getPos().getY(), getPos().getZ()), new AxisAlignedBB(.3125, 0, .625, .6875, offsetA, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			cachedMirrored = mirrored;
		}
		else if(dummy!=2)
		{
			advSelectionBoxes = null;
		}
		return advSelectionBoxes;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return box.grow(.002).contains(mop.hitVec);
	}

	@Override
	public Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		if(onPost)
			return super.getIgnored(other);
		return ImmutableSet.of(pos.up(2));
	}

	@Override
	public boolean getIsSecondState()
	{
		return onPost;
	}

	protected float getLowerOffset()
	{
		return .5F;
	}

	protected float getHigherOffset()
	{
		return .5625F;
	}

	public String getHigherWiretype()
	{
		return MV_CATEGORY;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		if(isDummy())
			return ImmutableList.of();
		else
			return ImmutableList.of(new ConnectionPoint(pos, RIGHT_INDEX), new ConnectionPoint(pos, LEFT_INDEX));
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		if(isDummy())
			return ImmutableList.of();
		else
			return ImmutableList.of(new Connection(pos, LEFT_INDEX, RIGHT_INDEX));
	}
}