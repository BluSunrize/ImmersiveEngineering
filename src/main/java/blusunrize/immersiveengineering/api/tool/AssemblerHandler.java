/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidUtil;

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
	private static final HashMap<Class<? extends IRecipe>, IRecipeAdapter> registry = new LinkedHashMap<Class<? extends IRecipe>, IRecipeAdapter>();
	private static final List<Function<Object, RecipeQuery>> specialQueryConverters = new ArrayList<>();

	public static void registerRecipeAdapter(Class<? extends IRecipe> recipeClass, IRecipeAdapter adapter)
	{
		registry.put(recipeClass, adapter);
	}

	public static IRecipeAdapter findAdapterForClass(Class<? extends IRecipe> recipeClass)
	{
		IRecipeAdapter adapter = registry.get(recipeClass);
		if(adapter==null&&recipeClass!=IRecipe.class&&recipeClass.getSuperclass()!=Object.class)
		{
			adapter = findAdapterForClass((Class<? extends IRecipe>)recipeClass.getSuperclass());
			registry.put(recipeClass, adapter);
		}
		return adapter;
	}

	public static IRecipeAdapter findAdapter(IRecipe recipe)
	{
		return findAdapterForClass(recipe.getClass());
	}

	public static void registerSpecialQueryConverters(Function<Object, RecipeQuery> func)
	{
		specialQueryConverters.add(func);
	}

	public interface IRecipeAdapter<R extends IRecipe>
	{
		RecipeQuery[] getQueriedInputs(R recipe);

		default RecipeQuery[] getQueriedInputs(R recipe, NonNullList<ItemStack> input)
		{
			return getQueriedInputs(recipe);
		}
	}

	public static RecipeQuery createQuery(Object o)
	{
		if(o==null)
			return null;
		for(Function<Object, RecipeQuery> func : specialQueryConverters)
		{
			RecipeQuery q = func.apply(o);
			if(q!=null)
				return q;
		}
		if(o instanceof ItemStack)
			return createQueryFromItemStack((ItemStack)o);
		else if(o instanceof Ingredient)
		{
			ItemStack[] stacks = ((Ingredient)o).getMatchingStacks();
			if(stacks.length <= 0)
				return null;
			if(stacks.length==1)
				return createQueryFromItemStack(stacks[0]);
			return new RecipeQuery(stacks, 1);
		}
		else if(o instanceof IngredientStack)
			return new RecipeQuery(o, ((IngredientStack)o).inputSize);
		return new RecipeQuery(o, 1);
	}

	public static RecipeQuery createQueryFromItemStack(ItemStack stack)
	{
		if(FluidUtil.getFluidContained(stack)!=null)
			return new RecipeQuery(FluidUtil.getFluidContained(stack), stack.getCount());
		return new RecipeQuery(stack, stack.getCount());
	}

	public static class RecipeQuery
	{
		public Object query;
		public int querySize;

		/**
		 * Valid types of Query are ItemStack, ItemStack[], ArrayList<ItemStack>, IngredientStack, String (OreDict Name) and FluidStack
		 */
		public RecipeQuery(Object query, int querySize)
		{
			this.query = query;
			this.querySize = querySize;
		}
	}
}
