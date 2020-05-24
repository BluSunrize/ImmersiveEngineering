/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RotationUtil
{
	public static final List<RotationBlacklistEntry> blacklist = new ArrayList<>();

	static
	{
		//Double chests don't implement updatePostPlacement correctly
		blacklist.add((w, pos) -> {
			BlockState state = w.getBlockState(pos);
			return state.getBlock()!=Blocks.CHEST||state.get(ChestBlock.TYPE)==ChestType.SINGLE;
		});
	}

	public static boolean rotateBlock(World world, BlockPos pos, boolean inverse)
	{
		return rotateBlock(world, pos, inverse?Rotation.COUNTERCLOCKWISE_90: Rotation.CLOCKWISE_90);
	}

	public static boolean rotateBlock(World world, BlockPos pos, Rotation rotation) {
		for(RotationBlacklistEntry e : blacklist)
			if(!e.blockRotation(world, pos))
				return false;

		BlockState state = world.getBlockState(pos);
		BlockState newState = state.rotate(world, pos, rotation);
		if(newState!=state)
		{
			world.setBlockState(pos, newState);
			for(Direction d : Direction.VALUES)
			{
				final BlockPos otherPos = pos.offset(d);
				final BlockState otherState = world.getBlockState(otherPos);
				final BlockState nextState = newState.updatePostPlacement(d, otherState, world, pos, otherPos);
				if(nextState!=newState)
				{
					if(!nextState.isAir())
					{
						world.setBlockState(pos, nextState);
						newState = nextState;
					}
					else
					{
						world.setBlockState(pos, state);
						return false;
					}
				}
			}
			for(Direction d : Direction.VALUES)
			{
				final BlockPos otherPos = pos.offset(d);
				final BlockState otherState = world.getBlockState(otherPos);
				final BlockState nextOther = otherState.updatePostPlacement(d.getOpposite(), newState, world, otherPos, pos);
				if(nextOther!=otherState)
					world.setBlockState(otherPos, nextOther);
			}
			return true;
		}
		else
			return false;
	}

	public static boolean rotateEntity(Entity entity, PlayerEntity player)
	{
		if(entity instanceof ArmorStandEntity)
		{
			((ArmorStandEntity)entity).rotationYaw += 22.5;
			((ArmorStandEntity)entity).rotationYaw %= 360;
		}
		return false;
	}

	public interface RotationBlacklistEntry
	{
		boolean blockRotation(World w, BlockPos pos);
	}
}