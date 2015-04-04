package blusunrize.immersiveengineering.common.crafting;

import java.util.ArrayList;

import blusunrize.immersiveengineering.common.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class BlastFurnaceRecipe
{
	public final Object input;
	public final ItemStack output;
	public final int time;

	public BlastFurnaceRecipe(Object input, ItemStack output, int time)
	{
		this.input=input;
		this.output=output;
		this.time=time;
	}

	public static ArrayList<BlastFurnaceRecipe> recipeList = new ArrayList<BlastFurnaceRecipe>();
	public static void addRecipe(Object input, ItemStack output, int time)
	{
		recipeList.add(new BlastFurnaceRecipe(input, output, time));
	}
	public static BlastFurnaceRecipe fineRecipe(ItemStack input)
	{
		for(BlastFurnaceRecipe recipe : recipeList)
		{
			if(recipe.input instanceof ItemStack && OreDictionary.itemMatches((ItemStack)recipe.input, input, false))
				return recipe;
			else if(recipe.input instanceof String && Utils.compareToOreName(input, (String)recipe.input))
				return recipe;
		}
		return null;
	}
}
