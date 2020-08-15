/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TurntableTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, IHammerInteraction
{
	public static TileEntityType<TurntableTileEntity> TYPE;
	//rotationMapping is rotating clockwise around the face of the turntable, starting at NORTH for top/bottom facing turntables and DOWN for sideways facing turntables
	private Rotation[] rotationMapping = new Rotation[]{Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90};
	private boolean[] redstone = {false, false, false, false};
	private static final int[] rotationDirectionIndexMap = {0, 2, 1, 3, 1};

	public TurntableTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
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
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
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
		BlockPos difference = otherPos.subtract(pos);
		Direction otherDir = Direction.getFacingFromVector(difference.getX(), difference.getY(), difference.getZ());
		if(otherDir.getAxis()!=facing.getAxis())
		{
			boolean r = this.world.isSidePowered(pos.offset(otherDir), otherDir);

			int directionIndex = getRotationDirectionIndexFromFacing(otherDir, facing);

			if(r!=this.redstone[directionIndex])
			{
				this.redstone[directionIndex] = r;
				if(this.redstone[directionIndex])
				{
					BlockPos target = pos.offset(facing);
					RotationUtil.rotateBlock(this.world, target, rotationMapping[directionIndex]);
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
		return placer.isSneaking();
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, Vec3d hitVec)
	{
		Direction facing = getFacing();
		if(player.isSneaking()&&side.getAxis()!=facing.getAxis())
		{
			if(!world.isRemote)
			{
				int directionIndex = getRotationDirectionIndexFromFacing(side, facing);
				rotationMapping[directionIndex] = intToRotation((rotationToInt(rotationMapping[directionIndex])%3)+1); //looks strange, but made to avoid values of <1 and >3
				markDirty();
				world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 254, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
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
		int index = indexee.getIndex();
		if (facing.getIndex()<index)
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