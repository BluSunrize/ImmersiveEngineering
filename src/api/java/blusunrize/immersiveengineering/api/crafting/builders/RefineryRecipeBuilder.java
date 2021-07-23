/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.material.Fluid;
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

	public RefineryRecipeBuilder addInput(FluidTagInput fluidTag)
	{
		return addFluidTag(generateSafeInputKey(), fluidTag);
	}

	public RefineryRecipeBuilder addInput(Named<Fluid> fluidTag, int amount)
	{
		return addFluidTag(generateSafeInputKey(), fluidTag, amount);
	}


}
