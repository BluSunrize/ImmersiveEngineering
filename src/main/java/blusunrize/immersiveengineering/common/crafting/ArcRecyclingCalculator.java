/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.ArcRecyclingChecker;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.*;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArcRecyclingCalculator
{
	private static List<ArcFurnaceRecipe> lastResult;
	private final List<Recipe<?>> recipeList;
	private final long startTime;
	private final ArcRecyclingChecker checker;

	public ArcRecyclingCalculator(Collection<Recipe<?>> allRecipes)
	{
		this.startTime = System.currentTimeMillis();
		Pair<Predicate<Recipe<?>>, ArcRecyclingChecker> pair = ArcRecyclingChecker.assembleRecyclingFilter();
		this.checker = pair.getRight();
		this.recipeList = allRecipes.stream()
				.filter(pair.getLeft())
				.collect(Collectors.toList());
	}

	public void run()
	{
		RecipeIterator iterator = new RecipeIterator(recipeList, checker);
		iterator.process();
		int timeout = 0;
		while(!iterator.nonValidated.isEmpty()&&timeout++ < (iterator.invalidCount*10))
		{
			ArrayList<RecyclingCalculation> newlyValid = new ArrayList<>();
			for(RecyclingCalculation valid : iterator.validated)
				for(ItemStack key : iterator.nonValidated.keySet())
				{
					if(ItemStack.isSame(key, valid.stack))
						for(RecyclingCalculation nonValid : iterator.nonValidated.get(key))
							if(nonValid.validateSubcomponent(valid))
								newlyValid.add(nonValid);
				}
			// No new ingredients, so further iterations are useless
			if(newlyValid.isEmpty())
				break;
			iterator.nonValidated.values().removeAll(newlyValid);
			iterator.validated.addAll(newlyValid);
		}
		List<ArcFurnaceRecipe> generatedRecipes = new ArrayList<>();
		Set<String> finishedRecycles = new HashSet<>();
		for(RecyclingCalculation valid : iterator.validated)
			if(finishedRecycles.add(valid.stack.toString())&&!valid.outputs.isEmpty())
				generatedRecipes.add(makeRecipe(valid));
		for(RecyclingCalculation invalid : Sets.newHashSet(iterator.nonValidated.values()))
			if(finishedRecycles.add(invalid.stack.toString())&&!invalid.outputs.isEmpty())
			{
				IELogger.info("Couldn't fully analyze "+invalid.stack+", missing knowledge for "+invalid.queriedSubcomponents);
				generatedRecipes.add(makeRecipe(invalid));
			}
		ArcRecyclingCalculator.lastResult = generatedRecipes;
		IELogger.info("Finished recipe profiler for Arc Recycling, took "
				+(System.currentTimeMillis()-startTime)+" milliseconds");
	}

	private ArcRecyclingRecipe makeRecipe(RecyclingCalculation calculation)
	{
		ResourceLocation id = new ResourceLocation(Lib.MODID, "recycling/"+ForgeRegistries.ITEMS.getKey(calculation.stack.getItem()).getPath());
		return new ArcRecyclingRecipe(id, calculation.outputs, IngredientWithSize.of(calculation.stack), 100, 51200);
	}

	public static List<ArcFurnaceRecipe> getRecipesFromRunningThreads()
	{
		return Objects.requireNonNull(lastResult);
	}

	private static class RecipeIterator
	{
		final List<Recipe<?>> recipeList;
		final List<RecyclingCalculation> validated = new ArrayList<>();
		final Multimap<ItemStack, RecyclingCalculation> nonValidated = ArrayListMultimap.create();
		private final ArcRecyclingChecker checker;
		int invalidCount = 0;

		public RecipeIterator(List<Recipe<?>> recipeList, ArcRecyclingChecker checker)
		{
			this.recipeList = recipeList;
			this.checker = checker;
		}

		public void process()
		{
			for(Recipe<?> recipe : recipeList)
			{
				RecyclingCalculation calc = getRecycleCalculation(recipe.getResultItem(), recipe);
				if(calc!=null)
				{
					if(calc.isValid())
						validated.add(calc);
					else
					{
						for(ItemStack s : calc.queriedSubcomponents.keySet())
							nonValidated.put(s, calc);
						invalidCount++;
					}
				}
			}
		}

		private RecyclingCalculation getRecycleCalculation(ItemStack stack, Recipe<?> recipe)
		{
			// Check if recipe output is among the items that have fixed returns
			Pair<ItemStack, Double> brokenDown = ApiUtils.breakStackIntoPreciseIngots(stack);
			if(brokenDown!=null&&ArcRecyclingChecker.isValidRecyclingOutput(brokenDown.getLeft())&&brokenDown.getRight() > 0)
				return new RecyclingCalculation(recipe, ItemHandlerHelper.copyStackWithSize(stack, 1),
						ImmutableMap.of(brokenDown.getLeft(), brokenDown.getRight()));

			// Else check recipe inputs
			NonNullList<Ingredient> inputs = recipe.getIngredients();
			if(!inputs.isEmpty())
			{
				int resultCount = stack.getCount();
				Map<ItemStack, Integer> missingSub = new HashMap<>();
				Map<ItemStack, Double> outputs = new IdentityHashMap<>();
				for(Ingredient in : inputs)
					if(in!=null&&in!=Ingredient.EMPTY)
					{
						ItemStack[] matchingStacks = in.getItems();
						ItemStack inputStack = ItemStack.EMPTY;
						if(matchingStacks.length>0)
							inputStack = IEApi.getPreferredStackbyMod(in.getItems());
						if(inputStack.isEmpty())
						{
							IELogger.warn("Recipe has invalid inputs and will be ignored: "+recipe+" ("+recipe.getId()+")");
							return null;
						}
						brokenDown = ApiUtils.breakStackIntoPreciseIngots(inputStack);
						if(brokenDown==null)
						{
							if(checker.isAllowed(inputStack)&&ArcRecyclingChecker.isValidRecyclingOutput(inputStack))
							{
								boolean b = false;
								for(ItemStack storedMiss : missingSub.keySet())
									if(ItemStack.isSame(inputStack, storedMiss))
									{
										missingSub.put(storedMiss, missingSub.get(storedMiss)+inputStack.getCount());
										b = true;
									}
								if(!b)
									missingSub.put(ItemHandlerHelper.copyStackWithSize(inputStack, 1), inputStack.getCount());
							}
							continue;
						}
						if(!brokenDown.getLeft().isEmpty()&&brokenDown.getRight() > 0)
						{
							boolean invalidOutput = !ArcRecyclingChecker.isValidRecyclingOutput(brokenDown.getLeft());
							if(!invalidOutput)
							{
								boolean b = false;
								for(ItemStack storedOut : outputs.keySet())
									if(ItemStack.isSame(brokenDown.getLeft(), storedOut))
									{
										outputs.put(storedOut, outputs.get(storedOut)+brokenDown.getRight());
										b = true;
									}
								if(!b)
									outputs.put(ItemHandlerHelper.copyStackWithSize(brokenDown.getLeft(), 1), brokenDown.getRight());
							}
						}
					}
				Map<ItemStack, Double> outputScaled = new IdentityHashMap<>(outputs.size());
				for(Entry<ItemStack, Double> e : outputs.entrySet())
					outputScaled.put(e.getKey(), e.getValue()/resultCount);
				if(!outputs.isEmpty()||!missingSub.isEmpty())
				{
					ItemStack in = ItemHandlerHelper.copyStackWithSize(stack, 1);
					RecyclingCalculation calc = new RecyclingCalculation(recipe, in
							, outputScaled);
					if(!missingSub.isEmpty())
						for(ItemStack s : missingSub.keySet())
							calc.queriedSubcomponents.put(s, (double)missingSub.get(s)/resultCount);
					return calc;
				}
			}
			return null;
		}
	}

	private static class RecyclingCalculation
	{
		Recipe<?> recipe;
		ItemStack stack;
		Map<ItemStack, Double> outputs;
		Map<ItemStack, Double> queriedSubcomponents = new HashMap<>();

		public RecyclingCalculation(Recipe<?> recipe, ItemStack stack, Map<ItemStack, Double> outputs)
		{
			this.recipe = recipe;
			this.stack = stack;
			this.outputs = outputs;
		}

		public boolean isValid()
		{
			return !outputs.isEmpty()&&queriedSubcomponents.isEmpty();
		}

		public boolean validateSubcomponent(RecyclingCalculation calc)
		{
			if(isValid())
				return true;
			if(!calc.isValid())
				return false;
			Iterator<ItemStack> it = queriedSubcomponents.keySet().iterator();
			while(it.hasNext())
			{
				ItemStack next = it.next();
				if(ItemStack.isSame(next, calc.stack))
				{
					double queriedAmount = queriedSubcomponents.get(next);
					for(Map.Entry<ItemStack, Double> e : calc.outputs.entrySet())
					{
						double scaledVal = e.getValue()*queriedAmount;
						boolean b = true;
						for(ItemStack key : outputs.keySet())
							if(ItemStack.isSame(key, e.getKey()))
							{
								outputs.put(key, outputs.get(key)+scaledVal);
								b = false;
								break;
							}
						if(b)
							outputs.put(e.getKey(), scaledVal);
					}
					it.remove();
				}
			}
			return isValid();
		}
	}

}