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
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

class ItemTags extends ItemTagsProvider
{

	public ItemTags(DataGenerator gen, BlockTagsProvider blocks, ExistingFileHelper existingFileHelper)
	{
		super(gen, blocks, Lib.MODID, existingFileHelper);
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
				getOrCreateBuilder(tags.ingot).addItemEntry(ingot);
				getOrCreateBuilder(Tags.Items.INGOTS).addItemEntry(ingot);
				getOrCreateBuilder(tags.nugget).addItemEntry(nugget);
				getOrCreateBuilder(Tags.Items.NUGGETS).addItemEntry(nugget);
				getOrCreateBuilder(Tags.Items.STORAGE_BLOCKS).addItemEntry(IEBlocks.Metals.storage.get(metal).asItem());
				if(metal.shouldAddOre())
					getOrCreateBuilder(Tags.Items.ORES).addItemEntry(IEBlocks.Metals.ores.get(metal).asItem());
			}
			getOrCreateBuilder(tags.plate).addItemEntry(plate);
			getOrCreateBuilder(IETags.plates).addItemEntry(plate);
			getOrCreateBuilder(tags.dust).addItemEntry(dust);
			getOrCreateBuilder(Tags.Items.DUSTS).addItemEntry(dust);
		}

		IETags.forAllBlocktags(this::copy);

		getOrCreateBuilder(IETags.clay).addItemEntry(Items.CLAY_BALL);
		getOrCreateBuilder(IETags.charCoal).addItemEntry(Items.CHARCOAL);

		getOrCreateBuilder(Tags.Items.SEEDS).addItemEntry(Misc.hempSeeds);
		getOrCreateBuilder(Tags.Items.RODS_WOODEN).addItemEntry(Ingredients.stickTreated);
		getOrCreateBuilder(IETags.treatedStick).addItemEntry(Ingredients.stickTreated);
		getOrCreateBuilder(IETags.slag).addItemEntry(Ingredients.slag);
		getOrCreateBuilder(IETags.ironRod).addItemEntry(Ingredients.stickIron);
		getOrCreateBuilder(IETags.steelRod).addItemEntry(Ingredients.stickSteel);
		getOrCreateBuilder(IETags.aluminumRod).addItemEntry(Ingredients.stickAluminum);
		getOrCreateBuilder(IETags.fiberHemp).addItemEntry(Ingredients.hempFiber);
		getOrCreateBuilder(IETags.fabricHemp).addItemEntry(Ingredients.hempFabric);
		getOrCreateBuilder(IETags.coalCoke).addItemEntry(Ingredients.coalCoke);
		getOrCreateBuilder(IETags.coalCokeDust).addItemEntry(Ingredients.dustCoke);
		getOrCreateBuilder(IETags.hopGraphiteDust).addItemEntry(Ingredients.dustHopGraphite);
		getOrCreateBuilder(IETags.hopGraphiteIngot).addItemEntry(Ingredients.ingotHopGraphite);
		getOrCreateBuilder(IETags.copperWire).addItemEntry(Ingredients.wireCopper);
		getOrCreateBuilder(IETags.electrumWire).addItemEntry(Ingredients.wireElectrum);
		getOrCreateBuilder(IETags.aluminumWire).addItemEntry(Ingredients.wireAluminum);
		getOrCreateBuilder(IETags.steelWire).addItemEntry(Ingredients.wireSteel);
		getOrCreateBuilder(IETags.saltpeterDust).addItemEntry(Ingredients.dustSaltpeter);
		getOrCreateBuilder(IETags.sulfurDust).addItemEntry(Ingredients.dustSulfur);
		getOrCreateBuilder(IETags.sawdust).addItemEntry(Ingredients.dustWood);
		getOrCreateBuilder(IETags.metalRods)
				.addTag(IETags.aluminumRod)
				.addTag(IETags.ironRod)
				.addTag(IETags.steelRod);
		getOrCreateBuilder(IETags.sawblades).addItemEntry(Tools.sawblade);
		getOrCreateBuilder(IETags.forbiddenInCrates).addItemEntry(Tools.toolbox)
				.addItemEntry(WoodenDevices.crate.asItem())
				.addItemEntry(WoodenDevices.reinforcedCrate.asItem())
				.addItemEntry(Misc.cartWoodenCrate)
				.addItemEntry(Misc.cartReinforcedCrate);
	}
}
