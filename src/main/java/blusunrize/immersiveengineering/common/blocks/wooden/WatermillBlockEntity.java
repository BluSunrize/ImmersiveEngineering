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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
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
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
import java.util.Arrays;
import java.util.List;

public class WatermillBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IEClientTickableBE, IStateBasedDirectional, IHasDummyBlocks, ISoundBE
{
	public int[] offset = {0, 0};
	private double rotation = 0;
	private double speed = 0;
	private double powerOut = 0;
	private long deferredUpdateTick = 0;
	private boolean beingBroken = false;
	private IEBlockCapabilityCache<IRotationAcceptor> dynamo = null;
	//These are position lists for blocks to check for flowrate. First 11 are breastshot, all 16 are overshot
	private static final List<Vec3> offsetsZ = Arrays.asList(
			new Vec3(+3, +1, 0),
			new Vec3(+3, 0, 0),
			new Vec3(+3, -1, 0),
			new Vec3(+2, -2, 0),
			new Vec3(+1, -3, 0),
			new Vec3(+0, -3, 0),
			new Vec3(-1, -3, 0),
			new Vec3(-2, -2, 0),
			new Vec3(-3, -1, 0),
			new Vec3(-3, 0, 0),
			new Vec3(-3, +1, 0),
			new Vec3(-2, +2, 0),
			new Vec3(-1, +3, 0),
			new Vec3(+0, +3, 0),
			new Vec3(+1, +3, 0),
			new Vec3(+2, +2, 0));
	private static final List<Vec3> offsetsX = Arrays.asList(
			new Vec3(0, +1, +3),
			new Vec3(0, +0, +3),
			new Vec3(0, -1, +3),
			new Vec3(0, -2, +2),
			new Vec3(0, -3, +1),
			new Vec3(0, -3, 0),
			new Vec3(0, -3, -1),
			new Vec3(0, -2, -2),
			new Vec3(0, -1, -3),
			new Vec3(0, +0, -3),
			new Vec3(0, +1, -3),
			new Vec3(0, +2, -2),
			new Vec3(0, +3, -1),
			new Vec3(0, +3, 0),
			new Vec3(0, +3, +1),
			new Vec3(0, +2, +2));
	private static final int MAX_WHEELS = 3;

