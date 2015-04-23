package blusunrize.immersiveengineering.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.util.Utils;

/**
 * @author BluSunrize - 23.03.2015
 *
 * The recipe for the coke oven
 */
public class BlastFurnaceRecipe
{
	public final Object input;
	public final ItemStack output;
	public final int time;

	public BlastFurnaceRecipe(ItemStack output, Object input, int time)
	{
		this.input=input;
		this.output=output;
		this.time=time;
	}

	public static ArrayList<BlastFurnaceRecipe> recipeList = new ArrayList<BlastFurnaceRecipe>();
	public static void addRecipe(ItemStack output, Object input, int time)
	{
		recipeList.add(new BlastFurnaceRecipe(output, input, time));
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

	public static HashMap<String, Integer> blastFuels = new HashMap<String, Integer>();
	public static void addBlastFuel(Object fuel, int burnTime)
	{
		if(fuel instanceof String)
			blastFuels.put((String)fuel, burnTime);
		else if(fuel instanceof ItemStack && !Utils.nameFromStack((ItemStack)fuel).isEmpty())
			blastFuels.put(Utils.nameFromStack((ItemStack)fuel)+"::"+((ItemStack)fuel).getItemDamage(), burnTime);
	}
	public static int getBlastFuelTime(ItemStack stack)
	{
		for(Map.Entry<String,Integer> e : blastFuels.entrySet())
		{
			if(Utils.compareToOreName(stack, e.getKey()))
				return e.getValue();
			else
			{
				int lIndx = e.getKey().lastIndexOf("::");
				if(lIndx>0)
				{
					String key = e.getKey().substring(0,lIndx);
					try{
						int reqMeta = Integer.parseInt(e.getKey().substring(lIndx+2));
						if(key.equals(Utils.nameFromStack(stack)) && (reqMeta==OreDictionary.WILDCARD_VALUE || reqMeta==stack.getItemDamage()))
						{
							return e.getValue();
						}
					}catch(Exception exception){}
				}
			}
		}
		return 0;
	}
	public static boolean isValidBlastFuel(ItemStack stack)
	{
		return getBlastFuelTime(stack)>0;
	}
}