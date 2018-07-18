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
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class ArcRecyclingThreadHandler extends Thread
{
	static boolean hasProfiled = false;
	public static List<ArcFurnaceRecipe> recipesToAdd = null;
	private List<IRecipe> recipeList = null;

	private ArcRecyclingThreadHandler(List<IRecipe> r)
	{
		recipeList = r;
	}

	public static void doRecipeProfiling()
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
					IECompatModule.jeiRemoveFunc.accept(recipe);
					r++;
				}
			}
		IELogger.info("Arc Recycling: Removed "+r+" old recipes");
		recipesToAdd = null;
		new ArcRecyclingThreadHandler(ForgeRegistries.RECIPES.getValues()).start();
	}

	@Override
	public void run()
	{
		int threadAmount = Runtime.getRuntime().availableProcessors();
		IELogger.info("Starting recipe profiler for Arc Recycling, using "+threadAmount+" Threads");
		long timestamp = System.currentTimeMillis();
		RegistryIterationThread[] threads = new RegistryIterationThread[threadAmount];
		boolean divisable = recipeList.size()%threadAmount==0;
		int limit = divisable?(recipeList.size()/threadAmount): (recipeList.size()/(threadAmount-1));
		int leftOver = divisable?limit: (recipeList.size()-(threadAmount-1)*limit);
		for(int i = 0; i < threadAmount; i++)
			threads[i] = new RegistryIterationThread(recipeList, limit*i, i==(threadAmount-1)?leftOver: limit);

		//iterate over each thread individually
		ArrayList<RecyclingCalculation> validated = new ArrayList<RecyclingCalculation>();
		ArrayListMultimap<ItemStack, RecyclingCalculation> nonValidated = ArrayListMultimap.create();
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
						for(ItemStack s : calc.queriedSubcomponents)
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
			ArrayList<RecyclingCalculation> newlyValid = new ArrayList<RecyclingCalculation>();
			for(RecyclingCalculation valid : validated)
			{
				Iterator<Map.Entry<ItemStack, RecyclingCalculation>> itNonValid = nonValidated.entries().iterator();
				while(itNonValid.hasNext())
				{
					Map.Entry<ItemStack, RecyclingCalculation> e = itNonValid.next();
					if(OreDictionary.itemMatches(e.getKey(), valid.stack, false))
					{
						RecyclingCalculation nonValid = e.getValue();
						if(nonValid.validateSubcomponent(valid))
							newlyValid.add(nonValid);
					}
				}
			}
			nonValidated.values().removeAll(newlyValid);
			validated.addAll(newlyValid);
		}
		//HashSet to avoid duplicates
		HashSet<String> finishedRecycles = new HashSet<String>();
		List<ArcFurnaceRecipe> ret = new ArrayList<>();
		for(RecyclingCalculation valid : validated)
			if(finishedRecycles.add(valid.stack.toString())&&!valid.outputs.isEmpty())
				ret.add(new ArcRecyclingRecipe(valid.outputs, valid.stack, 100, 512));
		for(RecyclingCalculation invalid : Sets.newHashSet(nonValidated.values()))
			if(finishedRecycles.add(invalid.stack.toString())&&!invalid.outputs.isEmpty())
			{
//				IELogger.info("Couldn't fully analyze "+invalid.stack+", missing knowledge for "+invalid.queriedSubcomponents);
				ret.add(new ArcRecyclingRecipe(invalid.outputs, invalid.stack, 100, 512));
			}
		IELogger.info("Finished recipe profiler for Arc Recycling, took "+(System.currentTimeMillis()-timestamp)+" milliseconds");
		recipesToAdd = ret;
		hasProfiled = true;
	}

	public static class RegistryIterationThread extends Thread
	{
		final List<IRecipe> recipeList;
		final int baseOffset;
		final int passes;
		ArrayList<RecyclingCalculation> calculatedOutputs = new ArrayList<RecyclingCalculation>();

		public RegistryIterationThread(List<IRecipe> recipeList, int baseOffset, int passes)
		{
			setName("Immersive Engineering Registry Iteratoration Thread");
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
			if(Utils.stackMatchesObject(stack, recycle))
				return true;
		return false;
	}

	public static RecyclingCalculation getRecycleCalculation(ItemStack stack, IRecipe recipe)
	{
		NonNullList<Ingredient> inputs = recipe.getIngredients();

		if(inputs!=null)
		{
			int inputSize = stack.getCount();
			List<ItemStack> missingSub = new ArrayList<ItemStack>();
			HashMap<ItemStack, Double> outputs = new HashMap<ItemStack, Double>();
			for(Object in : inputs)
				if(in!=null)
				{
					ItemStack inputStack = ItemStack.EMPTY;
					if(in instanceof ItemStack)
						inputStack = (ItemStack)in;
					else if(in instanceof List)
					{
						final List<ItemStack> list = (List<ItemStack>)in;
						inputStack = list.size() > 0?list.get(0): ItemStack.EMPTY;
					}
					else if(in instanceof String)
						inputStack = IEApi.getPreferredOreStack((String)in);
					if(inputStack.isEmpty())
						continue;

					Object[] brokenDown = ApiUtils.breakStackIntoPreciseIngots(inputStack);
					if(brokenDown==null)
					{
						if(isValidForRecycling(inputStack))
							missingSub.add(inputStack);
						continue;
					}
					if(brokenDown[0]!=null&&brokenDown[0] instanceof ItemStack&&brokenDown[1]!=null&&(Double)brokenDown[1] > 0)
					{
						boolean invalidOutput = false;
						for(Object invalid : ArcFurnaceRecipe.invalidRecyclingOutput)
							if(Utils.stackMatchesObject((ItemStack)brokenDown[0], invalid))
								invalidOutput = true;
						if(!invalidOutput)
						{
							boolean b = false;
							for(ItemStack storedOut : outputs.keySet())
								if(OreDictionary.itemMatches((ItemStack)brokenDown[0], storedOut, false))
								{
									outputs.put(storedOut, outputs.get(storedOut)+(Double)brokenDown[1]/inputSize);
									b = true;
								}
							if(!b)
								outputs.put(Utils.copyStackWithAmount((ItemStack)brokenDown[0], 1), (Double)brokenDown[1]/inputSize);
						}
					}
				}
			if(!outputs.isEmpty()||!missingSub.isEmpty())
			{
				RecyclingCalculation calc = new RecyclingCalculation(recipe, Utils.copyStackWithAmount(stack, 1), outputs);
				calc.queriedSubcomponents.addAll(missingSub);
				return calc;
			}
		}
		return null;
	}

	public static class RecyclingCalculation
	{
		IRecipe recipe;
		ItemStack stack;
		HashMap<ItemStack, Double> outputs;
		ArrayList<ItemStack> queriedSubcomponents = new ArrayList<ItemStack>();

		public RecyclingCalculation(IRecipe recipe, ItemStack stack, HashMap<ItemStack, Double> outputs)
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
			Iterator<ItemStack> it = queriedSubcomponents.iterator();
			while(it.hasNext())
			{
				ItemStack next = it.next();
				if(OreDictionary.itemMatches(next, calc.stack, false))
				{
					for(Map.Entry<ItemStack, Double> e : calc.outputs.entrySet())
					{
						boolean b = true;
						for(ItemStack key : outputs.keySet())
							if(OreDictionary.itemMatches(key, e.getKey(), false))
							{
								outputs.put(key, outputs.get(key)+e.getValue());
								b = false;
								break;
							}
						if(b)
							outputs.put(e.getKey(), e.getValue());
					}
					it.remove();
				}
			}
			return isValid();
		}
	}

}