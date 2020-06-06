/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BlockMatcher
{
	private final static List<MatcherPredicate> GLOBAL_MATCHERS = new ArrayList<>();

	public static void addPredicate(MatcherPredicate newPredicate)
	{
		GLOBAL_MATCHERS.add(newPredicate);
	}

	public static Result matches(BlockState expected, BlockState found, World world, BlockPos pos)
	{
		return matches(expected, found, world, pos, ImmutableList.of());
	}

	public static Result matches(BlockState expected, BlockState found, World world, BlockPos pos,
								 List<MatcherPredicate> additional)
	{
		return Stream.concat(GLOBAL_MATCHERS.stream(), additional.stream())
				.map(pred -> pred.matches(expected, found, world, pos))
				.reduce(Result.DEFAULT, Result::combine);
	}

	public interface MatcherPredicate
	{
		Result matches(BlockState expected, BlockState found, @Nullable World world, @Nullable BlockPos pos);
	}

	public static class Result
	{
		final int strength;
		final Event.Result type;

		private Result(int strength, Event.Result type)
		{
			this.strength = strength;
			this.type = type;
		}

		public boolean isAllow()
		{
			return type==Event.Result.ALLOW;
		}

		public boolean isDefault()
		{
			return type==Event.Result.DEFAULT;
		}

		public boolean isDeny()
		{
			return type==Event.Result.DENY;
		}

		public static Result combine(Result a, Result b)
		{
			if(Math.abs(a.strength) > Math.abs(b.strength))
				return a;
			else if(Math.abs(a.strength) < Math.abs(b.strength))
				return b;
			else
			{
				Preconditions.checkState(a.type==b.type,
						"Can't combine conflicting results of same strength");
				return a;
			}
		}

		public static final Result DEFAULT = new Result(0, Event.Result.DEFAULT);

		public static Result allow(int strength)
		{
			Preconditions.checkArgument(strength > 0);
			return new Result(strength, Event.Result.ALLOW);
		}

		public static Result deny(int strength)
		{
			Preconditions.checkArgument(strength > 0);
			return new Result(strength, Event.Result.DENY);
		}
	}
}
