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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

class FluidTags extends FluidTagsProvider
{
	public FluidTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper)
	{
		super(output, lookupProvider, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider p_256366_)
	{
		tag(IETags.fluidCreosote).add(IEFluids.CREOSOTE.getStill());
		tag(IETags.fluidPlantoil).add(IEFluids.PLANTOIL.getStill());
		tag(IETags.fluidEthanol).add(IEFluids.ETHANOL.getStill());
		tag(IETags.fluidBiodiesel).add(IEFluids.BIODIESEL.getStill());
		tag(IETags.fluidConcrete).add(IEFluids.CONCRETE.getStill());
		tag(IETags.fluidHerbicide).add(IEFluids.HERBICIDE.getStill());
		tag(IETags.fluidRedstoneAcid).add(IEFluids.REDSTONE_ACID.getStill());
		tag(IETags.fluidAcetaldehyde).add(IEFluids.ACETALDEHYDE.getStill());
		tag(IETags.fluidResin).add(IEFluids.PHENOLIC_RESIN.getStill());
		tag(IETags.fluidPotion).add(IEFluids.POTION.get());

		tag(IETags.drillFuel).addTag(IETags.fluidBiodiesel);
		tag(Fluids.GASEOUS).add(IEFluids.ACETALDEHYDE.getStill());
	}
}
