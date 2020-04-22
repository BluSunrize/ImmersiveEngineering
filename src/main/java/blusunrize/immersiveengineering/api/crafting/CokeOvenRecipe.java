/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the coke oven
 */
public class CokeOvenRecipe
{
	public final IngredientWithSize input;
	public final ItemStack output;
	public final int time;
	public final int creosoteOutput;

	public CokeOvenRecipe(ItemStack output, IngredientWithSize input, int time, int creosoteOutput)
	{
		this.output = output;
		this.input = input;
		this.time = time;
		this.creosoteOutput = creosoteOutput;
	}

	public static List<CokeOvenRecipe> recipeList = new ArrayList<>();

	public static void addRecipe(ItemStack output, IngredientWithSize input, int time, int creosoteOutput)
	{
		CokeOvenRecipe recipe = new CokeOvenRecipe(output, input, time, creosoteOutput);
		if(recipe.input!=null)
			recipeList.add(recipe);
	}

	public static CokeOvenRecipe findRecipe(ItemStack input)
	{
		for(CokeOvenRecipe recipe : recipeList)
			if(ApiUtils.stackMatchesObject(input, recipe.input))
				return recipe;
		return null;
	}

	public static List<CokeOvenRecipe> removeRecipes(ItemStack stack)
	{
		List<CokeOvenRecipe> list = new ArrayList<>();
		Iterator<CokeOvenRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			CokeOvenRecipe ir = it.next();
			if(ItemStack.areItemsEqual(ir.output, stack))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}
}
