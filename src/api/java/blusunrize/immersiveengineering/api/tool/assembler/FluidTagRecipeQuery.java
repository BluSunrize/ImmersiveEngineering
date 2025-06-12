/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.assembler;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class FluidTagRecipeQuery extends RecipeQuery
{
	private final SizedFluidIngredient tag;

	public FluidTagRecipeQuery(SizedFluidIngredient stack)
	{
		this.tag = stack;
	}

	@Override
	public boolean matchesIgnoringSize(ItemStack stack)
	{
		return FluidUtil.getFluidContained(stack)
				.map(s -> tag.ingredient().test(s))
				.orElse(false);
	}

	@Override
	public boolean matchesFluid(FluidStack fluid)
	{
		return tag.test(fluid);
	}

	@Override
	public int getFluidSize()
	{
		return tag.amount();
	}

	@Override
	public int getItemCount()
	{
		return 1;
	}

	@Override
	public boolean isFluid()
	{
		return true;
	}
}
