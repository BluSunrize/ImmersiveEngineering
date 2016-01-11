package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the blast furnace
 */
public class BlastFurnaceRecipe
{
	public final Object input;
	public final ItemStack output;
	public final ItemStack slag;
	public final int time;

	public BlastFurnaceRecipe(ItemStack output, Object input, int time, ItemStack slag)
	{
		this.output=output;
		this.input=ApiUtils.convertToValidRecipeInput(input);
		this.time=time;
		this.slag=slag;
	}

	public static ArrayList<BlastFurnaceRecipe> recipeList = new ArrayList<BlastFurnaceRecipe>();
	public static void addRecipe(ItemStack output, Object input, int time, ItemStack slag)
	{
		BlastFurnaceRecipe recipe = new BlastFurnaceRecipe(output, input, time, slag);
		if(recipe.input!=null)
			recipeList.add(recipe);
	}
	public static BlastFurnaceRecipe findRecipe(ItemStack input)
	{
		for(BlastFurnaceRecipe recipe : recipeList)
			if(ApiUtils.stackMatchesObject(input, recipe.input))
				return recipe;
		return null;
	}
	public static List<BlastFurnaceRecipe> removeRecipes(ItemStack stack)
	{
		List<BlastFurnaceRecipe> list = new ArrayList();
		Iterator<BlastFurnaceRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			BlastFurnaceRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	public static HashMap<Object, Integer> blastFuels = new HashMap<Object, Integer>();
	public static void addBlastFuel(Object fuel, int burnTime)
	{
		Object key = ApiUtils.convertToValidRecipeInput(fuel);
		if(key!=null)
			blastFuels.put(key, burnTime);
	}
	public static int getBlastFuelTime(ItemStack stack)
	{
		for(Map.Entry<Object,Integer> e : blastFuels.entrySet())
			if(ApiUtils.stackMatchesObject(stack, e.getKey()))
				return e.getValue();
		return 0;
	}
	public static boolean isValidBlastFuel(ItemStack stack)
	{
		return getBlastFuelTime(stack)>0;
	}
}