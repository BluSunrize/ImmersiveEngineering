/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.utils;

import com.google.common.base.Preconditions;

import java.util.function.Function;

public class FastEither<L, R>
{
	private final L left;
	private final R right;

	private FastEither(L left, R right)
	{
		Preconditions.checkState((left!=null)^(right!=null));
		this.left = left;
		this.right = right;
	}

	public static <L, R> FastEither<L, R> left(L l)
	{
		return new FastEither<>(l, null);
	}

	public static <L, R> FastEither<L, R> right(R r)
	{
		return new FastEither<>(null, r);
	}

	public boolean isLeft()
	{
		return left!=null;
	}

	public L leftNonnull()
	{
		return Preconditions.checkNotNull(left);
	}

	public R rightNonnull()
	{
		return Preconditions.checkNotNull(right);
	}

	public <T> T map(Function<L, T> left, Function<R, T> right)
	{
		if(isLeft())
			return left.apply(leftNonnull());
		else
			return right.apply(rightNonnull());
	}
}
