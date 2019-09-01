/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.immersiveengineering.CokeOven")
public class CokeOven
{
	@ZenMethod
	public static void addRecipe(IItemStack output, int fuelOutput, IIngredient input, int time)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput==null)
			return;

		CokeOvenRecipe r = new CokeOvenRecipe(CraftTweakerHelper.toStack(output), oInput, time, fuelOutput);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
	{
		private final CokeOvenRecipe recipe;

		public Add(CokeOvenRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			CokeOvenRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Coke Oven Recipe for "+recipe.output.getDisplayName();
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
		List<CokeOvenRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = CokeOvenRecipe.removeRecipes(output);
		}

		@Override
		public String describe()
		{
			return "Removing Coke Oven Recipe for "+output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		List<CokeOvenRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList<>(CokeOvenRecipe.recipeList);
			CokeOvenRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Coke Oven Recipes";
		}
	}
}
