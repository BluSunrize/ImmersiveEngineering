package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

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
	public ItemStack secondaryOutput;
	public float secondaryChance;

	public CrusherRecipe(ItemStack output, Object input, int energy)
	{
		this.output = output;
		this.input = ApiUtils.convertToValidRecipeInput(input);
		this.energy = energy;
	}
	public CrusherRecipe addSecondaryOutput(ItemStack output, float chance)
	{
		this.secondaryOutput = output;
		this.secondaryChance = chance;
		return this;
	}

	public static ArrayList<CrusherRecipe> recipeList = new ArrayList<CrusherRecipe>();
	public static CrusherRecipe addRecipe(ItemStack output, Object input, int energy)
	{
		CrusherRecipe r = new CrusherRecipe(output, input, energy);
		if(r.input!=null)
			recipeList.add(r);
		return r;
	}
	public static CrusherRecipe findRecipe(ItemStack input)
	{
		for(CrusherRecipe recipe : recipeList)
			if(ApiUtils.stackMatchesObject(input, recipe.input))
				return recipe;
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