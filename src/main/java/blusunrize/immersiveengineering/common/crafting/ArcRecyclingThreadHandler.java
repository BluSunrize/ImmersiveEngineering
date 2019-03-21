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
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.Map.Entry;

public class ArcRecyclingThreadHandler extends Thread
{
	static boolean hasProfiled = false;
	private ArrayList<RecyclingCalculation> validated;
	private ArrayListMultimap<ItemStack, RecyclingCalculation> nonValidated;
	private List<IRecipe> recipeList = new ArrayList<>(ForgeRegistries.RECIPES.getValuesCollection());

	@Override
	public void run()
	{
		Iterator<ArcFurnaceRecipe> prevRecipeIt = ArcFurnaceRecipe.recipeList.iterator();
		int r = 0;
		if(hasProfiled)
			while(prevRecipeIt.hasNext())
			{
				ArcFurnaceRecipe recipe = prevRecipeIt.next();
				if("Recycling".equals(recipe.specialRecipeType))
				{
					prevRecipeIt.remove();
					r++;
				}
			}
		IELogger.info("Arc Recycling: Removed "+r+" old recipes");

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

		for(int i = 0; i < threads.length; i++)
		{
			RegistryIterationThread thread = threads[i];
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
					if(OreDictionary.itemMatches(key, valid.stack, false))
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

	public void finishUp()
	{
		//HashSet to avoid duplicates
		HashSet<String> finishedRecycles = new HashSet<>();
		for(RecyclingCalculation valid : validated)
			if(finishedRecycles.add(valid.stack.toString())&&!valid.outputs.isEmpty())
				ArcFurnaceRecipe.recipeList.add(new ArcRecyclingRecipe(valid.outputs, valid.stack, 100, 512));
		for(RecyclingCalculation invalid : Sets.newHashSet(nonValidated.values()))
			if(finishedRecycles.add(invalid.stack.toString())&&!invalid.outputs.isEmpty())
			{
				IELogger.info("Couldn't fully analyze "+invalid.stack+", missing knowledge for "+invalid.queriedSubcomponents);
				ArcFurnaceRecipe.recipeList.add(new ArcRecyclingRecipe(invalid.outputs, invalid.stack, 100, 512));
			}
		hasProfiled = true;
	}

	public static class RegistryIterationThread extends Thread
	{
		final List<IRecipe> recipeList;
		final int baseOffset;
		final int passes;
		ArrayList<RecyclingCalculation> calculatedOutputs = new ArrayList<>();

		public RegistryIterationThread(List<IRecipe> recipeList, int baseOffset, int passes)
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
				IRecipe recipe = recipeList.get(baseOffset+pass);
				if(!recipe.getRecipeOutput().isEmpty()&&isValidForRecycling(recipe.getRecipeOutput()))
				{
					RecyclingCalculation calc = getRecycleCalculation(recipe.getRecipeOutput(), recipe);
					if(calc!=null)
						calculatedOutputs.add(calc);
				}
			}
		}
	}

	public static boolean isValidForRecycling(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		Item item = stack.getItem();
		if(item instanceof ItemTool||item instanceof ItemSword||item instanceof ItemHoe||item instanceof ItemArmor)
			return true;
		for(Object recycle : ArcFurnaceRecipe.recyclingAllowed)
			if(ApiUtils.stackMatchesObject(stack, recycle))
				return true;
		return false;
	}

	public static RecyclingCalculation getRecycleCalculation(ItemStack stack, IRecipe recipe)
	{
		NonNullList<Ingredient> inputs = recipe.getIngredients();
		if(!inputs.isEmpty())
		{
			int inputSize = stack.getCount();
			Map<ItemStack, Integer> missingSub = new HashMap<>();
			Map<ItemStack, Double> outputs = new IdentityHashMap<>();
			for(Ingredient in : inputs)
				if(in!=null&&in!=Ingredient.EMPTY)
				{
					ItemStack inputStack = IEApi.getPreferredStackbyMod(in.getMatchingStacks());
					if(inputStack.isEmpty())
					{
						IELogger.warn("Recipe has invalid inputs and will be ignored: "+recipe+" ("+recipe.getRegistryName()+")");
						return null;
					}

					Object[] brokenDown = ApiUtils.breakStackIntoPreciseIngots(inputStack);
					if(brokenDown==null)
					{
						if(isValidForRecycling(inputStack))
						{
							boolean b = false;
							for(ItemStack storedMiss : missingSub.keySet())
								if(OreDictionary.itemMatches(inputStack, storedMiss, false))
								{
									missingSub.put(storedMiss, missingSub.get(storedMiss)+inputStack.getCount());
									b = true;
								}
							if(!b)
								missingSub.put(Utils.copyStackWithAmount(inputStack, 1), inputStack.getCount());
						}
						continue;
					}
					if(brokenDown[0] instanceof ItemStack&&!((ItemStack)brokenDown[0]).isEmpty()
							&&brokenDown[1]!=null&&(Double)brokenDown[1] > 0)
					{
						boolean invalidOutput = false;
						for(Object invalid : ArcFurnaceRecipe.invalidRecyclingOutput)
							if(ApiUtils.stackMatchesObject((ItemStack)brokenDown[0], invalid))
								invalidOutput = true;
						if(!invalidOutput)
						{
							boolean b = false;
							for(ItemStack storedOut : outputs.keySet())
								if(OreDictionary.itemMatches((ItemStack)brokenDown[0], storedOut, false))
								{
									outputs.put(storedOut, outputs.get(storedOut)+(Double)brokenDown[1]);
									b = true;
								}
							if(!b)
								outputs.put(Utils.copyStackWithAmount((ItemStack)brokenDown[0], 1), (Double)brokenDown[1]);
						}
					}
				}
			Map<ItemStack, Double> outputScaled = new IdentityHashMap<>(outputs.size());
			for(Entry<ItemStack, Double> e : outputs.entrySet())
				outputScaled.put(e.getKey(), e.getValue()/inputSize);
			if(!outputs.isEmpty()||!missingSub.isEmpty())
			{
				ItemStack in = Utils.copyStackWithAmount(stack, 1);
				if(in.getItem().isDamageable())
					in.setItemDamage(OreDictionary.WILDCARD_VALUE);
				RecyclingCalculation calc = new RecyclingCalculation(recipe, in
						, outputScaled);
				if(!missingSub.isEmpty())
					for(ItemStack s : missingSub.keySet())
						calc.queriedSubcomponents.put(s, (double)missingSub.get(s)/inputSize);
				return calc;
			}
		}
		return null;
	}

	public static class RecyclingCalculation
	{
		IRecipe recipe;
		ItemStack stack;
		Map<ItemStack, Double> outputs;
		Map<ItemStack, Double> queriedSubcomponents = new HashMap<>();

		public RecyclingCalculation(IRecipe recipe, ItemStack stack, Map<ItemStack, Double> outputs)
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
				if(OreDictionary.itemMatches(next, calc.stack, false))
				{
					double queriedAmount = queriedSubcomponents.get(next);
					for(Map.Entry<ItemStack, Double> e : calc.outputs.entrySet())
					{
						double scaledVal = e.getValue()*queriedAmount;
						boolean b = true;
						for(ItemStack key : outputs.keySet())
							if(OreDictionary.itemMatches(key, e.getKey(), false))
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