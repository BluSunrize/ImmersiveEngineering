/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FermenterRecipeBuilder extends IEFinishedRecipe<FermenterRecipeBuilder>
{
	private FermenterRecipeBuilder()
	{
		super(FermenterRecipe.SERIALIZER.get());
	}

	public static FermenterRecipeBuilder builder(Fluid fluid, int amount)
	{
		return new FermenterRecipeBuilder().addFluid(fluid, amount);
	}

	public static FermenterRecipeBuilder builder(FluidStack fluidStack)
	{
		return new FermenterRecipeBuilder().addFluid(fluidStack);
	}

	public static FermenterRecipeBuilder builder()
	{
		return new FermenterRecipeBuilder();
	}
}
