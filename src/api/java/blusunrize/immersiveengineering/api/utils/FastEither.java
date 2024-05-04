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
import com.mojang.datafixers.util.Either;

import java.util.Objects;
import java.util.Optional;
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

	public static <L, R> FastEither<L, R> fromDFU(Either<L, R> l)
	{
		return l.map(FastEither::left, FastEither::right);
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

	public boolean isRight()
	{
		return right!=null;
	}

	public L leftNonnull()
	{
		return Preconditions.checkNotNull(left);
	}

	public R rightNonnull()
	{
		return Preconditions.checkNotNull(right);
	}

	public Optional<R> rightOptional()
	{
		return Optional.ofNullable(right);
	}

	public Optional<L> leftOptional()
	{
		return Optional.ofNullable(left);
	}

	public <T> T map(Function<L, T> left, Function<R, T> right)
	{
		if(isLeft())
			return left.apply(leftNonnull());
		else
			return right.apply(rightNonnull());
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		FastEither<?, ?> that = (FastEither<?, ?>)o;
		return Objects.equals(left, that.left)&&Objects.equals(right, that.right);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(left, right);
	}

	public Either<L, R> toDFU()
	{
		return map(Either::left, Either::right);
	}
}
