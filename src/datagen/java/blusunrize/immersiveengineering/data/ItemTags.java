/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.IETags.MetalTags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.items.IEItems.*;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
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
			Item nugget = Metals.nuggets.get(metal).get();
			Item ingot = Metals.ingots.get(metal).get();
			Item plate = Metals.plates.get(metal).get();
			Item dust = Metals.dusts.get(metal).get();
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

		getOrCreateBuilder(net.minecraft.tags.ItemTags.LECTERN_BOOKS).addItemEntry(Tools.manual.get());
		getOrCreateBuilder(Tags.Items.SEEDS).addItemEntry(Misc.hempSeeds.get());
		getOrCreateBuilder(Tags.Items.RODS_WOODEN).addItemEntry(Ingredients.stickTreated.get());
		getOrCreateBuilder(IETags.treatedStick).addItemEntry(Ingredients.stickTreated.get());
		getOrCreateBuilder(IETags.slag).addItemEntry(Ingredients.slag.get());
		getOrCreateBuilder(IETags.ironRod).addItemEntry(Ingredients.stickIron.get());
		getOrCreateBuilder(IETags.steelRod).addItemEntry(Ingredients.stickSteel.get());
		getOrCreateBuilder(IETags.aluminumRod).addItemEntry(Ingredients.stickAluminum.get());
		getOrCreateBuilder(IETags.fiberHemp).addItemEntry(Ingredients.hempFiber.get());
		getOrCreateBuilder(IETags.fabricHemp).addItemEntry(Ingredients.hempFabric.get());
		getOrCreateBuilder(IETags.coalCoke).addItemEntry(Ingredients.coalCoke.get());
		getOrCreateBuilder(IETags.coalCokeDust).addItemEntry(Ingredients.dustCoke.get());
		getOrCreateBuilder(IETags.hopGraphiteDust).addItemEntry(Ingredients.dustHopGraphite.get());
		getOrCreateBuilder(IETags.hopGraphiteIngot).addItemEntry(Ingredients.ingotHopGraphite.get());
		getOrCreateBuilder(IETags.copperWire).addItemEntry(Ingredients.wireCopper.get());
		getOrCreateBuilder(IETags.electrumWire).addItemEntry(Ingredients.wireElectrum.get());
		getOrCreateBuilder(IETags.aluminumWire).addItemEntry(Ingredients.wireAluminum.get());
		getOrCreateBuilder(IETags.steelWire).addItemEntry(Ingredients.wireSteel.get());
		getOrCreateBuilder(IETags.leadWire).addItemEntry(Ingredients.wireLead.get());
		getOrCreateBuilder(IETags.saltpeterDust).addItemEntry(Ingredients.dustSaltpeter.get());
		getOrCreateBuilder(IETags.sulfurDust).addItemEntry(Ingredients.dustSulfur.get());
		getOrCreateBuilder(IETags.sawdust).addItemEntry(Ingredients.dustWood.get());
		getOrCreateBuilder(IETags.metalRods)
				.addTag(IETags.aluminumRod)
				.addTag(IETags.ironRod)
				.addTag(IETags.steelRod);
		getOrCreateBuilder(IETags.sawblades).addItemEntry(Tools.sawblade.get());
		getOrCreateBuilder(IETags.forbiddenInCrates).addItemEntry(Tools.toolbox.get())
				.addItemEntry(WoodenDevices.crate.asItem())
				.addItemEntry(WoodenDevices.reinforcedCrate.asItem())
				.addItemEntry(Minecarts.cartWoodenCrate.asItem())
				.addItemEntry(Minecarts.cartReinforcedCrate.asItem());
		getOrCreateBuilder(IETags.circuitPCB).addItemEntry(Ingredients.circuitBoard.asItem());
		getOrCreateBuilder(IETags.circuitLogic).addItemEntry(Ingredients.electronTube.asItem());
		getOrCreateBuilder(IETags.circuitSolder).addTag(IETags.copperWire).addTag(IETags.leadWire);

		/* MOD COMPAT STARTS HERE */

		// Curios
		getOrCreateBuilder(TagUtils.createItemWrapper(new ResourceLocation("curios:back")))
				.addItemEntry(Misc.powerpack.asItem());
		getOrCreateBuilder(TagUtils.createItemWrapper(new ResourceLocation("curios:head")))
				.addItemEntry(Misc.earmuffs.asItem());
	}
}
