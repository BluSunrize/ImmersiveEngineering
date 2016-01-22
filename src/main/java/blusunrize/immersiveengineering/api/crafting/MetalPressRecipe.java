package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author BluSunrize - 07.01.2016
 *
 * The recipe for the extruder
 */
public class MetalPressRecipe
{
	public final Object input;
	public final ComparableItemStack mold;
	public final ItemStack output;
	public final int energy;
	public int inputSize;

	public MetalPressRecipe(ItemStack output, Object input, ItemStack mould, int energy)
	{
		this.output = output;
		this.input = ApiUtils.convertToValidRecipeInput(input);
		this.mold = ApiUtils.createComparableItemStack(mould);
		this.energy = energy;
		inputSize = this.input instanceof ItemStack?((ItemStack)this.input).stackSize:1;
	}
	public MetalPressRecipe setInputSize(int size)
	{
		this.inputSize = size;
		if(this.input instanceof ItemStack)
			((ItemStack)this.input).stackSize = size;
		return this;
	}


	public static ArrayListMultimap<ComparableItemStack, MetalPressRecipe> recipeList = ArrayListMultimap.create();
	public static MetalPressRecipe addRecipe(ItemStack output, Object input, ItemStack mold, int energy)
	{
		MetalPressRecipe r = new MetalPressRecipe(output, input, mold, energy);
		recipeList.put(r.mold, r);
		return r;
	}
	public static MetalPressRecipe findRecipe(ItemStack mould, ItemStack input, boolean checkStackSize)
	{
		if(mould==null || input==null)
			return null;
		ComparableItemStack comp = ApiUtils.createComparableItemStack(mould);
		List<MetalPressRecipe> list = recipeList.get(comp);
		for(MetalPressRecipe recipe : list)
			if(ApiUtils.stackMatchesObject(input, recipe.input) && (!checkStackSize||input.stackSize>=recipe.inputSize))
				return recipe;
		return null;
	}
	public static List<MetalPressRecipe> removeRecipes(ItemStack output)
	{
		List<MetalPressRecipe> list = new ArrayList();
		for(ComparableItemStack mold : recipeList.keySet())
		{
			Iterator<MetalPressRecipe> it = recipeList.get(mold).iterator();
			while(it.hasNext())
			{
				MetalPressRecipe ir = it.next();
				if(OreDictionary.itemMatches(ir.output, output, true))
				{
					list.add(ir);
					it.remove();
				}
			}
		}
		return list;
	}

	public static boolean isValidMold(ItemStack itemStack)
	{
		if(itemStack==null)
			return false;
		return recipeList.containsKey(ApiUtils.createComparableItemStack(itemStack));
	}
}