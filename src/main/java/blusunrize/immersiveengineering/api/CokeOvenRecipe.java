package blusunrize.immersiveengineering.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author BluSunrize - 23.03.2015
 *
 * The recipe for the coke oven
 */
public class CokeOvenRecipe
{
	public final Object input;
	public final ItemStack output;
	public final int time;
	public final int creosoteOutput;

	public CokeOvenRecipe(ItemStack output, Object input, int time, int creosoteOutput)
	{
		this.input=input;
		this.output=output;
		this.time=time;
		this.creosoteOutput=creosoteOutput;
	}

	public static ArrayList<CokeOvenRecipe> recipeList = new ArrayList<CokeOvenRecipe>();
	public static void addRecipe(ItemStack output, Object input, int time, int creosoteOutput)
	{
		recipeList.add(new CokeOvenRecipe(output, input, time, creosoteOutput));
	}
	public static CokeOvenRecipe findRecipe(ItemStack input)
	{
		for(CokeOvenRecipe recipe : recipeList)
			if(recipe.input instanceof ItemStack && OreDictionary.itemMatches((ItemStack)recipe.input, input, false))
				return recipe;
			else if(recipe.input instanceof String && Utils.compareToOreName(input, (String)recipe.input))
				return recipe;
		return null;
	}
	public static List<CokeOvenRecipe> removeRecipes(ItemStack stack)
	{
		List<CokeOvenRecipe> list = new ArrayList();
		Iterator<CokeOvenRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			CokeOvenRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}
}
