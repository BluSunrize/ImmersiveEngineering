/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

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
			return state.getBlock()!=Blocks.CHEST||state.getValue(ChestBlock.TYPE)==ChestType.SINGLE;
		});
	}

	public static boolean rotateBlock(Level world, BlockPos pos, boolean inverse)
	{
		return rotateBlock(world, pos, inverse?Rotation.COUNTERCLOCKWISE_90: Rotation.CLOCKWISE_90);
	}

	public static boolean rotateBlock(Level world, BlockPos pos, Rotation rotation) {
		for(RotationBlacklistEntry e : blacklist)
			if(!e.blockRotation(world, pos))
				return false;

		BlockState state = world.getBlockState(pos);
		BlockState newState = state.rotate(world, pos, rotation);
		if(newState!=state)
		{
			world.setBlockAndUpdate(pos, newState);
			for(Direction d : DirectionUtils.VALUES)
			{
				final BlockPos otherPos = pos.relative(d);
				final BlockState otherState = world.getBlockState(otherPos);
				final BlockState nextState = newState.updateShape(d, otherState, world, pos, otherPos);
				if(nextState!=newState)
				{
					if(!nextState.isAir())
					{
						world.setBlockAndUpdate(pos, nextState);
						newState = nextState;
					}
					else
					{
						world.setBlockAndUpdate(pos, state);
						return false;
					}
				}
			}
			for(Direction d : DirectionUtils.VALUES)
			{
				final BlockPos otherPos = pos.relative(d);
				final BlockState otherState = world.getBlockState(otherPos);
				final BlockState nextOther = otherState.updateShape(d.getOpposite(), newState, world, otherPos, pos);
				if(nextOther!=otherState)
					world.setBlockAndUpdate(otherPos, nextOther);
			}
			return true;
		}
		else
			return false;
	}

	public static boolean rotateEntity(Entity entity, Player player)
	{
		if(entity instanceof ArmorStand)
		{
			((ArmorStand)entity).yRot += 22.5;
			((ArmorStand)entity).yRot %= 360;
		}
		return false;
	}

	public interface RotationBlacklistEntry
	{
		boolean blockRotation(Level w, BlockPos pos);
	}
}