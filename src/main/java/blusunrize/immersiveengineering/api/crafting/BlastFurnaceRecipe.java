/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the blast furnace
 */
public class BlastFurnaceRecipe
{
	public final Object input;
	public final ItemStack output;
	@Nonnull
	public final ItemStack slag;
	public final int time;

	public BlastFurnaceRecipe(ItemStack output, Object input, int time, @Nonnull ItemStack slag)
	{
		this.output = output;
		this.input = ApiUtils.convertToValidRecipeInput(input);
		this.time = time;
		this.slag = slag;
	}

	public static ArrayList<BlastFurnaceRecipe> recipeList = new ArrayList<BlastFurnaceRecipe>();

	public static void addRecipe(ItemStack output, Object input, int time, @Nonnull ItemStack slag)
	{
		BlastFurnaceRecipe recipe = new BlastFurnaceRecipe(output, input, time, slag);
		if(recipe.input!=null)
			recipeList.add(recipe);
	}

	public static BlastFurnaceRecipe findRecipe(ItemStack input)
	{
		for(BlastFurnaceRecipe recipe : recipeList)
		{
			if(ApiUtils.stackMatchesObject(input, recipe.input))
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

	public static ArrayList<BlastFurnaceFuel> blastFuels = new ArrayList();

	public static class BlastFurnaceFuel
	{
		public final IngredientStack input;
		public final int burnTime;

		public BlastFurnaceFuel(IngredientStack input, int burnTime)
		{
			this.input = input;
			this.burnTime = burnTime;
		}
	}

	public static BlastFurnaceFuel addBlastFuel(Object fuel, int burnTime)
	{
		BlastFurnaceFuel entry = new BlastFurnaceFuel(ApiUtils.createIngredientStack(fuel), burnTime);
		blastFuels.add(entry);
		return entry;
	}

	public static int getBlastFuelTime(ItemStack stack)
	{
		for(BlastFurnaceFuel e : blastFuels)
			if(e.input.matchesItemStack(stack))
				return e.burnTime;
		return 0;
	}

	public static boolean isValidBlastFuel(ItemStack stack)
	{
		return getBlastFuelTime(stack) > 0;
	}
}