/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;

public abstract class MultiblockCapability<T>
{
	public static <BE, T> MultiblockCapability<T> make(
			Function<BE, MultiblockCapability<T>> getCap, Function<BE, BE> getMaster, BE owner, LazyOptional<T> ownValue
	)
	{
		return new Impl<>(getCap, getMaster, owner, ownValue);
	}

	private final LazyOptional<T> ownValue;

	private MultiblockCapability(LazyOptional<T> ownValue)
	{
		this.ownValue = ownValue;
	}

	public abstract LazyOptional<T> get();

	public final <T2> LazyOptional<T2> getAndCast()
	{
		return get().cast();
	}

	// Using inheritance to "hide" the extra type parameter from users
	private static final class Impl<T, BE> extends MultiblockCapability<T>
	{
		private final BE owner;
		private final Function<BE, MultiblockCapability<T>> getCap;
		private final Function<BE, BE> getMaster;
		private LazyOptional<T> cached = LazyOptional.empty();

		public Impl(
				Function<BE, MultiblockCapability<T>> getCap, Function<BE, BE> getMaster,
				BE owner, LazyOptional<T> ownValue
		)
		{
			super(ownValue);
			this.owner = owner;
			this.getCap = getCap;
			this.getMaster = getMaster;
		}

		@Override
		public LazyOptional<T> get()
		{
			if(!cached.isPresent())
			{
				BE master = getMaster.apply(owner);
				if(master!=null)
					cached = getCap.apply(master).ownValue;
			}
			return cached;
		}
	}
}
