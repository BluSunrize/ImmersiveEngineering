package blusunrize.immersiveengineering.common.crafting;

import java.util.ArrayList;

import blusunrize.immersiveengineering.common.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class CokeOvenRecipe
{
	public final Object input;
	public final ItemStack output;
	public final int time;
	public final int creosoteOutput;

	public CokeOvenRecipe(Object input, ItemStack output, int time, int creosoteOutput)
	{
		this.input=input;
		this.output=output;
		this.time=time;
		this.creosoteOutput=creosoteOutput;
	}

	public static ArrayList<CokeOvenRecipe> recipeList = new ArrayList<CokeOvenRecipe>();
	public static void addRecipe(Object input, ItemStack output, int time, int creosoteOutput)
	{
		recipeList.add(new CokeOvenRecipe(input, output, time, creosoteOutput));
	}
	public static CokeOvenRecipe fineRecipe(ItemStack input)
	{
		for(CokeOvenRecipe recipe : recipeList)
			if(recipe.input instanceof ItemStack && OreDictionary.itemMatches((ItemStack)recipe.input, input, false))
				return recipe;
			else if(recipe.input instanceof String && Utils.compareToOreName(input, (String)recipe.input))
				return recipe;
		return null;
	}
}
