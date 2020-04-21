/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 19.05.2017
 * <br>
 * The recipe for the alloy smelter
 */
public class AlloyRecipe
{
	public final IngredientWithSize input0;
	public final IngredientWithSize input1;
	public final ItemStack output;
	public final int time;

	public AlloyRecipe(ItemStack output, Ingredient input0, Ingredient input1, int time)
	{
		this.output = output;
		this.input0 = input0 instanceof IngredientWithSize? (IngredientWithSize)input0: new IngredientWithSize(input0);
		this.input1 = input1 instanceof IngredientWithSize? (IngredientWithSize)input1: new IngredientWithSize(input1);
		this.time = time;
	}

	public static ArrayList<AlloyRecipe> recipeList = new ArrayList<AlloyRecipe>();

	public static void addRecipe(ItemStack output, Ingredient input0, Ingredient input1, int time)
	{
		AlloyRecipe recipe = new AlloyRecipe(output, input0, input1, time);
		if(recipe.input0!=null&&recipe.input1!=null)
			recipeList.add(recipe);
	}

	public static AlloyRecipe findRecipe(ItemStack input0, ItemStack input1)
	{
		for(AlloyRecipe recipe : recipeList)
			if((recipe.input0.test(input0)&&recipe.input1.test(input1))||(recipe.input0.test(input1)&&recipe.input1.test(input0)))
				return recipe;
		return null;
	}

	public static List<AlloyRecipe> removeRecipes(ItemStack stack)
	{
		List<AlloyRecipe> list = new ArrayList<>();
		Iterator<AlloyRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			AlloyRecipe ir = it.next();
			if(ItemStack.areItemsEqual(ir.output, stack))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}
}
