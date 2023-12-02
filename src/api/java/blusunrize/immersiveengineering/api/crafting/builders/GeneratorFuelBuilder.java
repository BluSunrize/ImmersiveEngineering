/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class GeneratorFuelBuilder extends IEFinishedRecipe<GeneratorFuelBuilder>
{
	public static final String FLUID_TAG_KEY = "fluidTag";
	public static final String BURN_TIME_KEY = "burnTime";

	private GeneratorFuelBuilder(TagKey<Fluid> fluid, int burnTime)
	{
		super(GeneratorFuel.SERIALIZER.value());
		addWriter(obj -> obj.addProperty(FLUID_TAG_KEY, fluid.location().toString()));
		addWriter(obj -> obj.addProperty(BURN_TIME_KEY, burnTime));
	}

	public static GeneratorFuelBuilder builder(TagKey<Fluid> fluid, int burnTime)
	{
		return new GeneratorFuelBuilder(fluid, burnTime);
	}
}
