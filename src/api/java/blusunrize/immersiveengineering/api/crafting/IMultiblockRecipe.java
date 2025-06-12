/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

/**
 * @author BluSunrize - 02.02.2016
 * <br>
 * An interface implemented by recipes that can be handled by IE's Metal Multiblocks. <br>
 * This is only used by IE's own machines, it's just in the API because recipes have to implement it.
 */
public interface IMultiblockRecipe
{
	List<IngredientWithSize> getItemInputs();

	default boolean shouldCheckItemAvailability()
	{
		return true;
	}

	List<SizedFluidIngredient> getFluidInputs();

	NonNullList<ItemStack> getItemOutputs();

	default NonNullList<ItemStack> getActualItemOutputs()
	{
		return getItemOutputs();
	}

	List<FluidStack> getFluidOutputs();

	default ItemStack getDisplayStack(ItemStack input)
	{
		for(IngredientWithSize ingr : getItemInputs())
			if(ingr.test(input))
			{
				if(ingr.hasNoMatchingItems())
					return input;
				else
					return ingr.getMatchingStacks()[0];
			}
		return ItemStack.EMPTY;
	}

	default List<FluidStack> getActualFluidOutputs()
	{
		return getFluidOutputs();
	}

	int getTotalProcessTime();

	int getTotalProcessEnergy();

	int getMultipleProcessTicks();
}
