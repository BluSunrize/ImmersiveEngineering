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
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEItems.*;
import blusunrize.immersiveengineering.data.resources.RecipeMetals;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

class IEItemTags extends ItemTagsProvider
{

	public IEItemTags(DataGenerator gen, BlockTagsProvider blocks, ExistingFileHelper existingFileHelper)
	{
		super(gen, blocks, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags()
	{
		IETags.forAllBlocktags(this::copy);
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(metal.shouldAddNugget())
			{
				tag(tags.nugget).add(Metals.NUGGETS.get(metal).get());
				tag(Tags.Items.NUGGETS).addTag(tags.nugget);
			}
			if(!metal.isVanillaMetal())
			{
				tag(tags.ingot).add(Metals.INGOTS.get(metal).get());
				tag(Tags.Items.INGOTS).addTag(tags.ingot);
				tag(Tags.Items.STORAGE_BLOCKS).addTag(IETags.getItemTag(tags.storage));
				if(metal.shouldAddOre())
					tag(Tags.Items.ORES).addTag(IETags.getItemTag(tags.ore));
			}
			if(metal==EnumMetals.COPPER)
			{
				//TODO Forge#7891
				tag(tags.ingot).add(Metals.INGOTS.get(metal).get());
				tag(Tags.Items.INGOTS).addTag(tags.ingot);
				tag(Tags.Items.STORAGE_BLOCKS).addTag(IETags.getItemTag(tags.storage));
				tag(Tags.Items.ORES).addTag(IETags.getItemTag(tags.ore));
			}
			tag(tags.plate).add(Metals.PLATES.get(metal).get());
			tag(IETags.plates).addTag(tags.plate);
			tag(tags.dust).add(Metals.DUSTS.get(metal).get());
			tag(Tags.Items.DUSTS).addTag(tags.dust);
		}
		//TODO Forge#7891
		tag(RecipeMetals.COPPER.getRawOre()).add(Items.RAW_COPPER);
		tag(RecipeMetals.IRON.getRawOre()).add(Items.RAW_IRON);
		tag(RecipeMetals.GOLD.getRawOre()).add(Items.RAW_GOLD);

		tag(IETags.clay).add(Items.CLAY_BALL);
		tag(IETags.charCoal).add(Items.CHARCOAL);

		tag(net.minecraft.tags.ItemTags.LECTERN_BOOKS).add(Tools.MANUAL.get());
		tag(Tags.Items.SEEDS).add(Misc.HEMP_SEEDS.get());
		tag(Tags.Items.RODS_WOODEN).add(Ingredients.STICK_TREATED.get());
		tag(IETags.treatedStick).add(Ingredients.STICK_TREATED.get());
		tag(IETags.slag).add(Ingredients.SLAG.get());
		tag(IETags.ironRod).add(Ingredients.STICK_IRON.get());
		tag(IETags.steelRod).add(Ingredients.STICK_STEEL.get());
		tag(IETags.aluminumRod).add(Ingredients.STICK_ALUMINUM.get());
		tag(IETags.fiberHemp).add(Ingredients.HEMP_FIBER.get());
		tag(IETags.fabricHemp).add(Ingredients.HEMP_FABRIC.get());
		tag(IETags.coalCoke).add(Ingredients.COAL_COKE.get());
		tag(IETags.coalCokeDust).add(Ingredients.DUST_COKE.get());
		tag(IETags.hopGraphiteDust).add(Ingredients.DUST_HOP_GRAPHITE.get());
		tag(IETags.hopGraphiteIngot).add(Ingredients.INGOT_HOP_GRAPHITE.get());
		tag(IETags.copperWire).add(Ingredients.WIRE_COPPER.get());
		tag(IETags.electrumWire).add(Ingredients.WIRE_ELECTRUM.get());
		tag(IETags.aluminumWire).add(Ingredients.WIRE_ALUMINUM.get());
		tag(IETags.steelWire).add(Ingredients.WIRE_STEEL.get());
		tag(IETags.leadWire).add(Ingredients.WIRE_LEAD.get());
		tag(IETags.allWires).addTag(IETags.copperWire)
				.addTag(IETags.electrumWire)
				.addTag(IETags.aluminumWire)
				.addTag(IETags.steelWire)
				.addTag(IETags.leadWire);
		tag(IETags.saltpeterDust).add(Ingredients.DUST_SALTPETER.get());
		tag(IETags.sulfurDust).add(Ingredients.DUST_SULFUR.get());
		tag(IETags.sawdust).add(Ingredients.DUST_WOOD.get());
		tag(IETags.metalRods)
				.addTag(IETags.aluminumRod)
				.addTag(IETags.ironRod)
				.addTag(IETags.steelRod);
		tag(IETags.sawblades).add(Tools.SAWBLADE.get());
		tag(IETags.forbiddenInCrates).add(Tools.TOOLBOX.get())
				.add(WoodenDevices.CRATE.asItem())
				.add(WoodenDevices.REINFORCED_CRATE.asItem())
				.add(Minecarts.CART_WOODEN_CRATE.asItem())
				.add(Minecarts.CART_REINFORCED_CRATE.asItem());
		tag(IETags.circuitPCB).add(Ingredients.CIRCUIT_BOARD.asItem());
		tag(IETags.circuitLogic).add(Ingredients.ELECTRON_TUBE.asItem());
		tag(IETags.circuitSolder).addTag(IETags.copperWire).addTag(IETags.leadWire);
		tag(IETags.hammers).add(Tools.HAMMER.get());
		tag(IETags.screwdrivers).add(Tools.SCREWDRIVER.get());
		tag(IETags.wirecutters).add(Tools.WIRECUTTER.get());

		//TODO Forge#7891
		tag(Tags.Items.ORES_GOLD).add(Items.DEEPSLATE_GOLD_ORE);
		tag(Tags.Items.ORES_IRON).add(Items.DEEPSLATE_IRON_ORE);
		tag(Tags.Items.ORES_COAL).add(Items.DEEPSLATE_COAL_ORE);
		tag(Tags.Items.ORES_LAPIS).add(Items.DEEPSLATE_LAPIS_ORE);
		tag(Tags.Items.ORES_DIAMOND).add(Items.DEEPSLATE_DIAMOND_ORE);
		tag(Tags.Items.ORES_REDSTONE).add(Items.DEEPSLATE_REDSTONE_ORE);
		tag(Tags.Items.ORES_EMERALD).add(Items.DEEPSLATE_EMERALD_ORE);

		/* MOD COMPAT STARTS HERE */

		// Curios
		tag(TagUtils.createItemWrapper(new ResourceLocation("curios:back")))
				.add(Misc.POWERPACK.asItem());
		tag(TagUtils.createItemWrapper(new ResourceLocation("curios:head")))
				.add(Misc.EARMUFFS.asItem());
	}
}
