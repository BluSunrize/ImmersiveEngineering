/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.assembler;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author BluSunrize - 01.09.2016
 */
public class AssemblerHandler
{
	private static final HashMap<Class<? extends Recipe>, IRecipeAdapter> registry = new LinkedHashMap<>();
	private static final List<BiFunction<Ingredient, ItemStack, RecipeQuery>> specialIngredientConverters = new ArrayList<>();
	private static final List<BiFunction<ItemStack, ItemStack, RecipeQuery>> specialItemStackConverters = new ArrayList<>();
	public static IRecipeAdapter<Recipe<CraftingContainer>> defaultAdapter;

	public static void registerRecipeAdapter(Class<? extends Recipe> recipeClass, IRecipeAdapter adapter)
	{
		registry.put(recipeClass, adapter);
	}

	@Nonnull
	public static IRecipeAdapter<?> findAdapterForClass(Class<? extends Recipe> recipeClass)
	{
		IRecipeAdapter adapter = registry.get(recipeClass);
		boolean isSuperIRecipe = Recipe.class.isAssignableFrom(recipeClass.getSuperclass());
		if(adapter==null&&recipeClass!=Recipe.class&&isSuperIRecipe)
			adapter = findAdapterForClass((Class<? extends Recipe>)recipeClass.getSuperclass());
		else
			adapter = defaultAdapter;
		registry.put(recipeClass, adapter);
		return adapter;
	}

	@Nonnull
	public static <R extends Recipe<CraftingContainer>>
	IRecipeAdapter<R> findAdapter(R recipe)
	{
		return (IRecipeAdapter<R>)findAdapterForClass(recipe.getClass());
	}

	public static void registerSpecialIngredientConverter(BiFunction<Ingredient, ItemStack, RecipeQuery> func)
	{
		specialIngredientConverters.add(func);
	}

	public static void registerSpecialItemStackConverter(BiFunction<ItemStack, ItemStack, RecipeQuery> func)
	{
		specialItemStackConverters.add(func);
	}

	public interface IRecipeAdapter<R extends Recipe<CraftingContainer>>
	{
		@Nullable
		List<RecipeQuery> getQueriedInputs(R recipe, NonNullList<ItemStack> input, Level world);
	}

	private static <T> RecipeQuery fromFunctions(T in, ItemStack remaining, List<BiFunction<T, ItemStack, RecipeQuery>> converters)
	{
		for(BiFunction<T, ItemStack, RecipeQuery> func : converters)
		{
			RecipeQuery q = func.apply(in, remaining);
			if(q!=null)
				return q;
		}
		return null;
	}

	@Nullable
	public static RecipeQuery createQueryFromIngredient(Ingredient ingr, ItemStack remaining)
	{
		RecipeQuery special = fromFunctions(ingr, remaining, specialIngredientConverters);
		if(special!=null)
			return special;
		if(ingr.isEmpty())
			return null;
		else
			return new IngredientRecipeQuery(ingr, 1);
	}

	@Nullable
	public static RecipeQuery createQueryFromItemStack(ItemStack stack, ItemStack remaining)
	{
		if(stack.isEmpty())
			return null;
		RecipeQuery special = fromFunctions(stack, remaining, specialItemStackConverters);
		if(special!=null)
			return special;
		FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
		if(!fluidStack.isEmpty())
			return new FluidStackRecipeQuery(fluidStack);
		return new ItemStackRecipeQuery(stack);
	}
}
