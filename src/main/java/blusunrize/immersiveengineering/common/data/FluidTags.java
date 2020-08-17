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
		getBuilder(IETags.fluidCreosote).add(IEContent.fluidCreosote);
		getBuilder(IETags.fluidPlantoil).add(IEContent.fluidPlantoil);
		getBuilder(IETags.fluidEthanol).add(IEContent.fluidEthanol);
		getBuilder(IETags.fluidBiodiesel).add(IEContent.fluidBiodiesel);
		getBuilder(IETags.fluidConcrete).add(IEContent.fluidConcrete);
		getBuilder(IETags.fluidHerbicide).add(IEContent.fluidHerbicide);
		getBuilder(IETags.fluidPotion).add(IEContent.fluidPotion);
	}
}
