/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
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

@ZenClass("mods.immersiveengineering.Crusher")
public class Crusher
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, int energy, @Optional IItemStack secondaryOutput, @Optional double secondaryChance)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput==null)
		{
			CraftTweakerAPI.getLogger().logError("Did not add crusher recipe for "+output.getDisplayName()+", input was null");
			return;
		}

		CrusherRecipe r = new CrusherRecipe(CraftTweakerHelper.toStack(output), oInput, energy);
		if(r.input==null)
		{
			CraftTweakerAPI.getLogger().logError("Did not add crusher recipe for "+output.getDisplayName()+", converted input was null");
			return;
		}
		if(secondaryOutput!=null)
			r.addToSecondaryOutput(CraftTweakerHelper.toStack(secondaryOutput), (float)secondaryChance);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
	{
		private final CrusherRecipe recipe;

		public Add(CrusherRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			CrusherRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Crusher Recipe for "+recipe.output.getDisplayName();
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
		List<CrusherRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = CrusherRecipe.removeRecipesForOutput(output);
		}

		@Override
		public String describe()
		{
			return "Removing Crusher Recipe for output: "+output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeRecipesForInput(IItemStack input)
	{
		CraftTweakerAPI.apply(new RemoveForInput(CraftTweakerHelper.toStack(input)));
	}

	private static class RemoveForInput implements IAction
	{
		private final ItemStack input;
		List<CrusherRecipe> removedRecipes;

		public RemoveForInput(ItemStack input)
		{
			this.input = input;
		}

		@Override
		public void apply()
		{
			removedRecipes = CrusherRecipe.removeRecipesForInput(input);
		}

		@Override
		public String describe()
		{
			return "Removing Crusher Recipe for input: "+input.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		List<CrusherRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList<>(CrusherRecipe.recipeList);
			CrusherRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Crusher Recipes";
		}
	}
}