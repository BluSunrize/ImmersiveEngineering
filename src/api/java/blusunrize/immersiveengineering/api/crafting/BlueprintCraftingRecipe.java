/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author BluSunrize - 21.07.2015
 * <br>
 * These recipes are accessible in the Engineers Workbench, with a Engineers Blueprint item.<br>
 * For every "category" registered, a blueprint item will be added automatically.
 */
public class BlueprintCraftingRecipe extends MultiblockRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<BlueprintCraftingRecipe>> SERIALIZER;
	public static final SetRestrictedField<RecipeMultiplier> MULTIPLIERS = SetRestrictedField.common();

	public static final CachedRecipeList<BlueprintCraftingRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.BLUEPRINT);
	private static int reloadCountForCategories = CachedRecipeList.INVALID_RELOAD_COUNT;
	private static Map<String, List<RecipeHolder<BlueprintCraftingRecipe>>> recipesByCategory = Collections.emptyMap();
	public static SetRestrictedField<ItemLike> blueprintItem = SetRestrictedField.common();

	public final String blueprintCategory;
	public final TagOutput output;
	public final List<IngredientWithSize> inputs;

	public BlueprintCraftingRecipe(String blueprintCategory, TagOutput output, List<IngredientWithSize> inputs)
	{
		//Time and energy values are for the automatic workbench
		super(output, IERecipeTypes.BLUEPRINT, 180, 23040, MULTIPLIERS);
		this.blueprintCategory = blueprintCategory;
		this.output = output;
		this.inputs = inputs;

		setInputListWithSizes(Lists.newArrayList(this.inputs));
		this.outputList = new TagOutputList(output);
	}

	@Override
	protected IERecipeSerializer<BlueprintCraftingRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public static ItemStack getTypedBlueprint(String type)
	{
		ItemStack stack = new ItemStack(blueprintItem.get());
		stack.set(IEApiDataComponents.BLUEPRINT_TYPE, type);
		return stack;
	}

	public boolean matchesRecipe(NonNullList<ItemStack> query)
	{
		return getMaxCrafted(query) > 0;
	}

	public int getMaxCrafted(NonNullList<ItemStack> query)
	{
		HashMap<ItemStack, Integer> queryAmount = new HashMap<>();
		for(ItemStack q : query)
			if(!q.isEmpty())
			{
				boolean inc = false;
				for(ItemStack key : queryAmount.keySet())
					if(ItemStack.isSameItemSameComponents(q, key))
					{
						queryAmount.put(key, queryAmount.get(key)+q.getCount());
						inc = true;
					}
				if(!inc)
					queryAmount.put(q, q.getCount());
			}

		OptionalInt maxCrafted = OptionalInt.empty();
		for(IngredientWithSize ingr : inputs)
		{
			int maxCraftedWithIngredient = 0;
			int req = ingr.getCount();
			Iterator<Entry<ItemStack, Integer>> queryIt = queryAmount.entrySet().iterator();
			while(queryIt.hasNext())
			{
				Entry<ItemStack, Integer> e = queryIt.next();
				ItemStack compStack = e.getKey();
				if(ingr.testIgnoringSize(compStack))
				{
					int taken = e.getValue()/req;
					if(taken > 0)
					{
						e.setValue(e.getValue()-taken*req);
						if(e.getValue() <= 0)
							queryIt.remove();
						maxCraftedWithIngredient += taken;
					}
				}
			}
			if(maxCraftedWithIngredient <= 0)
				return 0;
			else if(maxCrafted.isPresent())
				maxCrafted = OptionalInt.of(Math.min(maxCrafted.getAsInt(), maxCraftedWithIngredient));
			else
				maxCrafted = OptionalInt.of(maxCraftedWithIngredient);
		}
		return maxCrafted.orElse(0);
	}

	public NonNullList<ItemStack> consumeInputs(NonNullList<ItemStack> query, int crafted)
	{
		NonNullList<ItemStack> consumed = NonNullList.create();
		for(IngredientWithSize ingr : inputs)
		{
			int inputSize = ingr.getCount()*crafted;

			for(int i = 0; i < query.size(); i++)
			{
				ItemStack queryStack = query.get(i);
				if(!queryStack.isEmpty()&&ingr.testIgnoringSize(queryStack))
				{
					int taken = Math.min(queryStack.getCount(), inputSize);
					consumed.add(queryStack.copyWithCount(taken));
					if(taken >= queryStack.getCount()&&queryStack.getItem().hasCraftingRemainingItem(queryStack))
						query.set(i, queryStack.getItem().getCraftingRemainingItem(queryStack));
					else
						queryStack.shrink(taken);
					inputSize -= taken;
					if(inputSize <= 0)
						break;
				}
			}
		}
		return consumed;
	}

	public static List<RecipeHolder<BlueprintCraftingRecipe>> findRecipes(Level level, String blueprintCategory)
	{
		updateRecipeCategories(level);
		return recipesByCategory.getOrDefault(blueprintCategory, ImmutableList.of())
				.stream()
				.toList();
	}

	public static void updateRecipeCategories(Level level)
	{
		if(reloadCountForCategories==CachedRecipeList.getReloadCount())
			return;
		recipesByCategory = RECIPES.getRecipes(level).stream()
				.collect(Collectors.groupingBy(r -> r.value().blueprintCategory));
		for(Entry<String, List<RecipeHolder<BlueprintCraftingRecipe>>> e : recipesByCategory.entrySet())
			e.getValue().sort(Comparator.comparing(RecipeHolder::id));
		reloadCountForCategories = CachedRecipeList.getReloadCount();
	}

	public static Set<String> getCategoriesWithRecipes(Level level)
	{
		updateRecipeCategories(level);
		return recipesByCategory.keySet();
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}