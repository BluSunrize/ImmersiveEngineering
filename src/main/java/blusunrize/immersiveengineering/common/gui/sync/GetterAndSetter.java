/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui.sync;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record GetterAndSetter<T>(Supplier<T> getter, Consumer<T> setter) implements Supplier<T>, Consumer<T>
{
	public static <T> GetterAndSetter<T> standalone(T initial)
	{
		Mutable<T> box = new MutableObject<>(initial);
		return new GetterAndSetter<>(box::getValue, box::setValue);
	}

	public static <T> GetterAndSetter<T> getterOnly(Supplier<T> getter)
	{
		return new GetterAndSetter<>(getter, $ -> {
		});
	}

	public static <T> List<GetterAndSetter<T>> forArray(T[] data)
	{
		List<GetterAndSetter<T>> result = new ArrayList<>(data.length);
		for(int i = 0; i < data.length; ++i)
		{
			int finalI = i;
			result.add(new GetterAndSetter<>(() -> data[finalI], o -> data[finalI] = o));
		}
		return result;
	}

	public static <T>
	GetterAndSetter<T> constant(T value)
	{
		return getterOnly(() -> value);
	}

	public void set(T newValue)
	{
		setter.accept(newValue);
	}

	public T get()
	{
		return getter.get();
	}

	@Override
	public void accept(T t)
	{
		set(t);
	}
}
