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
import blusunrize.immersiveengineering.common.IEContent;
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
		tag(IETags.fluidCreosote).add(IEContent.fluidCreosote);
		tag(IETags.fluidPlantoil).add(IEContent.fluidPlantoil);
		tag(IETags.fluidEthanol).add(IEContent.fluidEthanol);
		tag(IETags.fluidBiodiesel).add(IEContent.fluidBiodiesel);
		tag(IETags.fluidConcrete).add(IEContent.fluidConcrete);
		tag(IETags.fluidHerbicide).add(IEContent.fluidHerbicide);
		tag(IETags.fluidPotion).add(IEContent.fluidPotion);
		tag(IETags.drillFuel).addTag(IETags.fluidBiodiesel);
	}
}
