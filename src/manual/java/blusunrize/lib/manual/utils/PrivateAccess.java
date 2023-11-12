/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.util.Lazy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;

public class PrivateAccess
{
	private static final Lazy<MethodHandles.Lookup> LOOKUP = Lazy.of(MethodHandles::lookup);
	private static final Lazy<MethodHandle> GET_RECIPES_OF_TYPE = Lazy.of(() -> {
		try
		{
			Method reflectionMethod = RecipeManager.class.getDeclaredMethod("byType", RecipeType.class);
			reflectionMethod.setAccessible(true);
			return LOOKUP.get().unreflect(reflectionMethod);
		} catch(IllegalAccessException|NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	});

	@SuppressWarnings("unchecked")
	public static <C extends Container, T extends Recipe<C>>
	Map<ResourceLocation, RecipeHolder<T>> getRecipes(RecipeManager manager, RecipeType<T> recipeTypeIn)
	{
		try
		{
			return (Map<ResourceLocation, RecipeHolder<T>>)GET_RECIPES_OF_TYPE.get().invokeExact(manager, recipeTypeIn);
		} catch(Throwable throwable)
		{
			throw new RuntimeException(throwable);
		}
	}
}
