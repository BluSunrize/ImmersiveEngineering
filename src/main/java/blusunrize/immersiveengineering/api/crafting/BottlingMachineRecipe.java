/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.util.ListUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 14.01.2016
 * <br>
 * The recipe for the bottling machine
 */
public class BottlingMachineRecipe extends MultiblockRecipe
{
	public final IngredientStack input;
	public final FluidStack fluidInput;
	public final ItemStack output;

	public BottlingMachineRecipe(ItemStack output, Object input, FluidStack fluidInput)
	{
		this.output = output;
		this.input = ApiUtils.createIngredientStack(input);
		this.fluidInput = fluidInput;

		this.inputList = Lists.newArrayList(this.input);
		this.fluidInputList = Lists.newArrayList(this.fluidInput);
		this.outputList = ListUtils.fromItem(this.output);
	}

	public static ArrayList<BottlingMachineRecipe> recipeList = new ArrayList<>();

	public static void addRecipe(ItemStack output, Object input, FluidStack fluidInput)
	{
		BottlingMachineRecipe recipe = new BottlingMachineRecipe(output, input, fluidInput);
		recipeList.add(recipe);
	}

	public static BottlingMachineRecipe findRecipe(ItemStack input, FluidStack fluid)
	{
		if(!input.isEmpty()&&!fluid.isEmpty())
			for(BottlingMachineRecipe recipe : recipeList)
				if(ApiUtils.stackMatchesObject(input, recipe.input)&&fluid.containsFluid(recipe.fluidInput))
					return recipe;
		return null;
	}

	public static List<BottlingMachineRecipe> removeRecipes(ItemStack stack)
	{
		List<BottlingMachineRecipe> list = new ArrayList<>();
		Iterator<BottlingMachineRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			BottlingMachineRecipe ir = it.next();
			if(ItemStack.areItemsEqual(ir.output, stack))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt)
	{
		nbt.put("input", input.writeToNBT(new CompoundNBT()));
		return nbt;
	}

	public static BottlingMachineRecipe loadFromNBT(CompoundNBT nbt)
	{
		IngredientStack input = IngredientStack.readFromNBT(nbt.getCompound("input"));
		for(BottlingMachineRecipe recipe : recipeList)
			if(recipe.input.equals(input))
				return recipe;
		return null;
	}
}