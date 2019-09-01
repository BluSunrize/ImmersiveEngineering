/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.immersiveengineering.AlloySmelter")
public class AlloySmelter
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient first, IIngredient second, int time)
	{
		Object oFirst = CraftTweakerHelper.toObject(first), oSecond = CraftTweakerHelper.toObject(second);
		if(oFirst==null||oSecond==null)
			return;

		AlloyRecipe r = new AlloyRecipe(CraftTweakerHelper.toStack(output), oFirst, oSecond, time);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
	{
		private final AlloyRecipe recipe;

		public Add(AlloyRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			AlloyRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Alloy Smelter Recipe for "+recipe.output.getDisplayName();
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
		List<AlloyRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = AlloyRecipe.removeRecipes(output);
		}

		@Override
		public String describe()
		{
			return "Removing Alloy Smelter Recipe for "+output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		List<AlloyRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList<>(AlloyRecipe.recipeList);
			AlloyRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Alloy Smelter Recipes";
		}
	}
}
