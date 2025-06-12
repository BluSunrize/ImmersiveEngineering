/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.tags;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.IETags.MetalTags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BasicConnectorBlock;
import blusunrize.immersiveengineering.common.items.WireCoilItem;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEBlocks.Cloth;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEItems.*;
import com.google.common.base.Preconditions;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class IEItemTags extends ItemTagsProvider
{

	public IEItemTags(
			PackOutput output,
			CompletableFuture<Provider> lookupProvider,
			CompletableFuture<TagLookup<Block>> blocks,
			ExistingFileHelper existingFileHelper
	)
	{
		super(output, lookupProvider, blocks, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider p_256380_)
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
			}
			if(metal.shouldAddOre())
			{
				Preconditions.checkNotNull(tags.rawOre);
				tag(tags.rawOre).add(Metals.RAW_ORES.get(metal).get());
				tag(Tags.Items.RAW_MATERIALS).addTag(tags.rawOre);
			}
			tag(tags.plate).add(Metals.PLATES.get(metal).get());
			tag(IETags.plates).addTag(tags.plate);
			tag(tags.dust).add(Metals.DUSTS.get(metal).get());
			tag(Tags.Items.DUSTS).addTag(tags.dust);
		}

		tag(IETags.clay).add(Items.CLAY_BALL);
		tag(IETags.charCoal).add(Items.CHARCOAL);
		tag(IETags.paper).add(Items.PAPER);

		tag(ItemTags.LECTERN_BOOKS).add(Tools.MANUAL.get());
		tag(ItemTags.BOOKSHELF_BOOKS).add(Tools.MANUAL.get());
		tag(Tags.Items.SEEDS).add(Misc.HEMP_SEEDS.get());
		tag(IETags.seedsHemp).add(Misc.HEMP_SEEDS.get());
		tag(Tags.Items.RODS_WOODEN).add(Ingredients.STICK_TREATED.get());
		tag(ItemTags.COALS).add(Ingredients.COAL_COKE.get());
		tag(Tags.Items.LEATHERS).add(Ingredients.ERSATZ_LEATHER.get());
		tag(IETags.treatedStick).add(Ingredients.STICK_TREATED.get());
		tag(IETags.slag).add(Ingredients.SLAG.get());
		tag(IETags.ironRod).add(Ingredients.STICK_IRON.get());
		tag(IETags.steelRod).add(Ingredients.STICK_STEEL.get());
		tag(IETags.aluminumRod).add(Ingredients.STICK_ALUMINUM.get());
		tag(IETags.netheriteRod).add(Ingredients.STICK_NETHERITE.get());
		tag(IETags.netheriteNugget).add(Ingredients.NUGGET_NETHERITE.asItem());
		tag(Tags.Items.NUGGETS).add(Ingredients.NUGGET_NETHERITE.asItem());
		tag(IETags.fiberHemp).add(Ingredients.HEMP_FIBER.get());
		tag(IETags.fabricHemp).add(Ingredients.HEMP_FABRIC.get());
		tag(IETags.coalCoke).add(Ingredients.COAL_COKE.get());
		tag(IETags.coalCokeDust).add(Ingredients.DUST_COKE.get());
		tag(IETags.hopGraphiteDust).add(Ingredients.DUST_HOP_GRAPHITE.get());
		tag(IETags.hopGraphiteIngot).add(Ingredients.INGOT_HOP_GRAPHITE.get());
		tag(Tags.Items.INGOTS).addTag(IETags.hopGraphiteIngot);
		tag(IETags.hopGraphitePlate).add(Ingredients.PLATE_HOP_GRAPHITE.get());
		tag(IETags.plates).addTag(IETags.hopGraphitePlate);
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
				.addTag(IETags.steelRod)
				.addTag(IETags.netheriteRod);
		tag(IETags.plasticPlate).add(Ingredients.DUROPLAST_PLATE.asItem());
		tag(IETags.sawblades).add(Tools.SAWBLADE.get());
		tag(IETags.circuitPCB).add(Ingredients.CIRCUIT_BOARD.asItem());
		tag(IETags.circuitLogic).add(Ingredients.ELECTRON_TUBE.asItem());
		tag(IETags.circuitSolder).addTag(IETags.copperWire).addTag(IETags.leadWire);
		tag(IETags.hammers).add(Tools.HAMMER.get());
		tag(IETags.screwdrivers).add(Tools.SCREWDRIVER.get());
		tag(IETags.wirecutters).add(Tools.WIRECUTTER.get());
		tag(IETags.connectorInsulator)
				.addTag(ItemTags.TERRACOTTA)
				.add(IEBlocks.StoneDecoration.DUROPLAST.asItem());
		tag(IETags.repairsAutomaton).addTag(IETags.plates);
		tag(ItemTags.CLUSTER_MAX_HARVESTABLES).add(Tools.STEEL_PICK.get());

		generateTagsForToolbox();
		tag(ItemTags.SHOVELS).add(Tools.STEEL_SHOVEL.get());
		tag(ItemTags.PICKAXES).add(Tools.STEEL_PICK.get());
		tag(ItemTags.HOES).add(Tools.STEEL_HOE.get());
		tag(ItemTags.AXES).add(Tools.STEEL_AXE.get());
		tag(ItemTags.SWORDS).add(Tools.STEEL_SWORD.get());
		tag(Tags.Items.TOOLS_SHIELD).add(Misc.SHIELD.get());

		for(var slot : ArmorItem.Type.values())
			if(slot!=Type.BODY)
			{
				tag(Tags.Items.ARMORS)
						.add(Tools.STEEL_ARMOR.get(slot).asItem())
						.add(Misc.FARADAY_SUIT.get(slot).asItem());
				tag(ItemTags.TRIMMABLE_ARMOR).add(Tools.STEEL_ARMOR.get(slot).asItem());
			}
		tag(ItemTags.HEAD_ARMOR)
				.add(Tools.STEEL_ARMOR.get(ArmorItem.Type.HELMET).asItem())
				.add(Misc.FARADAY_SUIT.get(ArmorItem.Type.HELMET).asItem());
		tag(ItemTags.CHEST_ARMOR)
				.add(Tools.STEEL_ARMOR.get(ArmorItem.Type.CHESTPLATE).asItem())
				.add(Misc.FARADAY_SUIT.get(ArmorItem.Type.CHESTPLATE).asItem());
		tag(ItemTags.LEG_ARMOR)
				.add(Tools.STEEL_ARMOR.get(ArmorItem.Type.LEGGINGS).asItem())
				.add(Misc.FARADAY_SUIT.get(ArmorItem.Type.LEGGINGS).asItem());
		tag(ItemTags.FOOT_ARMOR)
				.add(Tools.STEEL_ARMOR.get(ArmorItem.Type.BOOTS).asItem())
				.add(Misc.FARADAY_SUIT.get(ArmorItem.Type.BOOTS).asItem());

		tag(IETags.recyclingIgnoredComponents)
				// Ignore bricks for outputting
				.addTag(Tags.Items.BRICKS)
				.addTag(Tags.Items.BRICKS_NETHER)
				// Prevent tools used during crafting to be recycled as components
				.add(Tools.HAMMER.get())
				.add(Tools.SCREWDRIVER.get())
				.add(Tools.WIRECUTTER.get());

		/* MOD COMPAT STARTS HERE */

		// Curios
		tag(TagUtils.createItemWrapper(ResourceLocation.fromNamespaceAndPath("curios", "back")))
				.add(Misc.POWERPACK.asItem());
		tag(TagUtils.createItemWrapper(ResourceLocation.fromNamespaceAndPath("curios", "head")))
				.add(Misc.EARMUFFS.asItem());
	}

	private void generateTagsForToolbox()
	{
		tag(IETags.toolboxTools)
				.add(Weapons.RAILGUN.asItem())
				.add(Weapons.CHEMTHROWER.asItem())
				.add(Weapons.REVOLVER.asItem())
				.add(Weapons.SPEEDLOADER.asItem())
				.add(Tools.MANUAL.asItem())
				.add(Tools.WIRECUTTER.asItem())
				.add(Tools.BUZZSAW.asItem())
				.add(Tools.DRILL.asItem())
				.add(Tools.HAMMER.asItem())
				.add(Tools.SCREWDRIVER.asItem())
				.add(Tools.SURVEY_TOOLS.asItem())
				.add(Tools.VOLTMETER.asItem())
				.add(Misc.EARMUFFS.asItem())
				.add(Misc.SKYHOOK.asItem())
				.addTag(Tags.Items.TOOLS)
				.add(Items.SPYGLASS)
				.add(Items.CLOCK)
				.add(Items.COMPASS)
				.add(Items.FLINT_AND_STEEL)
				.add(Items.FISHING_ROD)
				.add(Items.BRUSH)
				.add(Items.RECOVERY_COMPASS)
				.add(Items.GOAT_HORN)
				.addOptionalTag(Tags.Items.BUCKETS_EMPTY)
				.addOptionalTag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/wrenches")))
		;
		for(ItemRegObject<WireCoilItem> wirecoil : Misc.WIRE_COILS.values())
			tag(IETags.toolboxWiring).add(wirecoil.asItem());
		for(BlockEntry<BasicConnectorBlock<?>> connector : Connectors.ENERGY_CONNECTORS.values())
			tag(IETags.toolboxWiring).add(connector.asItem());
		tag(IETags.toolboxWiring)
				.add(Connectors.CONNECTOR_STRUCTURAL.asItem())
				.add(Connectors.TRANSFORMER.asItem())
				.add(Connectors.POST_TRANSFORMER.asItem())
				.add(Connectors.TRANSFORMER_HV.asItem())
				.add(Connectors.BREAKER_SWITCH.asItem())
				.add(Connectors.REDSTONE_BREAKER.asItem())
				.add(Connectors.CURRENT_TRANSFORMER.asItem())
				.add(Connectors.CONNECTOR_REDSTONE.asItem())
				.add(Connectors.CONNECTOR_PROBE.asItem())
				.add(Connectors.CONNECTOR_BUNDLED.asItem())
				.add(Connectors.REDSTONE_STATE_CELL.asItem())
				.add(Cloth.BALLOON.asItem())
				.add(MetalDevices.RAZOR_WIRE.asItem());
	}
}
