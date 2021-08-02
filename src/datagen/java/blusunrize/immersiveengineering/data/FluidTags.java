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
import blusunrize.immersiveengineering.common.register.IEFluids;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

class FluidTags extends FluidTagsProvider
{
	public FluidTags(DataGenerator gen, ExistingFileHelper existingFileHelper)
	{
		super(gen, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags()
	{
		tag(IETags.fluidCreosote).add(IEFluids.fluidCreosote.getStill());
		tag(IETags.fluidPlantoil).add(IEFluids.fluidPlantoil.getStill());
		tag(IETags.fluidEthanol).add(IEFluids.fluidEthanol.getStill());
		tag(IETags.fluidBiodiesel).add(IEFluids.fluidBiodiesel.getStill());
		tag(IETags.fluidConcrete).add(IEFluids.fluidConcrete.getStill());
		tag(IETags.fluidHerbicide).add(IEFluids.fluidHerbicide.getStill());
		tag(IETags.fluidPotion).add(IEFluids.fluidPotion.get());
	}
}
