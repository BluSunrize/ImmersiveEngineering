/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import net.neoforged.neoforge.common.util.LazyOptional;

import java.util.function.Function;

public abstract class MultiblockCapability<T>
{
	public static <BE, T> MultiblockCapability<T> make(
			BE owner, Function<BE, MultiblockCapability<T>> getCap, Function<BE, BE> getMaster, ResettableCapability<T> ownValue
	)
	{
		return new Impl<>(getCap, getMaster, owner, ownValue);
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
		private final ResettableCapability<T> ownValue;
		private final Function<BE, MultiblockCapability<T>> getCap;
		private final Function<BE, BE> getMaster;
		private LazyOptional<T> cached = LazyOptional.empty();

		public Impl(
				Function<BE, MultiblockCapability<T>> getCap, Function<BE, BE> getMaster,
				BE owner, ResettableCapability<T> ownValue
		)
		{
			this.owner = owner;
			this.getCap = getCap;
			this.getMaster = getMaster;
			this.ownValue = ownValue;
			// Hack, kind of but not: When a dummy is broken or unloaded, invalidate the cap it provided.
			// ResettableCapability ensures that a new LO will be created if the cap is queried again (e.g. if only the
			// dummy and not the master unloaded)
			this.ownValue.addResetListener(cached::invalidate);
		}

		@Override
		public LazyOptional<T> get()
		{
			if(!cached.isPresent())
			{
				BE master = getMaster.apply(owner);
				if(master!=null)
					cached = ((Impl<T, ?>)getCap.apply(master)).ownValue.getLO();
			}
			return cached;
		}
	}
}
