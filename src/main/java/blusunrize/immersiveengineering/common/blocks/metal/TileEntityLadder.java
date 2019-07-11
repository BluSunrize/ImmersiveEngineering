/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//TODO can this be done with just blockstates now?
public class TileEntityLadder extends TileEntityIEBase implements IDirectionalTile
{
	public static TileEntityType<TileEntityLadder> TYPE;
	public Direction facing = Direction.NORTH;

	public TileEntityLadder()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		facing = Direction.byIndex(nbt.getInt("facing"));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("facing", facing.ordinal());
	}

	@Override
	public Direction getFacingForPlacement(LivingEntity placer, BlockPos pos, Direction side, float hitX, float hitY, float hitZ)
	{
		World world = placer.getEntityWorld();
		BlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof BlockMetalLadder)
		{
			BlockMetalLadder ladder = (BlockMetalLadder)state.getBlock();
			if(side.getAxis().isHorizontal()&&ladder.canAttachTo(world, pos.offset(side.getOpposite()), facing))
				return side;
			else if(ladder.getMetaFromState(state) > 0)
				return placer.getAdjustedHorizontalFacing().getOpposite();
			else
			{
				for(Direction enumfacing : Plane.HORIZONTAL)
					if(ladder.canAttachTo(world, pos.offset(enumfacing.getOpposite()), enumfacing))
						return enumfacing;
			}
		}
		return Direction.NORTH;
	}

	@Override
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return -1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}
}