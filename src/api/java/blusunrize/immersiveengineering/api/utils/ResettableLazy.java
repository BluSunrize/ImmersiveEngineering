/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ResettableLazy<T> implements Supplier<T>
{
	private final Supplier<T> getter;
	private final Consumer<T> destructor;
	@Nullable
	private T cached;

	public ResettableLazy(Supplier<T> getter)
	{
		this(getter, v -> {
		});
	}

	public ResettableLazy(Supplier<T> getter, Consumer<T> destructor)
	{
		this.getter = getter;
		this.destructor = destructor;
	}

	@Nonnull
	public T get()
	{
		if(cached==null)
			cached = getter.get();
		return cached;
	}

	public void reset()
	{
		if(cached!=null)
		{
			destructor.accept(cached);
			cached = null;
		}
	}
}
