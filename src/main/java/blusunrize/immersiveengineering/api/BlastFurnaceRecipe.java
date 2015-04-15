package blusunrize.immersiveengineering.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.util.Utils;

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
	public static BlastFurnaceRecipe findRecipe(ItemStack input)
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

	public static HashMap<Object, Integer> blastFuels = new HashMap<Object, Integer>();
	public static void addBlastFuel(Object fuel, int burnTime)
	{
		if(fuel instanceof ItemStack || fuel instanceof String)
			blastFuels.put(fuel, burnTime);
	}
	public static int getBlastFuelTime(ItemStack stack)
	{
		for(Map.Entry<Object,Integer> e : blastFuels.entrySet())
			if(Utils.stackMatchesObject(stack, e.getKey()))
				return e.getValue();
		return 0;
	}
	public static boolean isValidBlastFuel(ItemStack stack)
	{
		return getBlastFuelTime(stack)>0;
	}
}