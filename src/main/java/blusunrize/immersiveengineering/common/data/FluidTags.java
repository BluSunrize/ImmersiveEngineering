/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;

class FluidTags extends FluidTagsProvider
{
	public FluidTags(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerTags()
	{
		func_240522_a_(IETags.fluidCreosote).func_240532_a_(IEContent.fluidCreosote);
		func_240522_a_(IETags.fluidPlantoil).func_240532_a_(IEContent.fluidPlantoil);
		func_240522_a_(IETags.fluidEthanol).func_240532_a_(IEContent.fluidEthanol);
		func_240522_a_(IETags.fluidBiodiesel).func_240532_a_(IEContent.fluidBiodiesel);
		func_240522_a_(IETags.fluidConcrete).func_240532_a_(IEContent.fluidConcrete);
		func_240522_a_(IETags.fluidHerbicide).func_240532_a_(IEContent.fluidHerbicide);
		func_240522_a_(IETags.fluidPotion).func_240532_a_(IEContent.fluidPotion);
	}
}
