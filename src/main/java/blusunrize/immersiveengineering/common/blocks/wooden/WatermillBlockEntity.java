/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.antlr.v4.runtime.misc.Triple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class WatermillBlockEntity extends IEBaseBlockEntity implements IETickableBlockEntity, IStateBasedDirectional, IHasDummyBlocks
{
	public int[] offset = {0, 0};
	public float rotation = 0;
	private Vec3 rotationVec = null;
	// Indicates that the next tick should be skipped since the waterwheel is being controlled by another waterwheel
	// attached to it
	public byte linkElementNumber = 0;
	private static final byte maxLinked = 3;
	private boolean beingBroken = false;
	public double perTick;
	private static final float speedFactor = 1f/1440;

	public WatermillBlockEntity(BlockEntityType<WatermillBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void tickClient()
	{
		rotation += perTick;
		rotation %= 1;
	}

	@Override
	public void tickServer()
	{
		if(isBlocked())//TODO throttle?
		{
			setPerTickAndAdvance(0);
			if(linkElementNumber > 0)
				dissolveLink();
			return;
		}
		if(level.getGameTime()%64==((getBlockPos().getX()^getBlockPos().getZ())&63))
			rotationVec = null;
		if(linkElementNumber > 1)
		{
			return;
		}

		BlockEntity acc = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(getFacing().getOpposite()));
		boolean hasRotAcceptor = false;
		Triple<Boolean, List<WatermillBlockEntity>, Double> linkedWheels = null;
		if(acc instanceof IRotationAcceptor rotAcc&&rotAcc.sideAcceptsRotation(getFacing().getOpposite()))
		{
			linkedWheels = searchAndSetLink((byte)1, getFacing()); //linkedWheels.a is always gonna be true at this point
			hasRotAcceptor = true;
		}
		else if(linkElementNumber==0)
		{
			acc = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(getFacing()));
			if(acc instanceof IRotationAcceptor rotAcc&&rotAcc.sideAcceptsRotation(getFacing()))
			{
				linkedWheels = searchAndSetLink((byte)1, getFacing().getOpposite());
				hasRotAcceptor = linkedWheels.a; //could be indirectly linked to another RotationAcceptor
			}
		}

		if(!hasRotAcceptor)
			setPerTickAndAdvance(speedFactor*getPower());
		else if(linkedWheels.a)
		{
			double power = linkedWheels.c;
			List<WatermillBlockEntity> connectedWheels = linkedWheels.b;
			double newPerTick = speedFactor*power/connectedWheels.size();
			for(ListIterator<WatermillBlockEntity> i = connectedWheels.listIterator(connectedWheels.size()); i.hasPrevious(); ) //last element is this, which imposes its rotation on the other wheels and should be updated first
			{
				WatermillBlockEntity watermill = i.previous();
				watermill.setPerTickAndAdvance(newPerTick);
				if(watermill.rotation!=rotation)
				{
					watermill.rotation = rotation;
					watermill.markContainingBlockForUpdate(null);
				}
			}
			((IRotationAcceptor)acc).inputRotation(Math.abs(power*.75), getFacing().getOpposite());
		}

	}

	private Triple<Boolean, List<WatermillBlockEntity>, Double> searchAndSetLink(byte depth, Direction searchDirection)
	{
		boolean facingAligned = getFacing()==searchDirection;
		boolean linkableChecker = facingAligned;
		double power = 0;
		List<WatermillBlockEntity> connectedWheels = new ArrayList<WatermillBlockEntity>();

		if(depth < maxLinked||!facingAligned) //keep searching when not facingAligned, it might lead to a RotationAcceptor
		{
			BlockEntity blockEntity = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(searchDirection));
			if(canUse(blockEntity))
			{
				if(depth >= maxLinked*2)//stop searching forward for a second RotationAcceptor
					linkableChecker = true;
				else
				{
					Triple<Boolean, List<WatermillBlockEntity>, Double> trickleValues = ((WatermillBlockEntity)blockEntity).searchAndSetLink((byte)(depth+1), searchDirection);
					if(trickleValues.a)
					{
						linkableChecker = true;
						connectedWheels = trickleValues.b;
						power = trickleValues.c;
					}
				}
			}
			else if(!(blockEntity instanceof IRotationAcceptor rotationAcceptor&&rotationAcceptor.sideAcceptsRotation(searchDirection)))
				linkableChecker = true; //sets true for !facingAligned unless it found a RotationAcceptor
		}

		if(linkableChecker&&depth <= maxLinked)
		{
			connectedWheels.add(this);
			power += getPower();
			linkElementNumber = depth;
			if(!facingAligned)
			{
				setFacing(searchDirection);
				markContainingBlockForUpdate(null);
			}
		}

		return new Triple<Boolean, List<WatermillBlockEntity>, Double>(linkableChecker, connectedWheels, power);
	}

	private void dissolveLink()
	{
		BlockEntity be = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(getFacing()));
		if(be instanceof WatermillBlockEntity watermill&&watermill.getFacing()==getFacing()&&watermill.linkElementNumber==linkElementNumber+1)
			watermill.dissolveLink();
		linkElementNumber = 0;
	}

	@Override
	protected void onNeighborBlockChange(BlockPos otherPos)
	{
		super.onNeighborBlockChange(otherPos);
		if(linkElementNumber > 0&&getBlockPos().relative(getFacing().getOpposite()).equals(otherPos)) //if connection downward changed
		{
			BlockEntity be = SafeChunkUtils.getSafeBE(level, otherPos);
			if(linkElementNumber > 1)
			{
				if(be instanceof WatermillBlockEntity watermill&&(watermill.linkElementNumber==0||(watermill.getFacing()==this.getFacing()&&watermill.linkElementNumber==linkElementNumber-1)))
					return;
			}
			else
			{
				if(be instanceof IRotationAcceptor rotationAcceptor&&rotationAcceptor.sideAcceptsRotation(getFacing()))
					return;
			}
			dissolveLink();
		}
	}

	private void setPerTickAndAdvance(double newValue)
	{
		if(newValue!=perTick)
		{
			perTick = newValue;
			markContainingBlockForUpdate(null);
		}
		rotation += perTick;
		rotation %= 1;
	}

	private boolean canUse(@Nullable BlockEntity tileEntity)
	{
		if(!(tileEntity instanceof WatermillBlockEntity watermill))
			return false;
		return !isDummy()&&(watermill.getFacing()==getFacing()||(watermill.getFacing()==getFacing().getOpposite()&&watermill.linkElementNumber==0))
				&&!watermill.isBlocked();
	}

	public boolean isBlocked()
	{
		if(level==null)
			return true;
		for(Direction fdY : new Direction[]{Direction.UP, Direction.DOWN})
			for(Direction fdW : getFacing().getAxis()==Axis.Z?new Direction[]{Direction.EAST, Direction.WEST}: new Direction[]{Direction.SOUTH, Direction.NORTH})
			{
				BlockPos pos = getBlockPos().relative(fdW, 2).relative(fdY, 2);
				BlockState state = level.getBlockState(pos);
				if(Block.isFaceFull(state.getShape(level, pos), fdW.getOpposite()))
					return true;
				if(Block.isFaceFull(state.getShape(level, pos), fdY.getOpposite()))
					return true;
			}
		return false;
	}

	public double getPower()
	{
		return getFacing().getAxis()==Axis.Z?-getRotationVec().x: getRotationVec().z;
	}

	public Vec3 getRotationVec()
	{
		if(rotationVec==null)
		{
			rotationVec = new Vec3(0, 0, 0);
			Vec3 dirHoz = getHorizontalVec();
			Vec3 dirVer = getVerticalVec();
			rotationVec = rotationVec.add(dirHoz);
			rotationVec = rotationVec.add(dirVer);
			//			world.addBlockEvent(xCoord, yCoord, zCoord, getBlockState(), (int)((float)rotationVec.xCoord*10000f), (int)((float)rotationVec.zCoord*10000f));
		}
		return rotationVec;
	}

	private Vec3 getHorizontalVec()
	{
		Vec3 dir = new Vec3(0, 0, 0);
		boolean faceZ = getFacing().ordinal() <= 3;
		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(-(faceZ?1: 0), +3, -(faceZ?0: 1))));
		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(0, +3, 0)));
		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(+(faceZ?1: 0), +3, +(faceZ?0: 1))));

		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(-(faceZ?2: 0), +2, -(faceZ?0: 2))));
		dir = dir.add(Utils.getFlowVector(level, getBlockPos().offset(+(faceZ?2: 0), +2, +(faceZ?0: 2))));

		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(-(faceZ?2: 0), -2, -(faceZ?0: 2))));
		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(+(faceZ?2: 0), -2, +(faceZ?0: 2))));
		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(-(faceZ?1: 0), -3, -(faceZ?0: 1))));
		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(0, -3, 0)));
		dir = dir.subtract(Utils.getFlowVector(level, getBlockPos().offset(+(faceZ?1: 0), -3, +(faceZ?0: 1))));

		return dir;
	}

	private Vec3 getVerticalVec()
	{
		Vec3 dir = new Vec3(0, 0, 0);

		Vec3 dirNeg = new Vec3(0, 0, 0);
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?2: 0), 2, -(getFacing().getAxis()==Axis.Z?0: 2))));
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?3: 0), 1, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?3: 0), 0, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?3: 0), -1, -(getFacing().getAxis()==Axis.Z?0: 3))));
		dirNeg = dirNeg.add(Utils.getFlowVector(level, getBlockPos().offset(-(getFacing().getAxis()==Axis.Z?2: 0), -2, -(getFacing().getAxis()==Axis.Z?0: 2))));
		Vec3 dirPos = new Vec3(0, 0, 0);
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?2: 0), 2, (getFacing().getAxis()==Axis.Z?0: 2))));
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?3: 0), 1, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?3: 0), 0, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?3: 0), -1, (getFacing().getAxis()==Axis.Z?0: 3))));
		dirPos = dirPos.add(Utils.getFlowVector(level, getBlockPos().offset((getFacing().getAxis()==Axis.Z?2: 0), -2, (getFacing().getAxis()==Axis.Z?0: 2))));
		if(getFacing().getAxis()==Axis.Z)
			dir = dir.add(dirNeg.y-dirPos.y, 0, 0);
		else
			dir = dir.add(0, 0, dirNeg.y-dirPos.y);
		return dir;
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		rotationVec = new Vec3(id/10000f, 0, arg/10000f);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		offset = nbt.getIntArray("offset");
		rotation = nbt.getFloat("rotation");
		perTick = nbt.getDouble("perTick");

		if(offset==null||offset.length < 2)
			offset = new int[]{0, 0};
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putIntArray("offset", offset);
		nbt.putFloat("rotation", rotation);
		nbt.putDouble("perTick", perTick);
	}

	private AABB renderAABB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(offset[0]==0&&offset[1]==0)
				renderAABB = new AABB(
						getBlockPos().getX()-(getFacing().getAxis()==Axis.Z?2: 0),
						getBlockPos().getY()-2,
						getBlockPos().getZ()-(getFacing().getAxis()==Axis.Z?0: 2),
						getBlockPos().getX()+(getFacing().getAxis()==Axis.Z?3: 1),
						getBlockPos().getY()+3,
						getBlockPos().getZ()+(getFacing().getAxis()==Axis.Z?1: 3)
				);
			else
				renderAABB = new AABB(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), getBlockPos().getX()+1, getBlockPos().getY()+1, getBlockPos().getZ()+1);
		return renderAABB;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public boolean isDummy()
	{
		return offset[0]!=0||offset[1]!=0;
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getBlockPos().offset(getFacing().getAxis()==Axis.Z?-offset[0]: 0, -offset[1], getFacing().getAxis()==Axis.Z?0: -offset[0]);
		BlockEntity te = SafeChunkUtils.getSafeBE(level, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if(((hh > -2&&hh < 2)||(ww > -2&&ww < 2))&&(hh!=0||ww!=0))
				{
					BlockPos pos2 = worldPosition.offset(getFacing().getAxis()==Axis.Z?ww: 0, hh, getFacing().getAxis()==Axis.Z?0: ww);
					level.setBlockAndUpdate(pos2, state);
					WatermillBlockEntity dummy = (WatermillBlockEntity)level.getBlockEntity(pos2);
					dummy.setFacing(getFacing());
					dummy.offset = new int[]{ww, hh};
				}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		if(beingBroken)
			return;
		BlockPos initPos = pos.offset(getFacing().getAxis()==Axis.Z?-offset[0]: 0, -offset[1], getFacing().getAxis()==Axis.X?-offset[0]: 0);
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if((hh > -2&&hh < 2)||(ww > -2&&ww < 2))
				{
					BlockPos pos2 = initPos.offset(getFacing().getAxis()==Axis.Z?ww: 0, hh, getFacing().getAxis()==Axis.X?ww: 0);
					if(level.getBlockEntity(pos2) instanceof WatermillBlockEntity dummy)
					{
						dummy.beingBroken = true;
						level.removeBlock(pos2, false);
					}
				}
	}
}
