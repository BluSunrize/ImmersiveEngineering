/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import com.google.common.base.Preconditions;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
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

	@Deprecated
	public static void registerSpecialQueryConverters(Function<Object, RecipeQuery> func)
	{
		specialQueryConverters.add(func);
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
		@Deprecated
		//TODO remove
		default RecipeQuery[] getQueriedInputs(R recipe, World world)
		{
			return getQueriedInputs(recipe, NonNullList.create(), world);
		}

		@Nullable
		default RecipeQuery[] getQueriedInputs(R recipe, NonNullList<ItemStack> input, World world)
		{
			return getQueriedInputs(recipe, world);
		}
	}

	@Deprecated
	@Nullable
	public static RecipeQuery createQuery(Object o)
	{
		if(o==null)
			return null;
		RecipeQuery special = fromFunctions(o, specialQueryConverters);
		if(special!=null)
			return special;
		if(o instanceof ItemStack)
			return createQueryFromItemStack((ItemStack)o);
		else if(o instanceof Ingredient)
			createQueryFromIngredient((Ingredient)o);
		return RecipeQuery.create(o, 1);
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
			return RecipeQuery.create(ingr, 1);
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
			return RecipeQuery.create(fluidStack, stack.getCount());
		return RecipeQuery.create(stack, stack.getCount());
	}

	public static class RecipeQuery
	{
		@Deprecated
		@Nonnull
		//Do not access from outside! Will be removed in later release!
		public Object query;
		public int querySize;

		/**
		 * Valid types of Query are ItemStack, ItemStack[], ArrayList<ItemStack>, IngredientWithSize,
		 * ResourceLocation (Tag Name), FluidStack or FluidTagInput
		 * Deprecated: Use create instead, this constructor will be removed in a later release!
		 */
		@Deprecated
		public RecipeQuery(@Nonnull Object query, int querySize)
		{
			Preconditions.checkArgument(
					query instanceof ItemStack||
							query instanceof ItemStack[]||
							query instanceof List||
							query instanceof IngredientWithSize||
							query instanceof Ingredient||
							query instanceof ResourceLocation||
							query instanceof FluidStack||
							query instanceof FluidTagInput,
					query+" is not a valid ingredient!"
			);
			this.query = query;
			this.querySize = querySize;
		}

		public static RecipeQuery create(@Nonnull Object query, int size)
		{
			return new RecipeQuery(query, size);
		}

		public boolean matchesIgnoringSize(ItemStack stack)
		{
			return ItemUtils.stackMatchesObject(stack, query, true);
		}

		public boolean matchesFluid(FluidStack fluid)
		{
			if(query instanceof FluidStack)
				return fluid.containsFluid((FluidStack)query);
			else if(query instanceof FluidTagInput)
				return ((FluidTagInput)query).test(fluid);
			else
				return false;
		}

		public int getFluidSize()
		{
			if(query instanceof FluidStack)
				return ((FluidStack)query).getAmount();
			else if(query instanceof FluidTagInput)
				return ((FluidTagInput)query).getAmount();
			else
				throw new UnsupportedOperationException("Query "+query+" (class "+query.getClass()+") is not a fluid query");
		}

		public boolean isFluid()
		{
			return query instanceof FluidStack||query instanceof FluidTagInput;
		}
	}
}
