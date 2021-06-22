/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.fluids.IEFluids;
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
		getOrCreateBuilder(IETags.fluidCreosote).add(IEFluids.fluidCreosote.getStill());
		getOrCreateBuilder(IETags.fluidPlantoil).add(IEFluids.fluidPlantoil.getStill());
		getOrCreateBuilder(IETags.fluidEthanol).add(IEFluids.fluidEthanol.getStill());
		getOrCreateBuilder(IETags.fluidBiodiesel).add(IEFluids.fluidBiodiesel.getStill());
		getOrCreateBuilder(IETags.fluidConcrete).add(IEFluids.fluidConcrete.getStill());
		getOrCreateBuilder(IETags.fluidHerbicide).add(IEFluids.fluidHerbicide.getStill());
		getOrCreateBuilder(IETags.fluidPotion).add(IEFluids.fluidPotion.get());
	}
}