	public WatermillBlockEntity(BlockEntityType<WatermillBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void tickClient()
	{
		rotation += speed;
		rotation %= 1;
		ImmersiveEngineering.proxy.handleTileSound(IESounds.mill_creaking, this, Math.abs(speed) > 0, 0.5f, 1f);
	}

	@Override
	public void tickServer()
	{
		//Update rotation to make sure we sync it with client correctly
		rotation += speed;
		rotation %= 1;
		//Do the rest of the update for the actual wheel logic
		if(dynamo!=null&&powerOut > 0&&dynamo.getCapability()!=null)
		{
			dynamo.getCapability().inputRotation(powerOut);
			if(level.getGameTime()%1024==((getBlockPos().getX()^getBlockPos().getZ())&1023))
				handleUpdate(null);
		}
		if(dynamo==null||level.getGameTime() > deferredUpdateTick)
			handleUpdate(null);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		if(level instanceof ServerLevel)
			if(master() instanceof WatermillBlockEntity master)
				master.setShouldUpdate();
	}

	@Override
	public void onNeighborBlockChange(BlockPos pos)
	{
		super.onNeighborBlockChange(pos);
		if(master() instanceof WatermillBlockEntity master)
			master.setShouldUpdate();
	}

	/**
	 * Handles updating the torque produced by this batch of waterwheels
	 * This method will synchronize rotation across only dynamo-connected waterwheels!
	 *
	 * @param search Direction in which to continue the search, if this is not the starting tile
	 */
	public void handleUpdate(@Nullable Direction search)
	{
		//Recalculate dynamo attachment
		IEBlockCapabilityCache<IRotationAcceptor> facingCap = IEBlockCapabilityCaches.forNeighbor(IRotationAcceptor.CAPABILITY, this, this::getFacing);
		IEBlockCapabilityCache<IRotationAcceptor> oppositeCap = IEBlockCapabilityCaches.forNeighbor(IRotationAcceptor.CAPABILITY, this, () -> getFacing().getOpposite());
		dynamo = facingCap.getCapability()==null?oppositeCap: facingCap;
		//Search for the waterwheel with the dynamo, or process the update if we have the connected dynamo
		if(dynamo.getCapability()!=null)
		{
			//Get the direction to step in
			Direction step = facingCap.getCapability()==null?getFacing(): getFacing().getOpposite();
			//Set power & speed to zero to start calculation
			powerOut = 0;
			speed = 0;
			//Calculate the new torque this wheel will produce
			if (!this.isBlocked())
			{
				//Number of wheels in a row, determined to be at least one
				int wheels = 1;
				//Get total torque across all connected wheels up to maximum, and check how many connected wheels there are
				double torque = getIFScaledTorque();
				for(int i = 1; i < MAX_WHEELS; i++)
				{
					BlockEntity be = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(step, i));
					if(be instanceof WatermillBlockEntity watermill)
						if(watermill.isBlocked())
							break;
						else
							torque += watermill.getIFScaledTorque();
					else
						break;
					wheels++;
				}
				//Calculate torque to use for this wheel
				powerOut = Math.abs(torque);
				speed = 0.00025*torque/wheels;
			}
			//Update client with the data we just set
			level.sendBlockUpdated(getBlockPos(), level.getBlockState(getBlockPos()), level.getBlockState(getBlockPos()), 3);
			//Updated connected waterwheels with torque & speed
			boolean blocked = false;
			for(int i=1; i < MAX_WHEELS; i++)
			{
				BlockPos pos = getBlockPos().relative(step, i);
				BlockEntity be = SafeChunkUtils.getSafeBE(level, pos);
				if(be instanceof WatermillBlockEntity watermill)
				{
					blocked = blocked || watermill.isBlocked();
					watermill.speed = blocked?0: speed;
					watermill.rotation = blocked?0: rotation;
				}
				//Update client with the data we just set
				level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
			}
		}
		//Start the search and go both directions as necessary
		else if(search==null)
		{
			BlockEntity be = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(getFacing()));
			if(be instanceof WatermillBlockEntity watermill) watermill.handleUpdate(getFacing());
			be = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(getFacing().getOpposite()));
			if(be instanceof WatermillBlockEntity watermill) watermill.handleUpdate(getFacing().getOpposite());
		}
		//Continue the search if we haven't found the dynamo yet
		else
		{
			BlockEntity be = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(search));
			if(be instanceof WatermillBlockEntity watermill) watermill.handleUpdate(search);
		}
		//Set this block's deferred update tick to -1
		deferredUpdateTick = Long.MAX_VALUE;
		//Mark chunk dirty so data saves
		markChunkDirty();
	}

	/**
	 * Returns whether the waterwheel has blocks in its blades preventing it from turning
	 * Blocks without solid faces will not count as blocking the waterwheel
	 * @return boolean for if the watermill can turn
	 */
	public boolean isBlocked()
	{
		if(level==null)
			return true;
		//Check corner blocks for solid blocks
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
		//Check side blocks for solid blocks
		for(Direction side : getFacing().getAxis()==Axis.Z?new Direction[]{Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN}
				: new Direction[]{Direction.SOUTH, Direction.NORTH, Direction.UP, Direction.DOWN})
		{
			BlockPos pos = getBlockPos().relative(side, 3).relative(side.getClockWise(getFacing().getAxis()));
			BlockState state = level.getBlockState(pos);
			if(Block.isFaceFull(state.getShape(level, pos), side.getOpposite()))
				return true;
			pos = getBlockPos().relative(side, 3);
			state = level.getBlockState(pos);
			if(Block.isFaceFull(state.getShape(level, pos), side.getOpposite()))
				return true;
			pos = getBlockPos().relative(side, 3).relative(side.getCounterClockWise(getFacing().getAxis()));
			state = level.getBlockState(pos);
			if(Block.isFaceFull(state.getShape(level, pos), side.getOpposite()))
				return true;
		}
		return false;
	}

	/**
	 * This method used to be getPower(), however power produced is always positive, so it was renamed.
	 * Instead, torque is scaled to another energy unit (IF) and direction is assumed to be about this BE's axis.
	 * Negative or positive torque is then represented as an IF double the magnitude of which is produced power per tick.
	 *
	 * @return double representation of scalar quantity torque about this BE's axis
	 */
	public double getIFScaledTorque()
	{
		boolean zFacing = getFacing().ordinal() <= 3;
		boolean overshot = getOvershot(zFacing);
		//Multiply by 1.25f to get output in IF for the dynamo
		return (zFacing?getTorque(zFacing, overshot).z(): getTorque(zFacing, overshot).x())*1.25f;
	}

	/**
	 * Returns a torque-alike measurement in units of "(scaled flow vector)-blocks".
	 * Scaled flow vector is interpreted as total averaged force on the wheel from the water current.
	 *
	 * @param zAxis    boolean for if the wheel is facing in the Z axis or not
	 * @param overshot boolean for if the wheel should be considered an overshot wheel
	 * @return Vec3 torque output from this wheel
	 */
	public Vec3 getTorque(boolean zAxis, boolean overshot)
	{
		Vec3 torqueVec = overshot?getOvershotTorque(zAxis): getBreastshotTorque(zAxis);
		torqueVec = torqueVec.add(getResistanceTorque(torqueVec, zAxis));
		return torqueVec;
	}

	/**
	 * Overshot waterwheels have water flowing over the top of them, getting most of their power from gravity.
	 * Breastshot & undershot (which can be modeled as low efficiency breastshot) have it coming in on the "breast" of the waterwheel.
	 * This function evaluates the wheel flow and selects between the two 'types' (overshot or breastshot) this wheel can be modeled by.
	 *
	 * @param zAxis boolean for if the wheel is facing in the Z axis or not
	 * @return boolean for if this waterwheel is operating as an overshot wheel
	 */
	private boolean getOvershot(boolean zAxis)
	{
		boolean overshot = false;
		//Top adjacent side positions
		overshot = !level.getFluidState(getBlockPos().offset(-(zAxis?1: 0), +3, -(zAxis?0: 1))).isEmpty()||overshot;
		overshot = !level.getFluidState(getBlockPos().offset(0, +3, 0)).isEmpty()||overshot;
		overshot = !level.getFluidState(getBlockPos().offset(+(zAxis?1: 0), +3, +(zAxis?0: 1))).isEmpty()||overshot;
		//Top corner positions
		overshot = !level.getFluidState(getBlockPos().offset(-(zAxis?2: 0), +2, -(zAxis?0: 2))).isEmpty()||overshot;
		overshot = !level.getFluidState(getBlockPos().offset(+(zAxis?2: 0), +2, +(zAxis?0: 2))).isEmpty()||overshot;
		//Straight downwards entry on tops of sides (0.889 height, 0.778 is our lower bound)
		overshot = (level.getFluidState(getBlockPos().offset(-(zAxis?3: 0), 1, -(zAxis?0: 3))).getOwnHeight() > 0.8)||overshot;
		overshot = (level.getFluidState(getBlockPos().offset(+(zAxis?3: 0), 1, +(zAxis?0: 3))).getOwnHeight() > 0.8)||overshot;
		return overshot;
	}

	/**
	 * Calculates torque in vector form for an overshot wheel, using torque = cross between position and force.
	 * Values are adjusted from straight torque vector calculations to account for 'weight' and 'head'.
	 *
	 * @param zAxis boolean for if the wheel is facing in the Z axis or not
	 * @return Vec3 torque for the waterwheel
	 */
	private Vec3 getOvershotTorque(boolean zAxis)
	{
		Vec3 torque = new Vec3(0, 0, 0);
		for(Vec3 position : zAxis?offsetsZ: offsetsX)
		{
			Vec3i tmp = new Vec3i((int)position.x(), (int)position.y(), (int)position.z());
			torque = torque.add(position.cross(Utils.getScaledFlowVector(level, getBlockPos().offset(tmp))));
		}
		return torque;
	}

	/**
	 * Calculates torque in vector form for a breastshot wheel, using torque = cross between position and force.
	 * Values are adjusted from straight torque vector calculations to account for 'weight' and 'head'.
	 *
	 * @param zAxis boolean for if the wheel is facing in the Z axis or not
	 * @return Vec3 torque for the waterwheel
	 */
	private Vec3 getBreastshotTorque(boolean zAxis)
	{
		Vec3 torque = new Vec3(0, 0, 0);
		for(int i = 0; i < 11; i++)
		{
			Vec3 position = zAxis?offsetsZ.get(i): offsetsX.get(i);
			Vec3i tmp = new Vec3i((int)position.x(), (int)position.y(), (int)position.z());
			torque = torque.add(position.cross(Utils.getScaledFlowVector(level, getBlockPos().offset(tmp))));
		}
		//Return early if we have a small torque - breastshot wheels really need throughspeed, and these won't have it
		if(torque.length() < 3.25) return torque;
		//We add 0.778+2.667*0.8 here to counteract the negative flow velocity of the 'inlet' on breastshot wheels
		//When the water enters 'against' the wheel the cross product makes a torque of -0.778, which severely hampers efficiency
		//2.667 is a perfectly full downwards block, and 0.8 is the approximate average height
		//This also has the helpful effect of diminishing the efficiency of sideways breastshot wheels
		//We then scale by 1.3 to make breastshot wheels competitive with overshot wheels
		return (torque.add(zAxis?0: (torque.x > 0?2.9116: -2.9116), 0, zAxis?(torque.z > 0?2.9116: -2.9116): 0)).scale(1.35f);
	}

	/**
	 * Calculates the viscosity-related torque that comes from having source blocks of fluid in the way of the waterwheel.
	 * Resistance torque will be negative if torque from the stream flow is positive, and is scaled by output torque.
	 *
	 * @param torque the pre-viscosity torque produced by the wheel
	 * @param zAxis  boolean for if the wheel is facing in the Z axis or not
	 * @return Vec3 torque produced by viscosity acting upon the waterwheel
	 */
	private Vec3 getResistanceTorque(Vec3 torque, boolean zAxis)
	{

		Vec3 resistanceTorque = new Vec3(0, 0, 0);
		if(Math.abs(torque.length()) < 0.1f) return resistanceTorque;
		for(Vec3 position : zAxis?offsetsZ: offsetsX)
		{
			Vec3i tmp = new Vec3i((int)position.x(), (int)position.y(), (int)position.z());
			double resistance = level.getFluidState(getBlockPos().offset(tmp)).isSourceOfType(level.getFluidState(getBlockPos().offset(tmp)).getType())?(2+(0.1*torque.length())): 0;
			resistanceTorque = zAxis?resistanceTorque.add(0, 0, torque.z() > 0?-resistance: resistance): resistanceTorque.add(torque.x() > 0?-resistance: resistance, 0, 0);
		}
		return (resistanceTorque.length() > torque.length())?torque.scale(-0.9): resistanceTorque;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		offset = nbt.getIntArray("offset");
		rotation = nbt.getDouble("rotation");
		speed = nbt.getDouble("speed");

		if(offset==null||offset.length < 2)
			offset = new int[]{0, 0};
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.putIntArray("offset", offset);
		nbt.putDouble("rotation", rotation);
		nbt.putDouble("speed", speed);
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

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return Math.abs(speed) > 0;
	}

	public double getRotation()
	{
		return rotation;
	}

	public double getSpeed()
	{
		return speed;
	}

	public void setShouldUpdate()
	{
		if(isDummy()) return;
		deferredUpdateTick = Math.min(deferredUpdateTick, level.getGameTime());
	}
}
