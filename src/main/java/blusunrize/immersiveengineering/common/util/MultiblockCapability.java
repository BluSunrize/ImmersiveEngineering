/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;

public class MultiblockCapability<BE extends IEBaseBlockEntity, T>
{
	private final BE owner;
	private final Function<BE, MultiblockCapability<?, T>> getCap;
	private final Function<BE, BE> getMaster;
	private final LazyOptional<T> ownValue;
	private LazyOptional<T> cached = LazyOptional.empty();

	public MultiblockCapability(
			Function<BE, MultiblockCapability<?, T>> getCap, Function<BE, BE> getMaster,
			BE owner, LazyOptional<T> ownValue
	)
	{
		this.owner = owner;
		this.getCap = getCap;
		this.getMaster = getMaster;
		this.ownValue = ownValue;
	}

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

	public <T2> LazyOptional<T2> getAndCast()
	{
		return get().cast();
	}
}
