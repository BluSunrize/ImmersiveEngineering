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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.INeighbourChangeTile;
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
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TurntableTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, INeighbourChangeTile, IHammerInteraction
{
	public static TileEntityType<TurntableTileEntity> TYPE;
	//rotationMapping is rotating clockwise around the face of the turntable, starting at North for top/bottom facing turntables and Top for sideways facing turntables
	private Rotation[] rotationMapping = new Rotation[]{Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90};
	private boolean[] redstone = {false, false, false, false};

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
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
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
		int index = 0;
		Direction indexFinder = facing.getAxis()==Axis.Y?Direction.NORTH: Direction.UP;
		while(indexee!=indexFinder&&index < 4)
		{
			indexFinder = indexFinder.rotateAround(facing.getAxis());
			index++;
		}
		if(index >= 4)
			throw new IllegalStateException("Unable to get "+facing.getAxis().getName2()+"-rotated facing of "+indexee);
		if(facing.getAxisDirection()==AxisDirection.NEGATIVE)
			index = Math.floorMod(-index, 4);
		return index;
	}

	public Rotation getRotationFromSide(Direction side)
	{
		Direction facing = getFacing();
		if(side.getAxis()==facing.getAxis())
			return Rotation.NONE;
		return rotationMapping[getRotationDirectionIndexFromFacing(side, facing)];
	}
}