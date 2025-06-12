/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags.Blocks;
import net.neoforged.neoforge.common.Tags.Items;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;
import static blusunrize.immersiveengineering.api.utils.TagUtils.*;

public class IETags
{

	private static final Map<TagKey<Block>, TagKey<Item>> toItemTag = new HashMap<>();
	private static final Map<EnumMetals, MetalTags> metals = new EnumMap<>(EnumMetals.class);

	static
	{
		toItemTag.put(Blocks.STORAGE_BLOCKS, Items.STORAGE_BLOCKS);
		toItemTag.put(Blocks.ORES, Items.ORES);
		toItemTag.put(Blocks.ORES_IN_GROUND_STONE, Items.ORES_IN_GROUND_STONE);
		toItemTag.put(Blocks.ORES_IN_GROUND_DEEPSLATE, Items.ORES_IN_GROUND_DEEPSLATE);
		toItemTag.put(Blocks.ORE_RATES_SINGULAR, Items.ORE_RATES_SINGULAR);
		toItemTag.put(BlockTags.PLANKS, ItemTags.PLANKS);
		toItemTag.put(Blocks.GRAVELS, Items.GRAVELS);
		toItemTag.put(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
		toItemTag.put(BlockTags.SLABS, ItemTags.SLABS);
		toItemTag.put(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
		toItemTag.put(BlockTags.STAIRS, ItemTags.STAIRS);
		toItemTag.put(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
		toItemTag.put(BlockTags.FENCES, ItemTags.FENCES);
		toItemTag.put(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
		toItemTag.put(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
		toItemTag.put(BlockTags.DOORS, ItemTags.DOORS);
		toItemTag.put(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
		toItemTag.put(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
	}

	//Vanilla
	public static final TagKey<Item> clay = createItemWrapper(cLoc("clay"));
	public static final TagKey<Block> clayBlock = createBlockTag(getStorageBlock("clay"));
	public static final TagKey<Item> charCoal = createItemWrapper(cLoc("charcoal"));
	public static final TagKey<Item> paper = createItemWrapper(cLoc("paper"));
	public static final TagKey<Block> glowstoneBlock = createBlockTag(getStorageBlock("glowstone"));
	public static final TagKey<Block> copperBlocks = createBlockTag(ieLoc("blocks/copper"));
	public static final TagKey<Block> cutCopperBlocks = createBlockTag(ieLoc("cut_blocks/copper"));

	public static final TagKey<Block> cutCopperStairs = createBlockTag(ieLoc("cut_stairs/copper"));
	public static final TagKey<Block> cutCopperSlabs = createBlockTag(ieLoc("cut_slabs/copper"));
	//IE Blocks
	public static final TagKey<Block> treatedWood = createBlockTag(ieLoc("treated_wood"));
	public static final TagKey<Block> treatedWoodSlab = createBlockTag(ieLoc("treated_wood_slab"));
	public static final TagKey<Block> coalCokeBlock = createBlockTag(getStorageBlock("coal_coke"));
	public static final TagKey<Block> scaffoldingSteel = createBlockTag(ieLoc("scaffoldings/steel"));
	public static final TagKey<Block> scaffoldingAlu = createBlockTag(ieLoc("scaffoldings/aluminum"));
	public static final TagKey<Block> scaffoldingSteelStair = createBlockTag(ieLoc("scaffolding_stairs/steel"));
	public static final TagKey<Block> scaffoldingAluStair = createBlockTag(ieLoc("scaffolding_stairs/aluminum"));
	public static final TagKey<Block> scaffoldingSteelSlab = createBlockTag(ieLoc("scaffolding_slabs/steel"));
	public static final TagKey<Block> scaffoldingAluSlab = createBlockTag(ieLoc("scaffolding_slabs/aluminum"));
	public static final TagKey<Block> sheetmetals = createBlockTag(cLoc("sheetmetals"));
	public static final TagKey<Block> sheetmetalSlabs = createBlockTag(cLoc("sheetmetal_slabs"));
	public static final TagKey<Block> fencesSteel = createBlockTag(cLoc("fences/steel"));
	public static final TagKey<Block> fencesAlu = createBlockTag(cLoc("fences/aluminum"));
	//IE Items
	public static final TagKey<Item> treatedStick = createItemWrapper(getRod("treated_wood"));
	public static final TagKey<Item> ironRod = createItemWrapper(getRod("iron"));
	public static final TagKey<Item> steelRod = createItemWrapper(getRod("steel"));
	public static final TagKey<Item> metalRods = createItemWrapper(getRod("all_metal"));
	public static final TagKey<Item> aluminumRod = createItemWrapper(getRod("aluminum"));
	public static final TagKey<Item> netheriteRod = createItemWrapper(getRod("netherite"));
	public static final TagKey<Item> netheriteNugget = createItemWrapper(getNugget("netherite"));
	public static final TagKey<Item> seedsHemp = createItemWrapper(cLoc("seeds/hemp"));
	public static final TagKey<Item> fiberHemp = createItemWrapper(cLoc("fiber_hemp"));
	public static final TagKey<Item> fabricHemp = createItemWrapper(cLoc("fabric_hemp"));
	public static final TagKey<Item> coalCoke = createItemWrapper(cLoc("coal_coke"));
	public static final TagKey<Item> slag = createItemWrapper(cLoc("slag"));
	public static final TagKey<Item> coalCokeDust = createItemWrapper(getDust("coal_coke"));
	public static final TagKey<Item> hopGraphiteDust = createItemWrapper(getDust("hop_graphite"));
	public static final TagKey<Item> hopGraphiteIngot = createItemWrapper(getIngot("hop_graphite"));
	public static final TagKey<Item> hopGraphitePlate = createItemWrapper(getPlate("hop_graphite"));
	public static final TagKey<Item> copperWire = createItemWrapper(getWire("copper"));
	public static final TagKey<Item> electrumWire = createItemWrapper(getWire("electrum"));
	public static final TagKey<Item> aluminumWire = createItemWrapper(getWire("aluminum"));
	public static final TagKey<Item> steelWire = createItemWrapper(getWire("steel"));
	public static final TagKey<Item> leadWire = createItemWrapper(getWire("lead"));
	public static final TagKey<Item> allWires = createItemWrapper(cLoc("wires"));
	public static final TagKey<Item> saltpeterDust = createItemWrapper(getDust("saltpeter"));
	public static final TagKey<Item> sulfurDust = createItemWrapper(getDust("sulfur"));
	public static final TagKey<Item> sawdust = createItemWrapper(getDust("wood"));
	public static final TagKey<Item> plates = createItemWrapper(cLoc("plates"));
	public static final TagKey<Item> plasticPlate = createItemWrapper(getPlate("plastic"));
	public static final TagKey<Item> sawblades = createItemWrapper(cLoc("sawblades"));
	//Utility tags
	public static final TagKey<Item> forbiddenInCrates = createItemWrapper(ieLoc("forbidden_in_crates"));
	public static final TagKey<Item> circuitPCB = createItemWrapper(ieLoc("circuits/pcb"));
	public static final TagKey<Item> circuitLogic = createItemWrapper(ieLoc("circuits/logic"));
	public static final TagKey<Item> circuitSolder = createItemWrapper(ieLoc("circuits/solder"));
	public static final TagKey<Item> hammers = createItemWrapper(ieLoc("tools/hammers"));
	public static final TagKey<Item> screwdrivers = createItemWrapper(ieLoc("tools/screwdrivers"));
	public static final TagKey<Item> wirecutters = createItemWrapper(ieLoc("tools/wirecutters"));
	public static final TagKey<Item> toolboxTools = createItemWrapper(ieLoc("toolbox/tools"));
	public static final TagKey<Item> toolboxFood = createItemWrapper(ieLoc("toolbox/food"));
	public static final TagKey<Item> toolboxWiring = createItemWrapper(ieLoc("toolbox/wiring"));
	public static final TagKey<Item> connectorInsulator = createItemWrapper(ieLoc("connector_insulator"));;
	public static final TagKey<Item> repairsAutomaton = createItemWrapper(ieLoc("repairs_automaton"));
	public static final TagKey<Block> hammerHarvestable = createBlockWrapper(ieLoc("mineable/hammer"));
	public static final TagKey<Block> wirecutterHarvestable = createBlockWrapper(ieLoc("mineable/wirecutter"));
	public static final TagKey<Block> drillHarvestable = createBlockWrapper(ieLoc("mineable/drill"));
	public static final TagKey<Block> rockcutterHarvestable = createBlockWrapper(ieLoc("mineable/rockcutter"));
	public static final TagKey<Block> grindingDiskHarvestable = createBlockWrapper(ieLoc("mineable/grinding_disk"));
	public static final TagKey<Block> surveyToolTargets = createBlockWrapper(ieLoc("survey_tool_targets"));
	public static final TagKey<Block> concreteForFeet = createBlockWrapper(ieLoc("concrete_for_concrete_feet"));
	public static final TagKey<Block> incorrectDropsSteel = createBlockWrapper(ieLoc("incorrect_for_steel_tool"));
	public static final TagKey<Block> teleportBlocking = createBlockWrapper(ieLoc("teleport_blocking"));

	public static final TagKey<Block> buzzsawTreeBlacklist = createBlockWrapper(ieLoc("buzzsaw/tree_blacklist"));
	public static final TagKey<Item> powerpackForbidAttach = createItemWrapper(ieLoc("powerpack/forbid_attach"));

	public static final TagKey<Item> recyclingIgnoredComponents = createItemWrapper(ieLoc("recycling/ignored_components"));
	public static final TagKey<Item> recyclingWhitelist = createItemWrapper(ieLoc("recycling/whitelist"));
	public static final TagKey<Item> recyclingBlacklist = createItemWrapper(ieLoc("recycling/blacklist"));

	public static final TagKey<Fluid> fluidCreosote = createFluidWrapper(cLoc("creosote"));
	public static final TagKey<Fluid> fluidPlantoil = createFluidWrapper(cLoc("plantoil"));
	public static final TagKey<Fluid> fluidEthanol = createFluidWrapper(cLoc("ethanol"));
	public static final TagKey<Fluid> fluidBiodiesel = createFluidWrapper(cLoc("biodiesel"));
	public static final TagKey<Fluid> fluidHighPowerBiodiesel = createFluidWrapper(cLoc("high_power_biodiesel"));
	public static final TagKey<Fluid> fluidConcrete = createFluidWrapper(cLoc("concrete"));
	public static final TagKey<Fluid> fluidHerbicide = createFluidWrapper(cLoc("herbicide"));
	public static final TagKey<Fluid> fluidRedstoneAcid = createFluidWrapper(cLoc("redstone_acid"));
	public static final TagKey<Fluid> fluidPotion = createFluidWrapper(cLoc("potion"));
	public static final TagKey<Fluid> fluidAcetaldehyde = createFluidWrapper(cLoc("acetaldehyde"));
	public static final TagKey<Fluid> fluidResin = createFluidWrapper(cLoc("phenolic_resin"));
	public static final TagKey<Fluid> drillFuel = createFluidWrapper(ieLoc("drill_fuel"));

	public static final TagKey<EntityType<?>> shaderbagWhitelist = createEntityWrapper(ieLoc("shaderbag/whitelist"));
	public static final TagKey<EntityType<?>> shaderbagBlacklist = createEntityWrapper(ieLoc("shaderbag/blacklist"));

	public static final TagKey<Biome> is_swamp = createBiomeWrapper(cLoc("is_swamp"));
	public static final TagKey<Biome> generateClaypan = createBiomeWrapper(ieLoc("generate_hardened_clay_pan"));
	public static final TagKey<Biome> generateSeabed = createBiomeWrapper(ieLoc("generate_ancient_seabed"));

	static
	{
		for(EnumMetals m : EnumMetals.values())
			metals.put(m, new MetalTags(m));
	}

	public static TagKey<Item> getItemTag(TagKey<Block> blockTag)
	{
		Preconditions.checkArgument(toItemTag.containsKey(blockTag));
		return toItemTag.get(blockTag);
	}

	public static MetalTags getTagsFor(EnumMetals metal)
	{
		return metals.get(metal);
	}

	private static TagKey<Block> createBlockTag(ResourceLocation name)
	{
		TagKey<Block> blockTag = createBlockWrapper(name);
		toItemTag.put(blockTag, createItemWrapper(name));
		return blockTag;
	}

	public static void forAllBlocktags(BiConsumer<TagKey<Block>, TagKey<Item>> out)
	{
		for(Entry<TagKey<Block>, TagKey<Item>> entry : toItemTag.entrySet())
			out.accept(entry.getKey(), entry.getValue());
	}

	public static class MetalTags
	{
		public final TagKey<Item> ingot;
		public final TagKey<Item> nugget;
		@Nullable
		public final TagKey<Item> rawOre;
		public final TagKey<Item> plate;
		public final TagKey<Item> dust;
		public final TagKey<Block> storage;
		public final TagKey<Block> sheetmetal;
		@Nullable
		public final TagKey<Block> ore;
		@Nullable
		public final TagKey<Block> rawBlock;

		private MetalTags(EnumMetals m)
		{
			String name = m.tagName();
			TagKey<Block> ore = null;
			TagKey<Item> rawOre = null;
			TagKey<Block> rawBlock = null;
			if(m.shouldAddOre())
			{
				ore = createBlockTag(getOre(name));
				rawOre = createItemWrapper(getRawOre(name));
				rawBlock = createBlockTag(getRawBlock(name));
			}
			if(!m.isVanillaMetal())
				storage = createBlockTag(getStorageBlock(name));
			else if(m==EnumMetals.COPPER)
			{
				storage = Blocks.STORAGE_BLOCKS_COPPER;
				ore = Blocks.ORES_COPPER;
				rawBlock = Blocks.STORAGE_BLOCKS_RAW_COPPER;
			}
			else if(m==EnumMetals.IRON)
			{
				storage = Blocks.STORAGE_BLOCKS_IRON;
				ore = Blocks.ORES_IRON;
				rawBlock = Blocks.STORAGE_BLOCKS_RAW_IRON;
			}
			else if(m==EnumMetals.GOLD)
			{
				storage = Blocks.STORAGE_BLOCKS_GOLD;
				ore = Blocks.ORES_GOLD;
				rawBlock = Blocks.STORAGE_BLOCKS_RAW_GOLD;
			}
			else
				throw new RuntimeException("Unkown vanilla metal: "+m.name());
			sheetmetal = createBlockTag(getSheetmetalBlock(name));
			nugget = createItemWrapper(getNugget(name));
			ingot = createItemWrapper(getIngot(name));
			plate = createItemWrapper(getPlate(name));
			dust = createItemWrapper(getDust(name));
			this.ore = ore;
			this.rawOre = rawOre;
			this.rawBlock = rawBlock;
		}
	}

	// TODO adjust *all* of these!
	private static ResourceLocation neoLoc(String path)
	{
		return ResourceLocation.fromNamespaceAndPath("neoforge", path);
	}

	private static ResourceLocation cLoc(String path)
	{
		return ResourceLocation.fromNamespaceAndPath("c", path);
	}

	public static ResourceLocation getOre(String type)
	{
		return cLoc("ores/"+type);
	}

	public static ResourceLocation getRawOre(String type)
	{
		return cLoc("raw_materials/"+type);
	}

	public static ResourceLocation getNugget(String type)
	{
		return cLoc("nuggets/"+type);
	}

	public static ResourceLocation getIngot(String type)
	{
		return cLoc("ingots/"+type);
	}

	public static ResourceLocation getGem(String type)
	{
		return cLoc("gems/"+type);
	}

	public static ResourceLocation getStorageBlock(String type)
	{
		return cLoc("storage_blocks/"+type);
	}

	public static ResourceLocation getRawBlock(String type)
	{
		return getStorageBlock("raw_"+type);
	}

	public static ResourceLocation getDust(String type)
	{
		return cLoc("dusts/"+type);
	}

	public static ResourceLocation getPlate(String type)
	{
		return cLoc("plates/"+type);
	}

	public static ResourceLocation getRod(String type)
	{
		return cLoc("rods/"+type);
	}

	public static ResourceLocation getGear(String type)
	{
		return cLoc("gears/"+type);
	}

	public static ResourceLocation getWire(String type)
	{
		return cLoc("wires/"+type);
	}

	public static ResourceLocation getSheetmetalBlock(String type)
	{
		return cLoc("sheetmetals/"+type);
	}
}
