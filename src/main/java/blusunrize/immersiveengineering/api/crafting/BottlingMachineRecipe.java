package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 27.10.2015
 * <br>
 * The recipe for the bottling machine
 */
public class BottlingMachineRecipe
{
	public final Object input;
	public final FluidStack fluidInput;
	public final ItemStack output;

	public BottlingMachineRecipe(ItemStack output, Object input, FluidStack fluidInput)
	{
		this.output=output;
		this.input=ApiUtils.convertToValidRecipeInput(input);
		this.fluidInput=fluidInput;
	}

	public static ArrayList<BottlingMachineRecipe> recipeList = new ArrayList<BottlingMachineRecipe>();
	public static void addRecipe(ItemStack output, Object input, FluidStack fluidInput)
	{
		BottlingMachineRecipe recipe = new BottlingMachineRecipe(output, input, fluidInput);
		if(recipe.input!=null)
			recipeList.add(recipe);
	}
	public static BottlingMachineRecipe findRecipe(ItemStack input, FluidStack fluid)
	{
		if(input!=null && fluid!=null)
			for(BottlingMachineRecipe recipe : recipeList)
				if(ApiUtils.stackMatchesObject(input, recipe.input) && fluid.containsFluid(recipe.fluidInput))
					return recipe;
		return null;
	}
	public static List<BottlingMachineRecipe> removeRecipes(ItemStack stack)
	{
		List<BottlingMachineRecipe> list = new ArrayList();
		Iterator<BottlingMachineRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			BottlingMachineRecipe ir = it.next();
			if(OreDictionary.itemMatches(ir.output, stack, true))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}
}