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
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArcRecyclingCalculator
{
	private final List<Recipe<?>> recipeList;
	private final long startTime;
	private final ArcRecyclingChecker checker;
	private final RegistryAccess tags;

	public ArcRecyclingCalculator(Collection<Recipe<?>> allRecipes, RegistryAccess tags)
	{
		this.tags = tags;
		this.startTime = System.currentTimeMillis();
		Pair<Predicate<Recipe<?>>, ArcRecyclingChecker> pair = ArcRecyclingChecker.assembleRecyclingFilter(tags);
		this.checker = pair.getSecond();
		this.recipeList = allRecipes.stream()
				.filter(pair.getFirst())
				.collect(Collectors.toList());
	}

	public List<ArcFurnaceRecipe> run()
	{
		RecipeIterator iterator = new RecipeIterator(recipeList, checker, tags);
		iterator.process();
		int timeout = 0;
		while(!iterator.nonValidated.isEmpty()&&timeout++ < (iterator.invalidCount*10))
		{
			ArrayList<RecyclingCalculation> newlyValid = new ArrayList<>();
			for(RecyclingCalculation valid : iterator.validated)
				for(ItemStack key : iterator.nonValidated.keySet())
				{
					if(ItemStack.isSameItem(key, valid.stack))
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
		IELogger.info("Finished recipe profiler for Arc Recycling, took "
				+(System.currentTimeMillis()-startTime)+" milliseconds");
		return generatedRecipes;
	}

	public static Mutable<List<ArcFurnaceRecipe>> makeFuture()
	{
		Mutable<List<ArcFurnaceRecipe>> result = new MutableObject<>();
		Mutable<Object> eventListener = new MutableObject<>();
		eventListener.setValue(new Object()
		{
			@SubscribeEvent
			public void onServerStarted(ServerStartedEvent ev)
			{
				fillInRecipes(ev.getServer());
			}

			@SubscribeEvent
			public void onServerTick(ServerTickEvent ev)
			{
				fillInRecipes(ServerLifecycleHooks.getCurrentServer());
			}

			@SubscribeEvent
			public void onDatapackSync(OnDatapackSyncEvent ev)
			{
				fillInRecipes(ev.getPlayerList().getServer());
			}

			private void fillInRecipes(MinecraftServer server)
			{
				Preconditions.checkState(result.getValue()==null);
				MinecraftForge.EVENT_BUS.unregister(eventListener.getValue());
				Collection<Recipe<?>> recipes = server.getRecipeManager().getRecipes();
				ArcRecyclingCalculator calculator = new ArcRecyclingCalculator(recipes, server.registryAccess());
				result.setValue(calculator.run());
			}
		});
		MinecraftForge.EVENT_BUS.register(eventListener.getValue());
		return result;
	}

	private ArcRecyclingRecipe makeRecipe(RecyclingCalculation calculation)
	{
		ResourceLocation id = new ResourceLocation(Lib.MODID, "recycling/"+ForgeRegistries.ITEMS.getKey(calculation.stack.getItem()).getPath());
		return new ArcRecyclingRecipe(
				id, () -> tags,
				calculation.outputs.entrySet().stream()
						.map(e -> Pair.of(Lazy.of(e::getKey), e.getValue()))
						.toList(),
				IngredientWithSize.of(calculation.stack), 100, 51200);
	}

	private static class RecipeIterator
	{
		final List<Recipe<?>> recipeList;
		final List<RecyclingCalculation> validated = new ArrayList<>();
		final Multimap<ItemStack, RecyclingCalculation> nonValidated = ArrayListMultimap.create();
		private final ArcRecyclingChecker checker;
		int invalidCount = 0;
		private final RegistryAccess tags;

		public RecipeIterator(List<Recipe<?>> recipeList, ArcRecyclingChecker checker, RegistryAccess tags)
		{
			this.recipeList = recipeList;
			this.checker = checker;
			this.tags = tags;
		}

		public void process()
		{
			for(Recipe<?> recipe : recipeList)
			{
				RecyclingCalculation calc = getRecycleCalculation(recipe.getResultItem(tags), recipe);
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
			Pair<ItemStack, Double> brokenDown = ApiUtils.breakStackIntoPreciseIngots(tags, stack);
			if(brokenDown!=null&&ArcRecyclingChecker.isValidRecyclingOutput(tags, brokenDown.getFirst())&&brokenDown.getSecond() > 0)
				return new RecyclingCalculation(recipe, ItemHandlerHelper.copyStackWithSize(stack, 1),
						ImmutableMap.of(brokenDown.getFirst(), brokenDown.getSecond()));

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
						if(matchingStacks.length > 0)
							inputStack = IEApi.getPreferredStackbyMod(in.getItems());
						if(inputStack.isEmpty())
						{
							IELogger.warn("Recipe has invalid inputs and will be ignored: "+recipe+" ("+recipe.getId()+")");
							return null;
						}
						brokenDown = ApiUtils.breakStackIntoPreciseIngots(tags, inputStack);
						if(brokenDown==null)
						{
							if(checker.isAllowed(tags, inputStack)&&ArcRecyclingChecker.isValidRecyclingOutput(tags, inputStack))
							{
								boolean b = false;
								for(ItemStack storedMiss : missingSub.keySet())
									if(ItemStack.isSameItem(inputStack, storedMiss))
									{
										missingSub.put(storedMiss, missingSub.get(storedMiss)+inputStack.getCount());
										b = true;
									}
								if(!b)
									missingSub.put(ItemHandlerHelper.copyStackWithSize(inputStack, 1), inputStack.getCount());
							}
							continue;
						}
						if(!brokenDown.getFirst().isEmpty()&&brokenDown.getSecond() > 0)
						{
							boolean invalidOutput = !ArcRecyclingChecker.isValidRecyclingOutput(tags, brokenDown.getFirst());
							if(!invalidOutput)
							{
								boolean b = false;
								for(ItemStack storedOut : outputs.keySet())
									if(ItemStack.isSameItem(brokenDown.getFirst(), storedOut))
									{
										outputs.put(storedOut, outputs.get(storedOut)+brokenDown.getSecond());
										b = true;
									}
								if(!b)
									outputs.put(ItemHandlerHelper.copyStackWithSize(brokenDown.getFirst(), 1), brokenDown.getSecond());
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
				if(ItemStack.isSameItem(next, calc.stack))
				{
					double queriedAmount = queriedSubcomponents.get(next);
					for(Map.Entry<ItemStack, Double> e : calc.outputs.entrySet())
					{
						double scaledVal = e.getValue()*queriedAmount;
						boolean b = true;
						for(ItemStack key : outputs.keySet())
							if(ItemStack.isSameItem(key, e.getKey()))
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