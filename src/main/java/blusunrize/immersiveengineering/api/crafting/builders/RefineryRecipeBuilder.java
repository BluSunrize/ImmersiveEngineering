/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.FluidTagWithSize;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.Tag;
import net.minecraftforge.fluids.FluidStack;

public class RefineryRecipeBuilder extends IEFinishedRecipe<RefineryRecipeBuilder>
{
	private RefineryRecipeBuilder()
	{
		super(RefineryRecipe.SERIALIZER.get());
		this.maxInputCount = 2;
	}

	public static RefineryRecipeBuilder builder(Fluid fluid, int amount)
	{
		return builder(new FluidStack(fluid, amount));
	}

	public static RefineryRecipeBuilder builder(FluidStack fluidStack)
	{
		return new RefineryRecipeBuilder().addFluid("result", fluidStack);
	}

	public RefineryRecipeBuilder addInput(FluidTagWithSize fluidTag)
	{
		return addFluidTag(generateSafeInputKey(), fluidTag);
	}

	public RefineryRecipeBuilder addInput(Tag<Fluid> fluidTag, int amount)
	{
		return addFluidTag(generateSafeInputKey(), fluidTag, amount);
	}


}
