/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.function.Function;

public abstract class MultiblockCapability<T>
{
	public static <BE extends BlockEntity, T> MultiblockCapability<T> make(
			BE owner, Function<BE, MultiblockCapability<T>> getCap, Function<BE, BE> getMaster, T ownValue
	)
	{
		return new Impl<>(getCap, getMaster, owner, ownValue);
	}

	public abstract @Nullable T get();

	// Using inheritance to "hide" the extra type parameter from users
	private static final class Impl<T, BE extends BlockEntity> extends MultiblockCapability<T>
	{
		private final BE owner;
		private final T ownValue;
		private final Function<BE, MultiblockCapability<T>> getCap;
		private final Function<BE, BE> getMaster;
		private WeakReference<BE> cachedMaster = new WeakReference<>(null);

		public Impl(
				Function<BE, MultiblockCapability<T>> getCap, Function<BE, BE> getMaster,
				BE owner, T ownValue
		)
		{
			this.owner = owner;
			this.getCap = getCap;
			this.getMaster = getMaster;
			this.ownValue = ownValue;
		}

		@Override
		public @Nullable T get()
		{
			BE master = cachedMaster.get();
			if(master==null||master.isRemoved())
				master = getMaster.apply(owner);
			if(master==owner)
				return ownValue;
			else if(master!=null)
				return getCap.apply(master).get();
			else
				return null;
		}
	}
}
