/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.immersiveengineering.BottlingMachine")
public class BottlingMachine
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, ILiquidStack fluid)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput==null||output==null||fluid==null)
			return;

		BottlingMachineRecipe r = new BottlingMachineRecipe(CraftTweakerHelper.toStack(output), oInput, CraftTweakerHelper.toFluidStack(fluid));
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
	{
		private final BottlingMachineRecipe recipe;

		public Add(BottlingMachineRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			BottlingMachineRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Bottling Machine Recipe for "+recipe.output.getDisplayName();
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
		List<BottlingMachineRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = BottlingMachineRecipe.removeRecipes(output);
		}

		@Override
		public String describe()
		{
			return "Removing Bottling Machine Recipe for "+output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		List<BottlingMachineRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList<>(BottlingMachineRecipe.recipeList);
			BottlingMachineRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Bottling Machine Recipes";
		}
	}
}
