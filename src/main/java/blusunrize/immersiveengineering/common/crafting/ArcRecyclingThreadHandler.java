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
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ArcRecyclingThreadHandler extends Thread
{
	private static ArcRecyclingThreadHandler runningHandler;
	private ArrayList<RecyclingCalculation> validated;
	private ArrayListMultimap<ItemStack, RecyclingCalculation> nonValidated;
	private final List<IRecipe<?>> recipeList;

	public ArcRecyclingThreadHandler(Collection<IRecipe<?>> allRecipes)
	{
		this.recipeList = allRecipes.stream()
				.filter(ArcFurnaceRecipe.assembleRecyclingFilter())
				.collect(Collectors.toList());
		runningHandler = this;
	}

	@Override
	public void run()
	{
		//Keep one core free for normal Forge lifecycle events
		int threadAmount = Math.max(Runtime.getRuntime().availableProcessors()-1, 1);
		RegistryIterationThread[] threads = new RegistryIterationThread[threadAmount];

		long timestamp = System.currentTimeMillis();

		boolean divisable = recipeList.size()%threadAmount==0;
		int limit = divisable?(recipeList.size()/threadAmount): (recipeList.size()/(threadAmount-1));
		int leftOver = divisable?limit: (recipeList.size()-(threadAmount-1)*limit);
		for(int i = 0; i < threadAmount; i++)
			threads[i] = new RegistryIterationThread(recipeList, limit*i, i==(threadAmount-1)?leftOver: limit);

		//iterate over each thread individually
		validated = new ArrayList<>();
		nonValidated = ArrayListMultimap.create();
		int invalidCount = 0;

		for(RegistryIterationThread thread : threads)
		{
			try
			{
				thread.join();
				for(RecyclingCalculation calc : thread.calculatedOutputs)
					if(calc.isValid())
						validated.add(calc);
					else
					{
						for(ItemStack s : calc.queriedSubcomponents.keySet())
							nonValidated.put(s, calc);
						invalidCount++;
					}
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		int timeout = 0;
		while(!nonValidated.isEmpty()&&timeout++ < (invalidCount*10))
		{
			ArrayList<RecyclingCalculation> newlyValid = new ArrayList<>();
			for(RecyclingCalculation valid : validated)
			{
				for(ItemStack key : nonValidated.keySet())
				{
					if(ItemStack.areItemsEqual(key, valid.stack))
						for(RecyclingCalculation nonValid : nonValidated.get(key))
							if(nonValid.validateSubcomponent(valid))
								newlyValid.add(nonValid);
				}
			}
			// No new ingredients, so further iterations are useless
			if(newlyValid.isEmpty())
				break;
			nonValidated.values().removeAll(newlyValid);
			validated.addAll(newlyValid);
		}
		IELogger.info("Finished recipe profiler for Arc Recycling, took "
				+(System.currentTimeMillis()-timestamp)+" milliseconds");
	}

	private ArcRecyclingRecipe makeRecipe(RecyclingCalculation calculation)
	{
		ResourceLocation id = new ResourceLocation(Lib.MODID, "recycling/"+ForgeRegistries.ITEMS.getKey(calculation.stack.getItem()).getPath());
		return new ArcRecyclingRecipe(id, calculation.outputs, IngredientWithSize.of(calculation.stack), 100, 51200);
	}

	public static List<ArcFurnaceRecipe> getRecipesFromRunningThreads()
	{
		ArcRecyclingThreadHandler handler = Preconditions.checkNotNull(runningHandler);
		try
		{
			handler.join();
			//HashSet to avoid duplicates
			HashSet<String> finishedRecycles = new HashSet<>();
			List<ArcFurnaceRecipe> generatedRecipes = new ArrayList<>();
			for(RecyclingCalculation valid : handler.validated)
				if(finishedRecycles.add(valid.stack.toString())&&!valid.outputs.isEmpty())
					generatedRecipes.add(handler.makeRecipe(valid));
			for(RecyclingCalculation invalid : Sets.newHashSet(handler.nonValidated.values()))
				if(finishedRecycles.add(invalid.stack.toString())&&!invalid.outputs.isEmpty())
				{
					IELogger.info("Couldn't fully analyze "+invalid.stack+", missing knowledge for "+invalid.queriedSubcomponents);
					generatedRecipes.add(handler.makeRecipe(invalid));
				}
			return generatedRecipes;
		} catch(InterruptedException x)
		{
			return ImmutableList.of();
		}
	}

	public static class RegistryIterationThread extends Thread
	{
		final List<IRecipe<?>> recipeList;
		final int baseOffset;
		final int passes;
		ArrayList<RecyclingCalculation> calculatedOutputs = new ArrayList<>();

		public RegistryIterationThread(List<IRecipe<?>> recipeList, int baseOffset, int passes)
		{
			setName("Immersive Engineering Registry Iteration Thread");
			setDaemon(true);
			start();
			this.recipeList = recipeList;
			this.baseOffset = baseOffset;
			this.passes = passes;
		}

		@Override
		public void run()
		{
			for(int pass = 0; pass < passes; pass++)
			{
				IRecipe<?> recipe = recipeList.get(baseOffset+pass);
				RecyclingCalculation calc = getRecycleCalculation(recipe.getRecipeOutput(), recipe);
				if(calc!=null)
					calculatedOutputs.add(calc);
			}
		}
	}

	public static RecyclingCalculation getRecycleCalculation(ItemStack stack, IRecipe<?> recipe)
	{
		// Check if recipe output is among the items that have fixed returns
		Pair<ItemStack, Double> brokenDown = ApiUtils.breakStackIntoPreciseIngots(stack);
		if(brokenDown!=null&&ArcFurnaceRecipe.isValidRecyclingOutput(brokenDown.getLeft())&&brokenDown.getRight() > 0)
			return new RecyclingCalculation(recipe, Utils.copyStackWithAmount(stack, 1),
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
					ItemStack inputStack = IEApi.getPreferredStackbyMod(in.getMatchingStacks());
					if(inputStack.isEmpty())
					{
						IELogger.warn("Recipe has invalid inputs and will be ignored: "+recipe+" ("+recipe.getId()+")");
						return null;
					}
					brokenDown = ApiUtils.breakStackIntoPreciseIngots(inputStack);
					if(brokenDown==null)
					{
						if(ArcFurnaceRecipe.canRecycle(inputStack)&&ArcFurnaceRecipe.isValidRecyclingOutput(inputStack))
						{
							boolean b = false;
							for(ItemStack storedMiss : missingSub.keySet())
								if(ItemStack.areItemsEqual(inputStack, storedMiss))
								{
									missingSub.put(storedMiss, missingSub.get(storedMiss)+inputStack.getCount());
									b = true;
								}
							if(!b)
								missingSub.put(Utils.copyStackWithAmount(inputStack, 1), inputStack.getCount());
						}
						continue;
					}
					if(!brokenDown.getLeft().isEmpty()&&brokenDown.getRight() > 0)
					{
						boolean invalidOutput = !ArcFurnaceRecipe.isValidRecyclingOutput(brokenDown.getLeft());
						if(!invalidOutput)
						{
							boolean b = false;
							for(ItemStack storedOut : outputs.keySet())
								if(ItemStack.areItemsEqual(brokenDown.getLeft(), storedOut))
								{
									outputs.put(storedOut, outputs.get(storedOut)+brokenDown.getRight());
									b = true;
								}
							if(!b)
								outputs.put(Utils.copyStackWithAmount(brokenDown.getLeft(), 1), brokenDown.getRight());
						}
					}
				}
			Map<ItemStack, Double> outputScaled = new IdentityHashMap<>(outputs.size());
			for(Entry<ItemStack, Double> e : outputs.entrySet())
				outputScaled.put(e.getKey(), e.getValue()/resultCount);
			if(!outputs.isEmpty()||!missingSub.isEmpty())
			{
				ItemStack in = Utils.copyStackWithAmount(stack, 1);
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

	public static class RecyclingCalculation
	{
		IRecipe<?> recipe;
		ItemStack stack;
		Map<ItemStack, Double> outputs;
		Map<ItemStack, Double> queriedSubcomponents = new HashMap<>();

		public RecyclingCalculation(IRecipe<?> recipe, ItemStack stack, Map<ItemStack, Double> outputs)
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
				if(ItemStack.areItemsEqual(next, calc.stack))
				{
					double queriedAmount = queriedSubcomponents.get(next);
					for(Map.Entry<ItemStack, Double> e : calc.outputs.entrySet())
					{
						double scaledVal = e.getValue()*queriedAmount;
						boolean b = true;
						for(ItemStack key : outputs.keySet())
							if(ItemStack.areItemsEqual(key, e.getKey()))
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