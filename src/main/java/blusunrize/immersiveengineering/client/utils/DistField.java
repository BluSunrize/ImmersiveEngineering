/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeCallable;

import java.util.function.Supplier;

/**
 * Safe wrapper around fields of a client-only type (or a type using client-only things)
 */
public class DistField<T>
{
	private final T content;

	public DistField(Dist correctDist, Supplier<SafeCallable<T>> content)
	{
		this.content = DistExecutor.safeCallWhenOn(correctDist, content);
	}

	public static <T> DistField<T> client(Supplier<SafeCallable<T>> content)
	{
		return new DistField<>(Dist.CLIENT, content);
	}

	public T get()
	{
		return content;
	}
}
