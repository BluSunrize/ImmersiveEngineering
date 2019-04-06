/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Objects;

public class CapabilityHolder<T>
{
	public static <T> CapabilityHolder<T> empty()
	{
		return new CapabilityHolder<>(null);
	}

	private LazyOptional<T> value;

	private CapabilityHolder(LazyOptional<T> val)
	{
		value = val;
	}

	public boolean isPresent()
	{
		return false;
	}

	public LazyOptional<T> get()
	{
		return value;
	}

	public LazyOptional<T> replaceIfAbsent(LazyOptional<T> source)
	{
		if(!isPresent())
			value = source;
		return value;
	}

	public void reset()
	{
		if(isPresent())
		{
			value.invalidate();
			value = null;
		}
	}

	@Nullable
	public LazyOptional<T> orNull()
	{
		return value;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		CapabilityHolder<?> that = (CapabilityHolder<?>)o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(value);
	}

	@Override
	public String toString()
	{
		return Objects.toString(value);
	}
}
