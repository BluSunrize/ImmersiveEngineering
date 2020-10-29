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
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import com.google.common.base.Preconditions;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author BluSunrize - 01.09.2016
 */
public class AssemblerHandler
{
	private static final HashMap<Class<? extends IRecipe>, IRecipeAdapter> registry = new LinkedHashMap<>();
	private static final List<Function<Object, RecipeQuery>> specialQueryConverters = new ArrayList<>();
	private static final List<Function<Ingredient, RecipeQuery>> specialIngredientConverters = new ArrayList<>();
	private static final List<Function<ItemStack, RecipeQuery>> specialItemStackConverters = new ArrayList<>();
	public static final IRecipeAdapter<IRecipe<CraftingInventory>> defaultAdapter = new IRecipeAdapter<IRecipe<CraftingInventory>>()
	{
		@Override
		public RecipeQuery[] getQueriedInputs(IRecipe<CraftingInventory> recipe, NonNullList<ItemStack> input, World world)
		{
			NonNullList<Ingredient> ingred = recipe.getIngredients();
			// Check that the ingredients roughly match what the recipe actually requires.
			// This is necessary to prevent infinite crafting for recipes like FireworkRocketRecipe which don't return
			// meaningful values in getIngredients.
			NonNullList<Predicate<ItemStack>> ingredientsForMatching = NonNullList.create();
			List<ItemStack> inputList = input.subList(0, input.size()-1);
			for(Ingredient i : ingred)
				if(!i.hasNoMatchingItems())
					ingredientsForMatching.add(i);
			final int numNonEmpty = ingredientsForMatching.size();
			while(ingredientsForMatching.size() < inputList.size())
				ingredientsForMatching.add(ItemStack::isEmpty);
			ForgeHooks.setCraftingPlayer(FakePlayerUtil.getFakePlayer(world));
			int[] ingredientAssignment = RecipeMatcher.findMatches(inputList, ingredientsForMatching);
			ForgeHooks.setCraftingPlayer(null);

			// - 1: Input list contains the output slot
			RecipeQuery[] query = new RecipeQuery[input.size()-1];
			if(ingredientAssignment!=null)
				// If the ingredients provided by the recipe are plausible request those
				// Try to request each ingredient at the index where it is in the input pattern, this is needed for
				// some CraftTweaker recipes
				for(int stackIndex = 0; stackIndex < ingredientAssignment.length; stackIndex++)
				{
					int ingredIndex = ingredientAssignment[stackIndex];
					if(ingredIndex < numNonEmpty)
						query[stackIndex] = AssemblerHandler.createQueryFromIngredient(
								(Ingredient)ingredientsForMatching.get(ingredIndex)
						);
				}
			else
				// Otherwise request the exact stacks used in the input
				for(int i = 0; i < query.length; i++)
					if(!input.get(i).isEmpty())
						query[i] = AssemblerHandler.createQueryFromItemStack(input.get(i));
			return query;
		}
	};

	static
	{
		AssemblerHandler.registerRecipeAdapter(IRecipe.class, defaultAdapter);
	}

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
