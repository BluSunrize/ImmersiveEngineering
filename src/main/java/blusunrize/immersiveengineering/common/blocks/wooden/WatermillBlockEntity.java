/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.IESounds;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WatermillBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IEClientTickableBE, IStateBasedDirectional, IHasDummyBlocks
{
	public int[] offset = {0, 0};
	public float rotation = 0;
	private Vec3 rotationVec = null;
	// Indicates that the next tick should be skipped since the waterwheel is being controlled by another waterwheel
	// attached to it
	public boolean multiblock = false;
	private boolean beingBroken = false;
	public double perTick;
	private IEBlockCapabilityCache<IRotationAcceptor> outputCap = IEBlockCapabilityCaches.forNeighbor(
			IRotationAcceptor.CAPABILITY, this, () -> getFacing().getOpposite()
	);
	private IEBlockCapabilityCache<IRotationAcceptor> reverseOutputCap = IEBlockCapabilityCaches.forNeighbor(
			IRotationAcceptor.CAPABILITY, this, this::getFacing
	);

	public WatermillBlockEntity(BlockEntityType<WatermillBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void tickClient()
	{
		rotation += perTick;
		rotation %= 1;
		ImmersiveEngineering.proxy.handleTileSound(IESounds.wooden_creaking, this, !isBlocked(), 0.5f, 1f);
	}

	@Override
	public void tickServer()
	{
		if(isBlocked())//TODO throttle?
		{
			setPerTickAndAdvance(0);
			return;
		}
		if(level.getGameTime()%64==((getBlockPos().getX()^getBlockPos().getZ())&63))
			rotationVec = null;
		if(multiblock)
		{
			multiblock = false;
			return;
		}

		IRotationAcceptor dynamo = outputCap.getCapability();
		Direction expandTo = getFacing();
		if(dynamo==null)
		{
			dynamo = reverseOutputCap.getCapability();
			expandTo = expandTo.getOpposite();
		}
		if(dynamo!=null)
		{
			double power = getPower();
			List<WatermillBlockEntity> connectedWheels = new ArrayList<>();
			for(int i = 1; i < 3; ++i)
			{
				BlockEntity blockEntity = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(expandTo, i));
				if(!canUse(blockEntity))
					break;
				WatermillBlockEntity asWatermill = (WatermillBlockEntity)blockEntity;
				connectedWheels.add(asWatermill);
				power += asWatermill.getPower();
			}

			// +1: Self is not included in list of connected wheels
			setPerTickAndAdvance(1f/1440*power/(connectedWheels.size()+1));
			for(WatermillBlockEntity watermill : connectedWheels)
			{
				watermill.setPerTickAndAdvance(perTick);
				if(watermill.rotation!=rotation)
				{
					watermill.rotation = rotation;
					watermill.markContainingBlockForUpdate(null);
				}
				watermill.multiblock = true;
			}

			dynamo.inputRotation(Math.abs(power*.75));
		}
		else
			setPerTickAndAdvance(1f/1440*getPower());
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
		return watermill.offset[0]==0&&watermill.offset[1]==0
				&&(watermill.getFacing()==getFacing()||watermill.getFacing()==getFacing().getOpposite())
				&&!watermill.isBlocked()&&!watermill.multiblock;
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

	public AABB renderAABB;

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
