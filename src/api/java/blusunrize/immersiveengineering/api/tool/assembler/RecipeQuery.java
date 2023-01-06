/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.assembler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public abstract class RecipeQuery
{
	public abstract boolean matchesIgnoringSize(ItemStack stack);

	public abstract boolean matchesFluid(FluidStack fluid);

	public abstract int getFluidSize();

	public abstract int getItemCount();

	public abstract boolean isFluid();
}
