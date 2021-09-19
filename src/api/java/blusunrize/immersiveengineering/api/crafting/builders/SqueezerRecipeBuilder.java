/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class SqueezerRecipeBuilder extends IEFinishedRecipe<SqueezerRecipeBuilder>
{
	private SqueezerRecipeBuilder()
	{
		super(SqueezerRecipe.SERIALIZER.get());
	}

	public static SqueezerRecipeBuilder builder(Fluid fluid, int amount)
	{
		return new SqueezerRecipeBuilder().addFluid(fluid, amount);
	}

	public static SqueezerRecipeBuilder builder(FluidStack fluidStack)
	{
		return new SqueezerRecipeBuilder().addFluid(fluidStack);
	}

	public static SqueezerRecipeBuilder builder()
	{
		return new SqueezerRecipeBuilder();
	}
}
