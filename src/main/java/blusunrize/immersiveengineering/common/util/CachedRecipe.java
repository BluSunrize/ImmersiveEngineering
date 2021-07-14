/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import com.mojang.datafixers.util.Function3;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CachedRecipe
{
	public static <K, R>
	Supplier<R> cached(BiFunction<K, R, R> getRecipeWithHint, Supplier<K> get) {
		Function<K, R> cached = cached(getRecipeWithHint);
		return () -> cached.apply(get.get());
	}

	public static <K, R>
	Function<K, R> cached(BiFunction<K, R, R> getRecipeWithHint) {
		Mutable<R> cached = new MutableObject<>();
		return k -> {
			R result = getRecipeWithHint.apply(k, cached.getValue());
			cached.setValue(result);
			return result;
		};
	}

	public static <K1, K2, R>
	Supplier<R> cached(Function3<K1, K2, R, R> getRecipeWithHint, Supplier<K1> get1, Supplier<K2> get2) {
		BiFunction<K1, K2, R> cached = cached(getRecipeWithHint);
		return () -> cached.apply(get1.get(), get2.get());
	}

	public static <K1, K2, R>
	BiFunction<K1, K2, R> cached(Function3<K1, K2, R, R> getRecipeWithHint) {
		Mutable<R> cached = new MutableObject<>();
		return (k1, k2) -> {
			R result = getRecipeWithHint.apply(k1, k2, cached.getValue());
			cached.setValue(result);
			return result;
		};
	}
}
