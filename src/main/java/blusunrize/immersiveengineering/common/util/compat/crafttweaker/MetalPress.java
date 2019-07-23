/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import com.google.common.collect.ArrayListMultimap;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.immersiveengineering.MetalPress")
public class MetalPress
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, IItemStack mold, int energy, @Optional int inputSize)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput==null)
			return;
		ItemStack sOut = CraftTweakerHelper.toStack(output);
		ItemStack sMold = CraftTweakerHelper.toStack(mold);
		if(!sOut.isEmpty()&&!sMold.isEmpty())
		{
			MetalPressRecipe r = new MetalPressRecipe(sOut, oInput, ApiUtils.createComparableItemStack(sMold, true), energy);
			if(inputSize > 0)
				r.setInputSize(inputSize);
			CraftTweakerAPI.apply(new Add(r));
		}
	}

	private static class Add implements IAction
	{
		private final MetalPressRecipe recipe;

		public Add(MetalPressRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			MetalPressRecipe.recipeList.put(recipe.mold, recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Metal Press Recipe for "+recipe.output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeRecipe(IItemStack output)
	{
		CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toStack(output)));
	}

	private static class Remove implements IAction
	{
		private final ItemStack output;
		List<MetalPressRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = MetalPressRecipe.removeRecipes(output);
		}

		@Override
		public String describe()
		{
			return "Removing Metal Press Recipes for "+output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeRecipeByMold(IItemStack mold)
	{
		CraftTweakerAPI.apply(new RemoveByMold(CraftTweakerHelper.toStack(mold)));
	}

	private static class RemoveByMold implements IAction
	{
		private final ComparableItemStack mold;
		List<MetalPressRecipe> removedRecipes;

		public RemoveByMold(ItemStack mold)
		{
			this.mold = ApiUtils.createComparableItemStack(mold, true);
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList<>(MetalPressRecipe.recipeList.get(mold));
			MetalPressRecipe.recipeList.removeAll(mold);
		}

		@Override
		public String describe()
		{
			return "Removing Metal Press Recipes for Mold: "+mold.stack.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		ArrayListMultimap<ComparableItemStack, MetalPressRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = ArrayListMultimap.create(MetalPressRecipe.recipeList);
			MetalPressRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Metal Press Recipes";
		}
	}
}