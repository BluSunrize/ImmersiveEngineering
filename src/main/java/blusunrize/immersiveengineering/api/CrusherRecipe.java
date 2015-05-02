package blusunrize.immersiveengineering.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.util.Utils;

/**
 * @author BluSunrize - 01.05.2015
 *
 * The recipe for the coke oven
 */
public class CrusherRecipe
{
	public final Object input;
	public final ItemStack output;
	public final int energy;

	public CrusherRecipe(ItemStack output, Object input, int energy)
	{
		this.input=input;
		this.output=output;
		this.energy=energy;
	}

	public static ArrayList<CrusherRecipe> recipeList = new ArrayList<CrusherRecipe>();
	public static void addRecipe(ItemStack output, Object input, int energy)
	{
		recipeList.add(new CrusherRecipe(output, input, energy));
	}
	public static CrusherRecipe findRecipe(ItemStack input)
	{
		for(CrusherRecipe recipe : recipeList)
		{
			if(recipe.input instanceof ItemStack && OreDictionary.itemMatches((ItemStack)recipe.input, input, false))
				return recipe;
			else if(recipe.input instanceof String && Utils.compareToOreName(input, (String)recipe.input))
				return recipe;
		}
		return null;
	}
	public static List<CrusherRecipe> removeRecipes(ItemStack stack)
	{
		List<CrusherRecipe> list = new ArrayList();
		Iterator<CrusherRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			CrusherRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}
}