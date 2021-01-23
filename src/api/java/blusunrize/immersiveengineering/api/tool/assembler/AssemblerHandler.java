/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.assembler;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * @author BluSunrize - 01.09.2016
 */
public class AssemblerHandler
{
	private static final HashMap<Class<? extends IRecipe>, IRecipeAdapter> registry = new LinkedHashMap<>();
	private static final List<Function<Object, RecipeQuery>> specialQueryConverters = new ArrayList<>();
	private static final List<Function<Ingredient, RecipeQuery>> specialIngredientConverters = new ArrayList<>();
	private static final List<Function<ItemStack, RecipeQuery>> specialItemStackConverters = new ArrayList<>();
	public static IRecipeAdapter<IRecipe<CraftingInventory>> defaultAdapter;

	public static void registerRecipeAdapter(Class<? extends IRecipe> recipeClass, IRecipeAdapter adapter)
	{
		registry.put(recipeClass, adapter);
	}

	@Nonnull
	public static IRecipeAdapter<?> findAdapterForClass(Class<? extends IRecipe> recipeClass)
	{
		IRecipeAdapter adapter = registry.get(recipeClass);
		boolean isSuperIRecipe = IRecipe.class.isAssignableFrom(recipeClass.getSuperclass());
		if(adapter==null&&recipeClass!=IRecipe.class&&isSuperIRecipe)
			adapter = findAdapterForClass((Class<? extends IRecipe>)recipeClass.getSuperclass());
		else
			adapter = defaultAdapter;
		registry.put(recipeClass, adapter);
		return adapter;
	}

	@Nonnull
	public static IRecipeAdapter<?> findAdapter(IRecipe recipe)
	{
		return findAdapterForClass(recipe.getClass());
	}

	public static void registerSpecialIngredientConverter(Function<Ingredient, RecipeQuery> func)
	{
		specialIngredientConverters.add(func);
	}

	public static void registerSpecialItemStackConverter(Function<ItemStack, RecipeQuery> func)
	{
		specialItemStackConverters.add(func);
	}

	public interface IRecipeAdapter<R extends IRecipe<CraftingInventory>>
	{
		@Nullable
		RecipeQuery[] getQueriedInputs(R recipe, NonNullList<ItemStack> input, World world);
	}

	private static <T> RecipeQuery fromFunctions(T in, List<Function<T, RecipeQuery>> converters)
	{
		for(Function<T, RecipeQuery> func : converters)
		{
			RecipeQuery q = func.apply(in);
			if(q!=null)
				return q;
		}
		return null;
	}

	@Nullable
	public static RecipeQuery createQueryFromIngredient(Ingredient ingr)
	{
		RecipeQuery special = fromFunctions(ingr, specialIngredientConverters);
		if(special==null)
			special = fromFunctions(ingr, specialQueryConverters);
		if(special!=null)
			return special;
		if(ingr.hasNoMatchingItems())
			return null;
		else
			return new IngredientRecipeQuery(ingr, 1);
	}

	@Nullable
	public static RecipeQuery createQueryFromItemStack(ItemStack stack)
	{
		if(stack.isEmpty())
			return null;
		RecipeQuery special = fromFunctions(stack, specialItemStackConverters);
		if(special==null)
			special = fromFunctions(stack, specialQueryConverters);
		if(special!=null)
			return special;
		FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
		if(!fluidStack.isEmpty())
			return new FluidStackRecipeQuery(fluidStack);
		return new ItemStackRecipeQuery(stack);
	}
}
