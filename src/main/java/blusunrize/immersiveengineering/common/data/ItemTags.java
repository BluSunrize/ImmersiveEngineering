/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.IETags.MetalTags;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

class ItemTags extends ItemTagsProvider
{

	public ItemTags(DataGenerator gen, BlockTagsProvider blocks)
	{
		super(gen, blocks);
	}

	@Override
	protected void registerTags()
	{
		for(EnumMetals metal : EnumMetals.values())
		{
			Item nugget = Metals.nuggets.get(metal);
			Item ingot = Metals.ingots.get(metal);
			Item plate = Metals.plates.get(metal);
			Item dust = Metals.dusts.get(metal);
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				func_240522_a_(tags.ingot).func_240532_a_(ingot);
				func_240522_a_(Tags.Items.INGOTS).func_240532_a_(ingot);
				func_240522_a_(tags.nugget).func_240532_a_(nugget);
				func_240522_a_(Tags.Items.NUGGETS).func_240532_a_(nugget);
				func_240522_a_(Tags.Items.STORAGE_BLOCKS).func_240532_a_(IEBlocks.Metals.storage.get(metal).asItem());
				if(metal.shouldAddOre())
					func_240522_a_(Tags.Items.ORES).func_240532_a_(IEBlocks.Metals.ores.get(metal).asItem());
			}
			func_240522_a_(tags.plate).func_240532_a_(plate);
			func_240522_a_(IETags.plates).func_240532_a_(plate);
			func_240522_a_(tags.dust).func_240532_a_(dust);
			func_240522_a_(Tags.Items.DUSTS).func_240532_a_(dust);
		}

		IETags.forAllBlocktags(this::func_240521_a_);

		func_240522_a_(IETags.clay).func_240532_a_(Items.CLAY_BALL);
		func_240522_a_(IETags.charCoal).func_240532_a_(Items.CHARCOAL);

		func_240522_a_(Tags.Items.SEEDS).func_240532_a_(Misc.hempSeeds);
		func_240522_a_(Tags.Items.RODS_WOODEN).func_240532_a_(Ingredients.stickTreated);
		func_240522_a_(IETags.treatedStick).func_240532_a_(Ingredients.stickTreated);
		func_240522_a_(IETags.slag).func_240532_a_(Ingredients.slag);
		func_240522_a_(IETags.ironRod).func_240532_a_(Ingredients.stickIron);
		func_240522_a_(IETags.steelRod).func_240532_a_(Ingredients.stickSteel);
		func_240522_a_(IETags.aluminumRod).func_240532_a_(Ingredients.stickAluminum);
		func_240522_a_(IETags.fiberHemp).func_240532_a_(Ingredients.hempFiber);
		func_240522_a_(IETags.fabricHemp).func_240532_a_(Ingredients.hempFabric);
		func_240522_a_(IETags.coalCoke).func_240532_a_(Ingredients.coalCoke);
		func_240522_a_(IETags.coalCokeDust).func_240532_a_(Ingredients.dustCoke);
		func_240522_a_(IETags.hopGraphiteDust).func_240532_a_(Ingredients.dustHopGraphite);
		func_240522_a_(IETags.hopGraphiteIngot).func_240532_a_(Ingredients.ingotHopGraphite);
		func_240522_a_(IETags.copperWire).func_240532_a_(Ingredients.wireCopper);
		func_240522_a_(IETags.electrumWire).func_240532_a_(Ingredients.wireElectrum);
		func_240522_a_(IETags.aluminumWire).func_240532_a_(Ingredients.wireAluminum);
		func_240522_a_(IETags.steelWire).func_240532_a_(Ingredients.wireSteel);
		func_240522_a_(IETags.saltpeterDust).func_240532_a_(Ingredients.dustSaltpeter);
		func_240522_a_(IETags.sulfurDust).func_240532_a_(Ingredients.dustSulfur);
		func_240522_a_(IETags.metalRods)
				.func_240531_a_(IETags.aluminumRod)
				.func_240531_a_(IETags.ironRod)
				.func_240531_a_(IETags.steelRod);
	}
}
