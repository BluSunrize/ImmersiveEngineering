/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * @author BluSunrize - 02.02.2016
 * <br>
 * An interface implemented by recipes that can be handled by IE's Metal Multiblocks. <br>
 * This is only used by IE's own machines, it's just in the API because recipes have to implement it.
 */
public interface IMultiblockRecipe
{
	List<IngredientStack> getItemInputs();

	default boolean shouldCheckItemAvailability()
	{
		return true;
	}

	List<FluidStack> getFluidInputs();

	NonNullList<ItemStack> getItemOutputs();

	default NonNullList<ItemStack> getActualItemOutputs(TileEntity tile)
	{
		return getItemOutputs();
	}

	List<FluidStack> getFluidOutputs();

	default ItemStack getDisplayStack(ItemStack input)
	{
		for(IngredientStack ingr : getItemInputs())
			if(ingr.matchesItemStack(input))
				return Utils.copyStackWithAmount(input, ingr.inputSize);
		return ItemStack.EMPTY;
	}

	default List<FluidStack> getActualFluidOutputs(TileEntity tile)
	{
		return getFluidOutputs();
	}

	int getTotalProcessTime();

	int getTotalProcessEnergy();

	int getMultipleProcessTicks();

	NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound);
}
