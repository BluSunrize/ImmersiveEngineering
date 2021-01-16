/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

class FluidTags extends FluidTagsProvider
{
	public FluidTags(DataGenerator gen, ExistingFileHelper existingFileHelper)
	{
		super(gen, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void registerTags()
	{
		getOrCreateBuilder(IETags.fluidCreosote).add(IEContent.fluidCreosote);
		getOrCreateBuilder(IETags.fluidPlantoil).add(IEContent.fluidPlantoil);
		getOrCreateBuilder(IETags.fluidEthanol).add(IEContent.fluidEthanol);
		getOrCreateBuilder(IETags.fluidBiodiesel).add(IEContent.fluidBiodiesel);
		getOrCreateBuilder(IETags.fluidConcrete).add(IEContent.fluidConcrete);
		getOrCreateBuilder(IETags.fluidHerbicide).add(IEContent.fluidHerbicide);
		getOrCreateBuilder(IETags.fluidPotion).add(IEContent.fluidPotion);
	}
}
