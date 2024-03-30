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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.Tags.Items;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

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
		toItemTag.put(Blocks.GRAVEL, Items.GRAVEL);
		toItemTag.put(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
		toItemTag.put(BlockTags.SLABS, ItemTags.SLABS);
		toItemTag.put(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
		toItemTag.put(BlockTags.STAIRS, ItemTags.STAIRS);
	}

	//Vanilla
	public static final TagKey<Item> clay = createItemWrapper(forgeLoc("clay"));
	public static final TagKey<Block> clayBlock = createBlockTag(getStorageBlock("clay"));
	public static final TagKey<Item> charCoal = createItemWrapper(forgeLoc("charcoal"));
	public static final TagKey<Block> glowstoneBlock = createBlockTag(getStorageBlock("glowstone"));
	public static final TagKey<Block> colorlessSandstoneBlocks = createBlockTag(forgeLoc("sandstone/colorless"));
	public static final TagKey<Block> redSandstoneBlocks = createBlockTag(forgeLoc("sandstone/red"));
	public static final TagKey<Item> cutCopperBlocks = createItemWrapper(rl("cut_blocks/copper"));
	public static final TagKey<Item> cutCopperStairs = createItemWrapper(rl("cut_stairs/copper"));
	public static final TagKey<Item> cutCopperSlabs = createItemWrapper(rl("cut_slabs/copper"));
	//IE Blocks
	public static final TagKey<Block> treatedWood = createBlockTag(forgeLoc("treated_wood"));
	public static final TagKey<Block> treatedWoodSlab = createBlockTag(forgeLoc("treated_wood_slab"));
	public static final TagKey<Block> coalCokeBlock = createBlockTag(getStorageBlock("coal_coke"));
	public static final TagKey<Block> scaffoldingSteel = createBlockTag(rl("scaffoldings/steel"));
	public static final TagKey<Block> scaffoldingAlu = createBlockTag(rl("scaffoldings/aluminum"));
	public static final TagKey<Block> scaffoldingSteelStair = createBlockTag(rl("scaffolding_stairs/steel"));
	public static final TagKey<Block> scaffoldingAluStair = createBlockTag(rl("scaffolding_stairs/aluminum"));
	public static final TagKey<Block> scaffoldingSteelSlab = createBlockTag(rl("scaffolding_slabs/steel"));
	public static final TagKey<Block> scaffoldingAluSlab = createBlockTag(rl("scaffolding_slabs/aluminum"));
	public static final TagKey<Block> sheetmetals = createBlockTag(forgeLoc("sheetmetals"));
	public static final TagKey<Block> sheetmetalSlabs = createBlockTag(forgeLoc("sheetmetal_slabs"));
	public static final TagKey<Block> fencesSteel = createBlockTag(forgeLoc("fences/steel"));
	public static final TagKey<Block> fencesAlu = createBlockTag(forgeLoc("fences/aluminum"));
	//IE Items
	public static final TagKey<Item> treatedStick = createItemWrapper(getRod("treated_wood"));
	public static final TagKey<Item> ironRod = createItemWrapper(getRod("iron"));
	public static final TagKey<Item> steelRod = createItemWrapper(getRod("steel"));
	public static final TagKey<Item> metalRods = createItemWrapper(getRod("all_metal"));
	public static final TagKey<Item> aluminumRod = createItemWrapper(getRod("aluminum"));
	public static final TagKey<Item> seedsHemp = createItemWrapper(forgeLoc("seeds/hemp"));
	public static final TagKey<Item> fiberHemp = createItemWrapper(forgeLoc("fiber_hemp"));
	public static final TagKey<Item> fabricHemp = createItemWrapper(forgeLoc("fabric_hemp"));
	public static final TagKey<Item> coalCoke = createItemWrapper(forgeLoc("coal_coke"));
	public static final TagKey<Item> slag = createItemWrapper(forgeLoc("slag"));
	public static final TagKey<Item> coalCokeDust = createItemWrapper(getDust("coal_coke"));
	public static final TagKey<Item> hopGraphiteDust = createItemWrapper(getDust("hop_graphite"));
	public static final TagKey<Item> hopGraphiteIngot = createItemWrapper(getIngot("hop_graphite"));
	public static final TagKey<Item> copperWire = createItemWrapper(getWire("copper"));
	public static final TagKey<Item> electrumWire = createItemWrapper(getWire("electrum"));
	public static final TagKey<Item> aluminumWire = createItemWrapper(getWire("aluminum"));
	public static final TagKey<Item> steelWire = createItemWrapper(getWire("steel"));
	public static final TagKey<Item> leadWire = createItemWrapper(getWire("lead"));
	public static final TagKey<Item> allWires = createItemWrapper(forgeLoc("wires"));
	public static final TagKey<Item> saltpeterDust = createItemWrapper(getDust("saltpeter"));
	public static final TagKey<Item> sulfurDust = createItemWrapper(getDust("sulfur"));
	public static final TagKey<Item> sawdust = createItemWrapper(getDust("wood"));
	public static final TagKey<Item> plates = createItemWrapper(forgeLoc("plates"));
	public static final TagKey<Item> plasticPlate = createItemWrapper(getPlate("plastic"));
	public static final TagKey<Item> sawblades = createItemWrapper(forgeLoc("sawblades"));
	//Utility tags
	public static final TagKey<Item> forbiddenInCrates = createItemWrapper(rl("forbidden_in_crates"));
	public static final TagKey<Item> circuitPCB = createItemWrapper(rl("circuits/pcb"));
	public static final TagKey<Item> circuitLogic = createItemWrapper(rl("circuits/logic"));
	public static final TagKey<Item> circuitSolder = createItemWrapper(rl("circuits/solder"));
	public static final TagKey<Item> hammers = createItemWrapper(rl("tools/hammers"));
	public static final TagKey<Item> screwdrivers = createItemWrapper(rl("tools/screwdrivers"));
	public static final TagKey<Item> wirecutters = createItemWrapper(rl("tools/wirecutters"));
	public static final TagKey<Item> toolboxTools = createItemWrapper(rl("toolbox/tools"));
	public static final TagKey<Item> toolboxFood = createItemWrapper(rl("toolbox/food"));
	public static final TagKey<Item> toolboxWiring = createItemWrapper(rl("toolbox/wiring"));
	public static final TagKey<Item> connectorInsulator = createItemWrapper(rl("connector_insulator"));
	public static final TagKey<Block> hammerHarvestable = createBlockWrapper(rl("mineable/hammer"));
	public static final TagKey<Block> wirecutterHarvestable = createBlockWrapper(rl("mineable/wirecutter"));
	public static final TagKey<Block> drillHarvestable = createBlockWrapper(rl("mineable/drill"));
	public static final TagKey<Block> rockcutterHarvestable = createBlockWrapper(rl("mineable/rockcutter"));
	public static final TagKey<Block> grindingDiskHarvestable = createBlockWrapper(rl("mineable/grinding_disk"));
	public static final TagKey<Block> surveyToolTargets = createBlockWrapper(rl("survey_tool_targets"));

	public static final TagKey<Block> buzzsawTreeBlacklist = createBlockWrapper(rl("buzzsaw/tree_blacklist"));
	public static final TagKey<Item> tools = createItemWrapper(forgeLoc("tools"));
	public static final TagKey<Item> pickaxes = createItemWrapper(forgeLoc("tools/pickaxes"));
	public static final TagKey<Item> shovels = createItemWrapper(forgeLoc("tools/shovels"));
	public static final TagKey<Item> axes = createItemWrapper(forgeLoc("tools/axes"));
	public static final TagKey<Item> hoes = createItemWrapper(forgeLoc("tools/hoes"));
	public static final TagKey<Item> swords = createItemWrapper(forgeLoc("tools/swords"));
	public static final TagKey<Item> powerpackForbidAttach = createItemWrapper(rl("powerpack/forbid_attach"));

	public static final TagKey<Item> recyclingIgnoredComponents = createItemWrapper(rl("recycling/ignored_components"));
	public static final TagKey<Item> recyclingWhitelist = createItemWrapper(rl("recycling/whitelist"));
	public static final TagKey<Item> recyclingBlacklist = createItemWrapper(rl("recycling/blacklist"));

	public static final TagKey<Fluid> fluidCreosote = createFluidWrapper(forgeLoc("creosote"));
	public static final TagKey<Fluid> fluidPlantoil = createFluidWrapper(forgeLoc("plantoil"));
	public static final TagKey<Fluid> fluidEthanol = createFluidWrapper(forgeLoc("ethanol"));
	public static final TagKey<Fluid> fluidBiodiesel = createFluidWrapper(forgeLoc("biodiesel"));
	public static final TagKey<Fluid> fluidConcrete = createFluidWrapper(forgeLoc("concrete"));
	public static final TagKey<Fluid> fluidHerbicide = createFluidWrapper(forgeLoc("herbicide"));
	public static final TagKey<Fluid> fluidRedstoneAcid = createFluidWrapper(forgeLoc("redstone_acid"));
	public static final TagKey<Fluid> fluidPotion = createFluidWrapper(forgeLoc("potion"));
	public static final TagKey<Fluid> fluidAcetaldehyde = createFluidWrapper(forgeLoc("acetaldehyde"));
	public static final TagKey<Fluid> fluidResin = createFluidWrapper(forgeLoc("phenolic_resin"));
	public static final TagKey<Fluid> drillFuel = createFluidWrapper(rl("drill_fuel"));

	public static final TagKey<EntityType<?>> shaderbagWhitelist = createEntityWrapper(rl("shaderbag/whitelist"));
	public static final TagKey<EntityType<?>> shaderbagBlacklist = createEntityWrapper(rl("shaderbag/blacklist"));

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

	private static ResourceLocation forgeLoc(String path)
	{
		return new ResourceLocation("forge", path);
	}

	public static ResourceLocation getOre(String type)
	{
		return forgeLoc("ores/"+type);
	}

	public static ResourceLocation getRawOre(String type)
	{
		return forgeLoc("raw_materials/"+type);
	}

	public static ResourceLocation getNugget(String type)
	{
		return forgeLoc("nuggets/"+type);
	}

	public static ResourceLocation getIngot(String type)
	{
		return forgeLoc("ingots/"+type);
	}

	public static ResourceLocation getGem(String type)
	{
		return forgeLoc("gems/"+type);
	}

	public static ResourceLocation getStorageBlock(String type)
	{
		return forgeLoc("storage_blocks/"+type);
	}

	public static ResourceLocation getRawBlock(String type)
	{
		return getStorageBlock("raw_"+type);
	}

	public static ResourceLocation getDust(String type)
	{
		return forgeLoc("dusts/"+type);
	}

	public static ResourceLocation getPlate(String type)
	{
		return forgeLoc("plates/"+type);
	}

	public static ResourceLocation getRod(String type)
	{
		return forgeLoc("rods/"+type);
	}

	public static ResourceLocation getGear(String type)
	{
		return forgeLoc("gears/"+type);
	}

	public static ResourceLocation getWire(String type)
	{
		return forgeLoc("wires/"+type);
	}

	public static ResourceLocation getSheetmetalBlock(String type)
	{
		return forgeLoc("sheetmetals/"+type);
	}

	private static ResourceLocation rl(String path)
	{
		return new ResourceLocation(Lib.MODID, path);
	}
}
