/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.orientation.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class TurntableBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional, IHammerInteraction
{
	//rotationMapping is rotating clockwise around the face of the turntable, starting at NORTH for top/bottom facing turntables and DOWN for sideways facing turntables
	private Rotation[] rotationMapping = new Rotation[]{Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90};
	private boolean[] redstone = {false, false, false, false};
	private static final int[] rotationDirectionIndexMap = {0, 2, 1, 3, 1};

	public TurntableBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.TURNTABLE.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		byte redstoneByte = nbt.getByte("redstone");
		byte rotationMapValue = nbt.getByte("rotationMapping");
		for(int i = 0; i < rotationMapping.length; i++)
		{
			rotationMapping[i] = intToRotation((rotationMapValue >> 2*i)&3);
			redstone[i] = (redstoneByte&(1<<i))!=0;
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		byte rotationMapValue = 0;
		byte redstoneByte = 0;
		for(int i = 0; i < rotationMapping.length; i++)
		{
			rotationMapValue += rotationToInt(rotationMapping[i])<<2*i;
			if(redstone[i])
				redstoneByte += 1<<i;
		}
		nbt.putByte("redstone", redstoneByte);
		nbt.putByte("rotationMapping", rotationMapValue);
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		super.onNeighborBlockChange(otherPos);
		Direction facing = getFacing();
		BlockPos difference = otherPos.subtract(worldPosition);
		Direction otherDir = Direction.getNearest(difference.getX(), difference.getY(), difference.getZ());
		if(otherDir.getAxis()!=facing.getAxis())
		{
			boolean r = this.level.hasSignal(worldPosition.relative(otherDir), otherDir);

			int directionIndex = getRotationDirectionIndexFromFacing(otherDir, facing);

			if(r!=this.redstone[directionIndex])
			{
				this.redstone[directionIndex] = r;
				if(this.redstone[directionIndex])
				{
					BlockPos target = worldPosition.relative(facing);
					RotationUtil.rotateBlock(this.level, target, rotationMapping[directionIndex]);
				}
			}
		}
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return placer.isShiftKeyDown();
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return !entity.isShiftKeyDown();
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		Direction facing = getFacing();
		if(player.isShiftKeyDown()&&side.getAxis()!=facing.getAxis())
		{
			if(!level.isClientSide)
			{
				int directionIndex = getRotationDirectionIndexFromFacing(side, facing);
				rotationMapping[directionIndex] = intToRotation((rotationToInt(rotationMapping[directionIndex])%3)+1); //looks strange, but made to avoid values of <1 and >3
				setChanged();
				level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 254, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	private Rotation intToRotation(int rotationValue)
	{
		switch(rotationValue)
		{
			case 2:
				return Rotation.CLOCKWISE_180;
			case 3:
				return Rotation.COUNTERCLOCKWISE_90;
			case 0: //illegal value, replace by default one
			case 1:
			default:
				return Rotation.CLOCKWISE_90;
		}
	}

	private int rotationToInt(Rotation rotation)
	{
		switch(rotation)
		{
			case CLOCKWISE_180:
				return 2;
			case COUNTERCLOCKWISE_90:
				return 3;
			case NONE: //illegal value, replace by default one
			case CLOCKWISE_90:
			default:
				return 1;
		}
	}

	private int getRotationDirectionIndexFromFacing(Direction indexee, Direction facing)
	{
		int index = indexee.get3DDataValue();
		if (facing.get3DDataValue()<index)
			index -= 2;
		if (index >= 2 && ((facing.getAxisDirection()==AxisDirection.POSITIVE) != (facing.getAxis()==Axis.Z)))
			index++;
 		return rotationDirectionIndexMap[index];
	}

	public Rotation getRotationFromSide(Direction side)
	{
		Direction facing = getFacing();
		if(side.getAxis()==facing.getAxis())
			return Rotation.NONE;
		return rotationMapping[getRotationDirectionIndexFromFacing(side, facing)];
	}

	public void rotateRotationMap(Rotation rotation) {
		if (rotation == Rotation.NONE)
			return;
		int offset = rotationToInt(rotation);
		for(int i = 0; i < rotationMapping.length; i++)
			rotationMapping[i] = rotationMapping[Math.floorMod(i-offset, rotationMapping.length)];
	}

	public void verticalTransitionRotationMap(Direction facingOld, Direction facingNew) {
		//see if it transition is to or from vertical. return if none of the directions are vertical
		boolean toVert = false;
		if (facingNew.getAxis() == Axis.Y)
			toVert = true;
		if ((facingOld.getAxis() == Axis.Y) == toVert)
			return;

		Direction horizontalFacing = (toVert ? facingOld : facingNew);
		int offset;
		if (horizontalFacing.getAxis() == Axis.Z)
			offset = facingNew.getAxisDirection() == facingOld.getAxisDirection() ? 2 : 0;
		else
			offset = (horizontalFacing.getAxisDirection() == AxisDirection.POSITIVE) == toVert ? 3 : 1;

		Rotation[] oldRotMap = rotationMapping.clone();
		boolean[] oldRedstone = redstone.clone();
		for(int i = 0; i < rotationMapping.length; i++)
		{
			int sourceIndex = Math.floorMod(i + offset, rotationMapping.length);
			rotationMapping[i] = oldRotMap[sourceIndex];
			redstone[i] = oldRedstone[sourceIndex]; //rotate redstone, too, to prevent toggling
		}
	}
}