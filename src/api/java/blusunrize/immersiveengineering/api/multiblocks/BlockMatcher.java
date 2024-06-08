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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BlockMatcher
{
	private final static List<MatcherPredicate> GLOBAL_MATCHERS = new ArrayList<>();
	private final static List<Preprocessor> PREPROCESSING = new ArrayList<>();

	public static void addPredicate(MatcherPredicate newPredicate)
	{
		GLOBAL_MATCHERS.add(newPredicate);
	}

	public static void addPreprocessor(Preprocessor preprocessor)
	{
		PREPROCESSING.add(preprocessor);
	}

	public static Result matches(BlockState expected, BlockState found, Level world, BlockPos pos)
	{
		return matches(expected, found, world, pos, ImmutableList.of());
	}

	public static Result matches(BlockState expected, BlockState found, Level world, BlockPos pos,
								 List<MatcherPredicate> additional)
	{
		for(Preprocessor p : PREPROCESSING)
			found = p.preprocessFoundState(expected, found, world, pos);
		BlockState finalFound = found;
		return Stream.concat(GLOBAL_MATCHERS.stream(), additional.stream())
				.map(pred -> pred.matches(expected, finalFound, world, pos))
				.reduce(Result.DEFAULT, Result::combine);
	}

	public interface MatcherPredicate
	{
		Result matches(BlockState expected, BlockState found, @Nullable Level world, @Nullable BlockPos pos);
	}

	public interface Preprocessor
	{
		BlockState preprocessFoundState(
				BlockState expected, BlockState found, @Nullable Level world, @Nullable BlockPos pos
		);
	}

	public static class Result
	{
		final int strength;
		final ResultType type;

		private Result(int strength, ResultType type)
		{
			this.strength = strength;
			this.type = type;
		}

		public boolean isAllow()
		{
			return type==ResultType.ALLOW;
		}

		public boolean isDefault()
		{
			return type==ResultType.DEFAULT;
		}

		public boolean isDeny()
		{
			return type==ResultType.DENY;
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

		public static final Result DEFAULT = new Result(0, ResultType.DEFAULT);

		public static Result allow(int strength)
		{
			Preconditions.checkArgument(strength > 0);
			return new Result(strength, ResultType.ALLOW);
		}

		public static Result deny(int strength)
		{
			Preconditions.checkArgument(strength > 0);
			return new Result(strength, ResultType.DENY);
		}
	}

	public enum ResultType
	{
		ALLOW, DEFAULT, DENY
	}
}
