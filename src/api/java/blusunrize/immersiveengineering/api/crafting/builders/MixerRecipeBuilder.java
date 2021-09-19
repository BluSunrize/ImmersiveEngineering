/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class MixerRecipeBuilder extends IEFinishedRecipe<MixerRecipeBuilder>
{
	private MixerRecipeBuilder()
	{
		super(MixerRecipe.SERIALIZER.get());
		this.setUseInputArray(6);
	}

	public static MixerRecipeBuilder builder(Fluid fluid, int amount)
	{
		return builder(new FluidStack(fluid, amount));
	}

	public static MixerRecipeBuilder builder(FluidStack fluidStack)
	{
		return new MixerRecipeBuilder().addFluid("result", fluidStack);
	}

}
