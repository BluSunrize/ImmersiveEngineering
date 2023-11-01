/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import net.neoforged.neoforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;

public final class ResettableCapability<T>
{
	private final T containedValue;
	private final List<Runnable> onReset = new ArrayList<>();
	private LazyOptional<T> currentOptional = LazyOptional.empty();

	public ResettableCapability(T containedValue)
	{
		this.containedValue = containedValue;
	}

	public LazyOptional<T> getLO()
	{
		if(!currentOptional.isPresent())
			currentOptional = CapabilityUtils.constantOptional(containedValue);
		return currentOptional;
	}

	public T get()
	{
		return containedValue;
	}

	public <A> LazyOptional<A> cast()
	{
		return getLO().cast();
	}

	public void reset()
	{
		currentOptional.invalidate();
		this.onReset.forEach(Runnable::run);
	}

	public void addResetListener(Runnable onReset)
	{
		this.onReset.add(onReset);
	}
}
