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
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

class ItemTags extends ItemTagsProvider
{

	public ItemTags(DataGenerator gen, BlockTagsProvider blocks, ExistingFileHelper existingFileHelper)
	{
		super(gen, blocks, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags()
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
				tag(tags.ingot).add(ingot);
				tag(Tags.Items.INGOTS).add(ingot);
				tag(tags.nugget).add(nugget);
				tag(Tags.Items.NUGGETS).add(nugget);
				tag(Tags.Items.STORAGE_BLOCKS).add(IEBlocks.Metals.storage.get(metal).asItem());
				if(metal.shouldAddOre())
					tag(Tags.Items.ORES).add(IEBlocks.Metals.ores.get(metal).asItem());
			}
			tag(tags.plate).add(plate);
			tag(IETags.plates).add(plate);
			tag(tags.dust).add(dust);
			tag(Tags.Items.DUSTS).add(dust);
		}

		IETags.forAllBlocktags(this::copy);

		tag(IETags.clay).add(Items.CLAY_BALL);
		tag(IETags.charCoal).add(Items.CHARCOAL);

		tag(net.minecraft.tags.ItemTags.LECTERN_BOOKS).add(Tools.manual);
		tag(Tags.Items.SEEDS).add(Misc.hempSeeds);
		tag(Tags.Items.RODS_WOODEN).add(Ingredients.stickTreated);
		tag(IETags.treatedStick).add(Ingredients.stickTreated);
		tag(IETags.slag).add(Ingredients.slag);
		tag(IETags.ironRod).add(Ingredients.stickIron);
		tag(IETags.steelRod).add(Ingredients.stickSteel);
		tag(IETags.aluminumRod).add(Ingredients.stickAluminum);
		tag(IETags.fiberHemp).add(Ingredients.hempFiber);
		tag(IETags.fabricHemp).add(Ingredients.hempFabric);
		tag(IETags.coalCoke).add(Ingredients.coalCoke);
		tag(IETags.coalCokeDust).add(Ingredients.dustCoke);
		tag(IETags.hopGraphiteDust).add(Ingredients.dustHopGraphite);
		tag(IETags.hopGraphiteIngot).add(Ingredients.ingotHopGraphite);
		tag(IETags.copperWire).add(Ingredients.wireCopper);
		tag(IETags.electrumWire).add(Ingredients.wireElectrum);
		tag(IETags.aluminumWire).add(Ingredients.wireAluminum);
		tag(IETags.steelWire).add(Ingredients.wireSteel);
		tag(IETags.leadWire).add(Ingredients.wireLead);
		tag(IETags.allWires).addTag(IETags.copperWire)
				.addTag(IETags.electrumWire)
				.addTag(IETags.aluminumWire)
				.addTag(IETags.steelWire)
				.addTag(IETags.leadWire);
		tag(IETags.saltpeterDust).add(Ingredients.dustSaltpeter);
		tag(IETags.sulfurDust).add(Ingredients.dustSulfur);
		tag(IETags.sawdust).add(Ingredients.dustWood);
		tag(IETags.metalRods)
				.addTag(IETags.aluminumRod)
				.addTag(IETags.ironRod)
				.addTag(IETags.steelRod);
		tag(IETags.sawblades).add(Tools.sawblade);
		tag(IETags.forbiddenInCrates).add(Tools.toolbox)
				.add(WoodenDevices.crate.asItem())
				.add(WoodenDevices.reinforcedCrate.asItem())
				.add(Misc.cartWoodenCrate)
				.add(Misc.cartReinforcedCrate);
		tag(IETags.circuitPCB).add(Ingredients.circuitBoard);
		tag(IETags.circuitLogic).add(Ingredients.electronTube);
		tag(IETags.circuitSolder).addTag(IETags.copperWire).addTag(IETags.leadWire);

		/* MOD COMPAT STARTS HERE */

		// Curios
		tag(TagUtils.createItemWrapper(new ResourceLocation("curios:back")))
				.add(Misc.powerpack);
		tag(TagUtils.createItemWrapper(new ResourceLocation("curios:head")))
				.add(Misc.earmuffs);
	}
}
