/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import com.google.common.collect.ArrayListMultimap;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ZenClass("mods.immersiveengineering.Blueprint")
public class Blueprint
{
	@ZenMethod
	public static void addRecipe(String category, IItemStack output, IIngredient[] inputs)
	{
		Object[] oInputs = new Object[inputs.length];
		for(int i = 0; i < inputs.length; i++)
			oInputs[i] = CraftTweakerHelper.toObject(inputs[i]);
		BlueprintCraftingRecipe r = new BlueprintCraftingRecipe(category, CraftTweakerHelper.toStack(output), oInputs);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
	{
		private final BlueprintCraftingRecipe recipe;

		public Add(BlueprintCraftingRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			if(!BlueprintCraftingRecipe.blueprintCategories.contains(recipe.blueprintCategory))
				BlueprintCraftingRecipe.blueprintCategories.add(recipe.blueprintCategory);
			BlueprintCraftingRecipe.recipeList.put(recipe.blueprintCategory, recipe);
//			CraftTweakerAPI.getIjeiRecipeRegistry().addRecipe(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Blueprint Recipe for "+recipe.output.getDisplayName();
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
		List<BlueprintCraftingRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList();
			Iterator<String> itCat = BlueprintCraftingRecipe.blueprintCategories.iterator();
			while(itCat.hasNext())
			{
				String category = itCat.next();
				Iterator<BlueprintCraftingRecipe> it = BlueprintCraftingRecipe.recipeList.get(category).iterator();
				while(it.hasNext())
				{
					BlueprintCraftingRecipe ir = it.next();
					if(OreDictionary.itemMatches(ir.output, output, true) && ItemStack.areItemStackTagsEqual(ir.output, output))
					{
						removedRecipes.add(ir);
//						CraftTweakerAPI.getIjeiRecipeRegistry().removeRecipe(ir);
						it.remove();
					}
				}
				if(BlueprintCraftingRecipe.recipeList.get(category).isEmpty())
					itCat.remove();
			}
		}

		@Override
		public String describe()
		{
			return "Removing Blueprint Recipe for "+output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		ArrayListMultimap<String, BlueprintCraftingRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = ArrayListMultimap.create(BlueprintCraftingRecipe.recipeList);
			BlueprintCraftingRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Blueprint Recipes";
		}
	}
}
