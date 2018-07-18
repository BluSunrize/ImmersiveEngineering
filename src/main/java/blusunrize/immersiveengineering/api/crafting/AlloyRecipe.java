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
import net.minecraftforge.oredict.OreDictionary;

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
	public final IngredientStack input0;
	public final IngredientStack input1;
	public final ItemStack output;
	public final int time;

	public AlloyRecipe(ItemStack output, Object input0, Object input1, int time)
	{
		this.output = output;
		this.input0 = ApiUtils.createIngredientStack(input0);
		this.input1 = ApiUtils.createIngredientStack(input1);
		this.time = time;
	}

	public static ArrayList<AlloyRecipe> recipeList = new ArrayList<AlloyRecipe>();

	public static void addRecipe(ItemStack output, Object input0, Object input1, int time)
	{
		AlloyRecipe recipe = new AlloyRecipe(output, input0, input1, time);
		if(recipe.input0!=null&&recipe.input1!=null)
			recipeList.add(recipe);
	}

	public static AlloyRecipe findRecipe(ItemStack input0, ItemStack input1)
	{
		for(AlloyRecipe recipe : recipeList)
			if((recipe.input0.matchesItemStack(input0)&&recipe.input1.matchesItemStack(input1))||(recipe.input0.matchesItemStack(input1)&&recipe.input1.matchesItemStack(input0)))
				return recipe;
		return null;
	}

	public static List<AlloyRecipe> removeRecipes(ItemStack stack)
	{
		List<AlloyRecipe> list = new ArrayList();
		Iterator<AlloyRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			AlloyRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}
}
