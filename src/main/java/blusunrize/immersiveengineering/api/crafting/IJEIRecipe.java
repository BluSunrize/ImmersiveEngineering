/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IJEIRecipe
{
	default boolean listInJEI()
	{
		return true;
	}

	List<ItemStack> getJEITotalItemInputs();

	List<ItemStack> getJEITotalItemOutputs();

	List<FluidStack> getJEITotalFluidInputs();

	List<FluidStack> getJEITotalFluidOutputs();
}