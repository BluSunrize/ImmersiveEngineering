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
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.generic.ScaffoldingBlock;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.fluids.IEFluidBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IEBlockTags extends BlockTagsProvider
{

	public IEBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existing)
	{
		super(output, lookupProvider, Lib.MODID, existing);
	}

	@Override
	protected void addTags(Provider p_256380_)
	{
		tag(BlockTags.CROPS)
				.add(Misc.HEMP_PLANT.get());
		tag(BlockTags.FENCES)
				.add(MetalDecoration.ALU_FENCE.get())
				.add(MetalDecoration.STEEL_FENCE.get())
				.add(WoodenDecoration.TREATED_FENCE.get());
		tag(BlockTags.FENCE_GATES)
				.add(MetalDecoration.ALU_FENCE_GATE.get())
				.add(MetalDecoration.STEEL_FENCE_GATE.get())
				.add(WoodenDecoration.TREATED_FENCE_GATE.get());
		tag(BlockTags.WOODEN_FENCES)
				.add(WoodenDecoration.TREATED_FENCE.get());
		tag(BlockTags.DOORS)
				.add(MetalDecoration.STEEL_DOOR.get())
				.add(WoodenDecoration.DOOR.get())
				.add(WoodenDecoration.DOOR_FRAMED.get());
		tag(BlockTags.WOODEN_DOORS)
				.add(WoodenDecoration.DOOR.get())
				.add(WoodenDecoration.DOOR_FRAMED.get());
		tag(BlockTags.TRAPDOORS)
				.add(MetalDecoration.STEEL_TRAPDOOR.get())
				.add(WoodenDecoration.TRAPDOOR.get())
				.add(WoodenDecoration.TRAPDOOR_FRAMED.get());
		tag(BlockTags.WOODEN_TRAPDOORS)
				.add(WoodenDecoration.TRAPDOOR.get())
				.add(WoodenDecoration.TRAPDOOR_FRAMED.get());
		registerSigns(WoodenDecoration.SIGN, MetalDecoration.STEEL_SIGN, MetalDecoration.ALU_SIGN);
		tag(BlockTags.PLANKS).add(WoodenDecoration.FIBERBOARD.get());
		tag(IETags.fencesSteel)
				.add(MetalDecoration.STEEL_FENCE.get());
		tag(IETags.fencesAlu)
				.add(MetalDecoration.ALU_FENCE.get());
		tag(IETags.clayBlock)
				.add(Blocks.CLAY);
		tag(IETags.glowstoneBlock)
				.add(Blocks.GLOWSTONE);
		tag(IETags.copperBlocks)
				.add(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER)
				.add(Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
		tag(IETags.cutCopperBlocks)
				.add(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER)
				.add(Blocks.WAXED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER);
		tag(IETags.cutCopperStairs)
				.add(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS)
				.add(Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
		tag(IETags.cutCopperSlabs)
				.add(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB)
				.add(Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB);

		for(BlockEntry<MetalLadderBlock> b : MetalDecoration.METAL_LADDER.values())
			tag(BlockTags.CLIMBABLE).add(b.get());
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				tag(tags.storage).add(IEBlocks.Metals.STORAGE.get(metal).get());
				tag(Tags.Blocks.STORAGE_BLOCKS).addTag(tags.storage);
				if(metal.shouldAddOre())
				{
					Preconditions.checkNotNull(tags.ore);
					tag(tags.ore)
							.add(IEBlocks.Metals.ORES.get(metal).get())
							.add(IEBlocks.Metals.DEEPSLATE_ORES.get(metal).get());
					tag(Tags.Blocks.ORES).addTag(tags.ore);
					Preconditions.checkNotNull(tags.rawBlock);
					tag(tags.rawBlock).add(IEBlocks.Metals.RAW_ORES.get(metal).get());
					tag(Tags.Blocks.STORAGE_BLOCKS).addTag(tags.rawBlock);
					tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(Metals.ORES.get(metal).get());
					tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(Metals.DEEPSLATE_ORES.get(metal).get());
					tag(Tags.Blocks.ORE_RATES_SINGULAR).add(Metals.ORES.get(metal).get())
							.add(Metals.DEEPSLATE_ORES.get(metal).get());
				}
			}
			tag(tags.sheetmetal).add(IEBlocks.Metals.SHEETMETAL.get(metal).get());
			tag(IETags.sheetmetals).addTag(tags.sheetmetal);
			tag(IETags.sheetmetalSlabs).add(IEBlocks.TO_SLAB.get(Metals.SHEETMETAL.get(metal).getId()).get());
		}
		for(DyeColor dye : DyeColor.values())
		{
			tag(IETags.sheetmetals).add(MetalDecoration.COLORED_SHEETMETAL.get(dye).get());
			tag(IETags.sheetmetalSlabs).add(IEBlocks.TO_SLAB.get(MetalDecoration.COLORED_SHEETMETAL.get(dye).getId()).get());
		}
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			tag(IETags.treatedWood).add(WoodenDecoration.TREATED_WOOD.get(style).get());
			tag(IETags.treatedWoodSlab).add(IEBlocks.TO_SLAB.get(IEBlocks.WoodenDecoration.TREATED_WOOD.get(style).getId()).get());
		}
		for(MetalScaffoldingType t : MetalScaffoldingType.values())
		{
			tag(IETags.scaffoldingSteel).add(MetalDecoration.STEEL_SCAFFOLDING.get(t).get());
			tag(IETags.scaffoldingAlu).add(MetalDecoration.ALU_SCAFFOLDING.get(t).get());
		}
		//Scaffolding stairs & such
		for(final BlockEntry<ScaffoldingBlock> entry : IEBlocks.MetalDecoration.STEEL_SCAFFOLDING.values())
		{
			tag(IETags.scaffoldingSteelStair).add(IEBlocks.TO_STAIRS.get(entry.getId()).get());
			tag(IETags.scaffoldingSteelSlab).add(IEBlocks.TO_SLAB.get(entry.getId()).get());
		}
		for(final BlockEntry<ScaffoldingBlock> entry : IEBlocks.MetalDecoration.ALU_SCAFFOLDING.values())
		{
			tag(IETags.scaffoldingAluStair).add(IEBlocks.TO_STAIRS.get(entry.getId()).get());
			tag(IETags.scaffoldingAluSlab).add(IEBlocks.TO_SLAB.get(entry.getId()).get());
		}

		tag(IETags.coalCokeBlock)
				.add(StoneDecoration.COKE.get());
		tag(Tags.Blocks.GRAVELS)
				.add(StoneDecoration.SLAG_GRAVEL.get());
		tag(BlockTags.FLOWER_POTS)
				.add(Misc.POTTED_HEMP.get());
		tag(BlockTags.WITHER_IMMUNE)
				.add(StoneDecoration.CONCRETE_REINFORCED.get())
				.add(IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_REINFORCED.getId()).get())
				.add(StoneDecoration.CONCRETE_REINFORCED_TILE.get())
				.add(IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_REINFORCED_TILE.getId()).get())
				.add(MetalDecoration.REINFORCED_WINDOW.get());
		tag(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)
				.add(WoodenDevices.WATERMILL.get())
				.add(WoodenDevices.WINDMILL.get());
		//Add parity tags to gravel & sand for IE similar blocks
		tag(BlockTags.BAMBOO_PLANTABLE_ON)
				.add(StoneDecoration.SLAG_GRAVEL.get())
				.add(StoneDecoration.GRIT_SAND.get());
		tag(BlockTags.SCULK_REPLACEABLE)
				.add(StoneDecoration.SLAG_GRAVEL.get())
				.add(StoneDecoration.GRIT_SAND.get());
		tag(BlockTags.AZALEA_ROOT_REPLACEABLE)
				.add(StoneDecoration.SLAG_GRAVEL.get())
				.add(StoneDecoration.GRIT_SAND.get());
		tag(BlockTags.DEAD_BUSH_MAY_PLACE_ON)
				.add(StoneDecoration.GRIT_SAND.get());
		BlockEntry<?>[] concreteBlocks = new BlockEntry<?>[]{
				StoneDecoration.CONCRETE,
				StoneDecoration.CONCRETE_TILE,
				StoneDecoration.CONCRETE_SPRAYED,
				StoneDecoration.CONCRETE_THREE_QUARTER,
				StoneDecoration.CONCRETE_SHEET,
				StoneDecoration.CONCRETE_QUARTER,
				StoneDecoration.CONCRETE_LEADED,
				StoneDecoration.CONCRETE_REINFORCED,
				StoneDecoration.CONCRETE_REINFORCED_TILE,
		};
		for(BlockEntry<?> entry : concreteBlocks)
		{
			tag(IETags.concreteForFeet).add(entry.get());
			BlockEntry<?> shaped;
			if((shaped = IEBlocks.TO_SLAB.get(entry.getId()))!=null)
				tag(IETags.concreteForFeet).add(shaped.get());
			if((shaped = IEBlocks.TO_STAIRS.get(entry.getId()))!=null)
				tag(IETags.concreteForFeet).add(shaped.get());
		}
		tag(IETags.teleportBlocking)
				.add(StoneDecoration.CONCRETE_LEADED.get())
				.add(IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_LEADED.getId()).get())
				.add(IEBlocks.TO_STAIRS.get(StoneDecoration.CONCRETE_LEADED.getId()).get());

		registerHammerMineable();
		registerRockcutterMineable();
		registerGrindingDiskMineable();
		registerPickaxeMineable();
		registerAxeMineable();
		tag(BlockTags.MINEABLE_WITH_SHOVEL)
				.add(WoodenDecoration.SAWDUST.get())
				.add(StoneDecoration.SLAG_GRAVEL.get())
				.add(StoneDecoration.GRIT_SAND.get());
		tag(IETags.wirecutterHarvestable)
				.add(MetalDevices.RAZOR_WIRE.get());
		tag(IETags.drillHarvestable)
				.addTag(BlockTags.MINEABLE_WITH_SHOVEL)
				.addTag(BlockTags.MINEABLE_WITH_PICKAXE);
		tag(IETags.buzzsawTreeBlacklist)
				.addOptionalTag(ResourceLocation.fromNamespaceAndPath("dynamictrees", "branches"))
				.addOptionalTag(ResourceLocation.fromNamespaceAndPath("dynamictrees", "leaves"));
		tag(IETags.surveyToolTargets)
				//Overworld stones
				.addTag(Tags.Blocks.STONES)
				.add(Blocks.DRIPSTONE_BLOCK)
				.add(Blocks.CALCITE)
				.add(Blocks.SANDSTONE)
				.add(Blocks.RED_SANDSTONE)
				//Overworld soils
				.addTag(BlockTags.DIRT)
				.remove(Blocks.MOSS_BLOCK)
				.addTag(Tags.Blocks.GRAVELS)
				.addTag(Tags.Blocks.SANDS)
				.add(Blocks.CLAY)
				//Overworld terracotta
				.add(Blocks.TERRACOTTA)
				.add(Blocks.WHITE_TERRACOTTA)
				.add(Blocks.LIGHT_GRAY_TERRACOTTA)
				.add(Blocks.GRAY_TERRACOTTA)
				.add(Blocks.BROWN_TERRACOTTA)
				.add(Blocks.RED_TERRACOTTA)
				.add(Blocks.ORANGE_TERRACOTTA)
				.add(Blocks.YELLOW_TERRACOTTA)
				//Nether stones
				.addTag(Tags.Blocks.NETHERRACKS)
				.add(Blocks.BASALT)
				.add(Blocks.BLACKSTONE)
				//Nether soils
				.addTag(BlockTags.NYLIUM)
				.add(Blocks.SOUL_SAND)
				.add(Blocks.SOUL_SOIL)
				//End Stones
				.add(Blocks.END_STONE);
		checkAllRegisteredForBreaking();

		for(BlockEntry<?> treatedWood : WoodenDecoration.TREATED_WOOD.values())
		{
			tag(BlockTags.WOODEN_SLABS).add(IEBlocks.TO_SLAB.get(treatedWood.getId()).get());
			tag(BlockTags.WOODEN_STAIRS).add(IEBlocks.TO_STAIRS.get(treatedWood.getId()).get());
		}
		for(BlockEntry<?> slab : IEBlocks.TO_SLAB.values())
			tag(BlockTags.SLABS).add(slab.get());
		for(BlockEntry<?> stairs : IEBlocks.TO_STAIRS.values())
			tag(BlockTags.STAIRS).add(stairs.get());
		for(BlockEntry<?> walls : IEBlocks.TO_WALL.values())
			tag(BlockTags.WALLS).add(walls.get());

		tag(IETags.incorrectDropsSteel).addTag(BlockTags.INCORRECT_FOR_IRON_TOOL);

		/* MOD COMPAT STARTS HERE */

		// TConstruct
		tag(TagUtils.createBlockWrapper(ResourceLocation.fromNamespaceAndPath("tconstruct", "harvestable/stackable")))
				.add(Misc.HEMP_PLANT.get());
		tag(TagUtils.createBlockWrapper(ResourceLocation.fromNamespaceAndPath("chiselsandbits", "chiselable/forced")))
				.add(StoneDecoration.INSULATING_GLASS.get())
				.add(WoodenDevices.WOODEN_BARREL.get())
				.add(WoodenDevices.TURNTABLE.get())
				.add(WoodenDevices.CRATE.get())
				.add(WoodenDevices.REINFORCED_CRATE.get())
				.add(WoodenDevices.ITEM_BATCHER.get())
				.add(WoodenDevices.FLUID_SORTER.get())
				.add(WoodenDevices.SORTER.get())
				.add(MetalDevices.CAPACITOR_LV.get())
				.add(MetalDevices.CAPACITOR_MV.get())
				.add(MetalDevices.CAPACITOR_HV.get())
				.add(MetalDevices.CAPACITOR_CREATIVE.get())
				.add(MetalDevices.BARREL.get())
				.add(MetalDevices.FURNACE_HEATER.get())
				.add(MetalDevices.DYNAMO.get())
				.add(MetalDevices.THERMOELECTRIC_GEN.get());
	}

	private void registerHammerMineable()
	{
		IntrinsicTagAppender<Block> tag = tag(IETags.hammerHarvestable);
		MetalDecoration.METAL_LADDER.values().forEach(b -> tag.add(b.get()));
		tag.addTag(IETags.scaffoldingSteel);
		tag.addTag(IETags.scaffoldingAlu);
		tag.add(StoneDecoration.CONCRETE_SPRAYED.get())
				.add(Cloth.STRIP_CURTAIN.get());
		//TODO not really the nicest approach, but maintains 1.16 behavior
		for(Holder<Block> regObject : IEBlocks.REGISTER.getEntries())
		{
			Block block = regObject.value();
			if(block instanceof ConnectorBlock<?>||block instanceof ConveyorBlock)
				tag.add(block);
		}
		//Razor wire should only be broken by wirecutters, and its addition is accidental
		tag.remove(MetalDevices.RAZOR_WIRE.get());
		// switchboard doesn't extend ConnectorBlock
		tag.add(Connectors.REDSTONE_SWITCHBOARD.get());
	}

	private void registerRockcutterMineable()
	{
		/*
		 * Current design philosophy for the rockcutter is as follows:
		 * The rockcutter is not here to break synthetic stone blocks, it's here to break worldgen blocks + silktouchables
		 * Thus, no stairs or slabs or similar, and the exclusion of prismarine
		 * Just the stone blocks common in every dimension along with ores and other similar blocks
		 */
		IntrinsicTagAppender<Block> tag = tag(IETags.rockcutterHarvestable);
		// overworld stones & ores
		tag.addTag(Tags.Blocks.COBBLESTONES);
		tag.addTag(Tags.Blocks.STONES);
		tag.addTag(Tags.Blocks.SANDSTONE_BLOCKS);
		tag.addTag(Tags.Blocks.ORES);
		tag.add(Blocks.CALCITE, Blocks.DRIPSTONE_BLOCK, Blocks.POINTED_DRIPSTONE);
		// specialty stones and stone-alikes
		tag.addTag(Tags.Blocks.NETHERRACKS);
		tag.add(Blocks.BASALT, Blocks.SMOOTH_BASALT);
		tag.add(Blocks.BLACKSTONE);
		tag.add(Blocks.END_STONE);
		tag.add(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
		tag.add(Blocks.BONE_BLOCK);
		// glass, ice, amethyst, glowing blocks, gilded blackstone
		tag.addTag(Tags.Blocks.GLASS_BLOCKS);
		tag.addTag(Tags.Blocks.GLASS_PANES);
		tag.addTag(BlockTags.ICE);
		tag.add(Blocks.AMETHYST_BLOCK, Blocks.BUDDING_AMETHYST, Blocks.AMETHYST_CLUSTER, Blocks.LARGE_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.SMALL_AMETHYST_BUD);
		tag.add(Blocks.GLOWSTONE);
		tag.add(Blocks.SEA_LANTERN);
		tag.add(Blocks.GILDED_BLACKSTONE);
		// coral, first alive by tag and then dead by block - silktouchable and pickaxe-needed + the dead ones for consistency
		tag.addTag(BlockTags.CORAL_BLOCKS);
		tag.add(Blocks.DEAD_BRAIN_CORAL_BLOCK, Blocks.DEAD_BUBBLE_CORAL_BLOCK, Blocks.DEAD_FIRE_CORAL_BLOCK, Blocks.DEAD_HORN_CORAL_BLOCK, Blocks.DEAD_TUBE_CORAL_BLOCK);
		// enderchest
		tag.addTag(Tags.Blocks.CHESTS_ENDER);
		// skulk, but intentionally only some of them
		tag.add(Blocks.SCULK, Blocks.SCULK_CATALYST, Blocks.SCULK_SENSOR, Blocks.CALIBRATED_SCULK_SENSOR);
	}

	private void registerGrindingDiskMineable()
	{
		IntrinsicTagAppender<Block> tag = tag(IETags.grindingDiskHarvestable);
		// storage and remove rocklike storage, sheetmetal
		tag.addTag(Tags.Blocks.STORAGE_BLOCKS);
		tag.addTag(IETags.sheetmetals);
		// storage and sheetmetal slabs
		for(EnumMetals metal : EnumMetals.values())
			if(!metal.isVanillaMetal())
				tag.add(IEBlocks.TO_SLAB.get(Metals.STORAGE.get(metal).getId()).get());
		tag.addTag(IETags.sheetmetalSlabs);
		// remove blocks the grinding disc shouldn't cut
		tag.remove(Blocks.AMETHYST_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.LAPIS_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.COAL_BLOCK);
		tag.remove(Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER, Tags.Blocks.STORAGE_BLOCKS_RAW_IRON, Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD);
		for(BlockEntry raw_storage : Metals.RAW_ORES.values())
			tag.remove(raw_storage.get());
		// copper
		tag.addTag(IETags.copperBlocks);
		tag.addTag(IETags.cutCopperBlocks);
		tag.addTag(IETags.cutCopperSlabs);
		tag.addTag(IETags.cutCopperStairs);
		// vanilla blocks
		tag.add(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL);
		tag.add(Blocks.CHAIN, Blocks.IRON_BARS, Blocks.IRON_DOOR, Blocks.IRON_TRAPDOOR, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
				Blocks.HOPPER, Blocks.CAULDRON, Blocks.LAVA_CAULDRON, Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON, Blocks.LIGHTNING_ROD);
		// scaffolding
		tag.addTag(IETags.scaffoldingSteel);
		tag.addTag(IETags.scaffoldingAlu);
		// decorations including catwalks
		tag.add(MetalDecoration.LANTERN.get(), MetalDecoration.CAGELAMP.get(), MetalDecoration.STEEL_DOOR.get(), MetalDecoration.STEEL_TRAPDOOR.get(), MetalDecoration.REINFORCED_WINDOW.get());
		tag.add(MetalDecoration.ALU_FENCE.get(), MetalDecoration.ALU_POST.get(), MetalDecoration.ALU_WALLMOUNT.get(), MetalDecoration.ALU_SLOPE.get(),
				MetalDecoration.ALU_CATWALK.get(), MetalDecoration.ALU_CATWALK_STAIRS.get(), MetalDecoration.ALU_WINDOW.get());
		tag.add(MetalDecoration.STEEL_FENCE.get(), MetalDecoration.STEEL_POST.get(), MetalDecoration.STEEL_WALLMOUNT.get(), MetalDecoration.STEEL_SLOPE.get(),
				MetalDecoration.STEEL_CATWALK.get(), MetalDecoration.STEEL_CATWALK_STAIRS.get(), MetalDecoration.STEEL_WINDOW.get());
		MetalDecoration.METAL_LADDER.values().forEach(entry -> tag.add(entry.get()));
		// chutes
		MetalDevices.CHUTES.values().forEach(entry -> tag.add(entry.get()));
		MetalDevices.DYED_CHUTES.values().forEach(entry -> tag.add(entry.get()));
		// fluid machines
		tag.add(MetalDevices.BARREL.get(), MetalDevices.FLUID_PUMP.get(), MetalDevices.FLUID_PIPE.get(), MetalDevices.FLUID_PLACER.get(), MetalDevices.PIPE_VALVE.get());
		// other machines
		tag.add(MetalDevices.BLAST_FURNACE_PREHEATER.get(), MetalDevices.FURNACE_HEATER.get(), MetalDevices.DYNAMO.get(), MetalDevices.THERMOELECTRIC_GEN.get(),
				MetalDevices.ELECTRIC_LANTERN.get(), MetalDevices.SAMPLE_DRILL.get(), MetalDevices.FLOODLIGHT.get(), MetalDevices.ELECTROMAGNET.get());
		// wire connected machines
		tag.add(Connectors.CONNECTOR_STRUCTURAL.get(), Connectors.TRANSFORMER.get(), Connectors.TRANSFORMER_HV.get(), Connectors.BREAKER_SWITCH.get(),
				Connectors.REDSTONE_BREAKER.get(), Connectors.CURRENT_TRANSFORMER.get(), Connectors.POST_TRANSFORMER.get());
		// multiblock components
		tag.add(MetalDecoration.LV_COIL.get(), MetalDecoration.MV_COIL.get(), MetalDecoration.MV_COIL.get());
		tag.add(MetalDecoration.ENGINEERING_RS.get(), MetalDecoration.ENGINEERING_LIGHT.get(), MetalDecoration.ENGINEERING_HEAVY.get(), MetalDecoration.ENGINEERING_RESONANZ.get(), MetalDecoration.RADIATOR.get(), MetalDecoration.GENERATOR.get());
		// multiblock blocks?
		tag.add(IEMultiblocks.CRUSHER.getBlock(), IEMultiblocks.SAWMILL.getBlock(), IEMultiblocks.ARC_FURNACE.getBlock(), IEMultiblocks.ASSEMBLER.getBlock(),
				IEMultiblocks.AUTO_WORKBENCH.getBlock(), IEMultiblocks.BOTTLING_MACHINE.getBlock(), IEMultiblocks.BUCKET_WHEEL.getBlock(),
				IEMultiblocks.DIESEL_GENERATOR.getBlock(), IEMultiblocks.EXCAVATOR.getBlock(), IEMultiblocks.FERMENTER.getBlock(), IEMultiblocks.LIGHTNING_ROD.getBlock(),
				IEMultiblocks.METAL_PRESS.getBlock(), IEMultiblocks.MIXER.getBlock(), IEMultiblocks.REFINERY.getBlock(), IEMultiblocks.SHEETMETAL_TANK.getBlock(),
				IEMultiblocks.SILO.getBlock(), IEMultiblocks.SQUEEZER.getBlock(), IEMultiblocks.RADIO_TOWER.getBlock(), IEMultiblocks.CHUNK_LOADER.getBlock());
	}

	private void registerAxeMineable()
	{
		IntrinsicTagAppender<Block> tag = tag(BlockTags.MINEABLE_WITH_AXE);
		registerMineable(
				tag,
				WoodenDevices.CRAFTING_TABLE,
				WoodenDevices.WORKBENCH,
				WoodenDevices.BLUEPRINT_SHELF,
				WoodenDevices.CIRCUIT_TABLE,
				WoodenDevices.GUNPOWDER_BARREL,
				WoodenDevices.WOODEN_BARREL,
				WoodenDevices.TURNTABLE,
				WoodenDevices.CRATE,
				WoodenDevices.REINFORCED_CRATE,
				WoodenDevices.SORTER,
				WoodenDevices.ITEM_BATCHER,
				WoodenDevices.FLUID_SORTER,
				WoodenDevices.WINDMILL,
				WoodenDevices.WATERMILL,
				WoodenDevices.TREATED_WALLMOUNT,
				WoodenDevices.LOGIC_UNIT,
				WoodenDevices.MACHINE_INTERFACE,
				WoodenDecoration.TREATED_FENCE,
				WoodenDecoration.TREATED_FENCE_GATE,
				WoodenDecoration.TREATED_SCAFFOLDING,
				WoodenDecoration.TREATED_POST,
				WoodenDecoration.SAWDUST,
				WoodenDecoration.FIBERBOARD,
				WoodenDecoration.WINDOW,
				WoodenDecoration.CATWALK,
				WoodenDecoration.CATWALK_STAIRS,
				WoodenDecoration.DOOR,
				WoodenDecoration.DOOR_FRAMED,
				WoodenDecoration.TRAPDOOR,
				WoodenDecoration.TRAPDOOR_FRAMED,
				WoodenDecoration.BASIC_ENGINEERING,
				Cloth.SHADER_BANNER,
				Cloth.SHADER_BANNER_WALL
		);
		registerMineable(tag, WoodenDecoration.SIGN.getEntries());
		for(BlockEntry<?> treatedWood : WoodenDecoration.TREATED_WOOD.values())
			registerMineable(tag, treatedWood);
	}

	private void registerMineable(IntrinsicTagAppender<Block> tag, MultiblockRegistration<?>... entries)
	{
		for(MultiblockRegistration<?> entry : entries)
			tag.add(entry.block().get());
	}

	private <T extends Block> void registerMineable(IntrinsicTagAppender<Block> tag, Map<?, BlockEntry<T>> entries)
	{
		registerMineable(tag, new ArrayList<>(entries.values()));
	}

	private void registerMineable(IntrinsicTagAppender<Block> tag, BlockEntry<?>... entries)
	{
		registerMineable(tag, Arrays.asList(entries));
	}

	private void registerMineable(IntrinsicTagAppender<Block> tag, List<BlockEntry<?>> entries)
	{
		entries.sort(Comparator.comparing(BlockEntry::getId));
		for(BlockEntry<?> entry : entries)
		{
			tag.add(entry.get());
			BlockEntry<?> slab = IEBlocks.TO_SLAB.get(entry.getId());
			if(slab!=null)
				tag.add(slab.get());
			BlockEntry<?> stairs = IEBlocks.TO_STAIRS.get(entry.getId());
			if(stairs!=null)
				tag.add(stairs.get());
			BlockEntry<?> wall = IEBlocks.TO_WALL.get(entry.getId());
			if(wall!=null)
				tag.add(wall.get());
		}
	}

	private void registerPickaxeMineable()
	{
		IntrinsicTagAppender<Block> tag = tag(BlockTags.MINEABLE_WITH_PICKAXE);

		registerMineable(
				tag,
				IEMultiblockLogic.COKE_OVEN,
				IEMultiblockLogic.BLAST_FURNACE,
				IEMultiblockLogic.ALLOY_SMELTER,
				IEMultiblockLogic.ADV_BLAST_FURNACE,
				IEMultiblockLogic.METAL_PRESS,
				IEMultiblockLogic.CRUSHER,
				IEMultiblockLogic.SAWMILL,
				IEMultiblockLogic.TANK,
				IEMultiblockLogic.SILO,
				IEMultiblockLogic.ASSEMBLER,
				IEMultiblockLogic.AUTO_WORKBENCH,
				IEMultiblockLogic.BOTTLING_MACHINE,
				IEMultiblockLogic.SQUEEZER,
				IEMultiblockLogic.FERMENTER,
				IEMultiblockLogic.REFINERY,
				IEMultiblockLogic.DIESEL_GENERATOR,
				IEMultiblockLogic.EXCAVATOR,
				IEMultiblockLogic.BUCKET_WHEEL,
				IEMultiblockLogic.ARC_FURNACE,
				IEMultiblockLogic.LIGHTNING_ROD,
				IEMultiblockLogic.MIXER,
				IEMultiblockLogic.CHUNK_LOADER
		);
		registerMineable(
				tag,
				StoneDecoration.COKEBRICK,
				StoneDecoration.BLASTBRICK,
				StoneDecoration.BLASTBRICK_REINFORCED,
				StoneDecoration.SLAG_BRICK,
				StoneDecoration.CLINKER_BRICK,
				StoneDecoration.CLINKER_BRICK_QUOIN,
				StoneDecoration.CLINKER_BRICK_SILL,
				StoneDecoration.COKE,
				StoneDecoration.HEMPCRETE,
				StoneDecoration.HEMPCRETE_BRICK,
				StoneDecoration.HEMPCRETE_BRICK_CRACKED,
				StoneDecoration.HEMPCRETE_CHISELED,
				StoneDecoration.HEMPCRETE_PILLAR,
				StoneDecoration.CONCRETE,
				StoneDecoration.CONCRETE_BRICK,
				StoneDecoration.CONCRETE_BRICK_CRACKED,
				StoneDecoration.CONCRETE_CHISELED,
				StoneDecoration.CONCRETE_PILLAR,
				StoneDecoration.CONCRETE_TILE,
				StoneDecoration.CONCRETE_LEADED,
				StoneDecoration.CONCRETE_REINFORCED,
				StoneDecoration.CONCRETE_REINFORCED_TILE,
				StoneDecoration.INSULATING_GLASS,
				StoneDecoration.SLAG_GLASS,
				StoneDecoration.CONCRETE_SPRAYED,
				StoneDecoration.ALLOYBRICK,
				StoneDecoration.CONCRETE_SHEET,
				StoneDecoration.CONCRETE_QUARTER,
				StoneDecoration.CONCRETE_THREE_QUARTER,
				StoneDecoration.CORESAMPLE,
				StoneDecoration.DUROPLAST,
				MetalDevices.RAZOR_WIRE,
				MetalDevices.TOOLBOX,
				MetalDevices.CAPACITOR_LV,
				MetalDevices.CAPACITOR_MV,
				MetalDevices.CAPACITOR_HV,
				MetalDevices.CAPACITOR_CREATIVE,
				MetalDevices.BARREL,
				MetalDevices.FLUID_PUMP,
				MetalDevices.FLUID_PLACER,
				MetalDevices.BLAST_FURNACE_PREHEATER,
				MetalDevices.FURNACE_HEATER,
				MetalDevices.DYNAMO,
				MetalDevices.THERMOELECTRIC_GEN,
				MetalDevices.ELECTRIC_LANTERN,
				MetalDevices.CHARGING_STATION,
				MetalDevices.FLUID_PIPE,
				MetalDevices.SAMPLE_DRILL,
				MetalDevices.TESLA_COIL,
				MetalDevices.FLOODLIGHT,
				MetalDevices.TURRET_CHEM,
				MetalDevices.TURRET_GUN,
				MetalDevices.CLOCHE,
				MetalDevices.ELECTROMAGNET,
				MetalDevices.PIPE_VALVE,
				MetalDecoration.LV_COIL,
				MetalDecoration.MV_COIL,
				MetalDecoration.HV_COIL,
				MetalDecoration.ENGINEERING_RS,
				MetalDecoration.ENGINEERING_HEAVY,
				MetalDecoration.ENGINEERING_LIGHT,
				MetalDecoration.ENGINEERING_RESONANZ,
				MetalDecoration.GENERATOR,
				MetalDecoration.RADIATOR,
				MetalDecoration.STEEL_FENCE,
				MetalDecoration.STEEL_FENCE_GATE,
				MetalDecoration.ALU_FENCE,
				MetalDecoration.ALU_FENCE_GATE,
				MetalDecoration.STEEL_WALLMOUNT,
				MetalDecoration.ALU_WALLMOUNT,
				MetalDecoration.STEEL_POST,
				MetalDecoration.ALU_POST,
				MetalDecoration.LANTERN,
				MetalDecoration.CAGELAMP,
				MetalDecoration.STEEL_SLOPE,
				MetalDecoration.ALU_SLOPE,
				MetalDecoration.STEEL_WINDOW,
				MetalDecoration.ALU_WINDOW,
				MetalDecoration.REINFORCED_WINDOW,
				MetalDecoration.STEEL_CATWALK,
				MetalDecoration.STEEL_CATWALK_STAIRS,
				MetalDecoration.ALU_CATWALK,
				MetalDecoration.ALU_CATWALK_STAIRS,
				MetalDecoration.STEEL_DOOR,
				MetalDecoration.STEEL_TRAPDOOR,
				Connectors.CONNECTOR_STRUCTURAL,
				Connectors.TRANSFORMER,
				Connectors.POST_TRANSFORMER,
				Connectors.TRANSFORMER_HV,
				Connectors.BREAKER_SWITCH,
				Connectors.REDSTONE_BREAKER,
				Connectors.CURRENT_TRANSFORMER,
				Connectors.CONNECTOR_REDSTONE,
				Connectors.CONNECTOR_PROBE,
				Connectors.CONNECTOR_BUNDLED,
				Connectors.REDSTONE_STATE_CELL,
				Connectors.REDSTONE_TIMER,
				Connectors.REDSTONE_SWITCHBOARD,
				Connectors.SIREN,
				Connectors.FEEDTHROUGH
		);
		registerMineable(tag, Metals.SHEETMETAL);
		registerMineable(tag, MetalDecoration.COLORED_SHEETMETAL);
		registerMineable(tag, MetalDecoration.METAL_LADDER);
		registerMineable(tag, MetalDecoration.STEEL_SCAFFOLDING);
		registerMineable(tag, MetalDecoration.ALU_SCAFFOLDING);
		registerMineable(tag, MetalDecoration.STEEL_SIGN.getEntries());
		registerMineable(tag, MetalDecoration.ALU_SIGN.getEntries());
		registerMineable(tag, MetalDecoration.WARNING_SIGNS);
		registerMineable(tag, MetalDevices.CHUTES);
		registerMineable(tag, MetalDevices.DYED_CHUTES);
		registerMineable(tag, Connectors.ENERGY_CONNECTORS);
		registerMineable(tag, MetalDevices.CONVEYORS);
		setOreMiningLevel(EnumMetals.COPPER, Tiers.STONE);
		setOreMiningLevel(EnumMetals.ALUMINUM, Tiers.STONE);
		setOreMiningLevel(EnumMetals.LEAD, Tiers.IRON);
		setOreMiningLevel(EnumMetals.SILVER, Tiers.IRON);
		setOreMiningLevel(EnumMetals.NICKEL, Tiers.IRON);
		setOreMiningLevel(EnumMetals.URANIUM, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.COPPER, Tiers.STONE);
		setStorageMiningLevel(EnumMetals.ALUMINUM, Tiers.STONE);
		setStorageMiningLevel(EnumMetals.LEAD, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.SILVER, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.NICKEL, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.URANIUM, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.CONSTANTAN, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.ELECTRUM, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.STEEL, Tiers.IRON);
	}

	private void setOreMiningLevel(EnumMetals metal, Tiers level)
	{
		final BlockEntry<Block> ore = Metals.ORES.get(metal);
		final BlockEntry<Block> deepslateOre = Metals.DEEPSLATE_ORES.get(metal);
		final BlockEntry<Block> rawOre = Metals.RAW_ORES.get(metal);
		setMiningLevel(ore, level);
		setMiningLevel(deepslateOre, level);
		setMiningLevel(rawOre, level);
		registerMineable(tag(BlockTags.MINEABLE_WITH_PICKAXE), ore, deepslateOre, rawOre);
	}

	private void setStorageMiningLevel(EnumMetals metal, Tiers level)
	{
		final BlockEntry<Block> storage = Metals.STORAGE.get(metal);
		setMiningLevel(storage, level);
		registerMineable(tag(BlockTags.MINEABLE_WITH_PICKAXE), storage);
	}

	private void setMiningLevel(Supplier<Block> block, Tiers level)
	{
		TagKey<Block> tag = switch(level)
		{
			case STONE -> BlockTags.NEEDS_STONE_TOOL;
			case IRON -> BlockTags.NEEDS_IRON_TOOL;
			case DIAMOND -> BlockTags.NEEDS_DIAMOND_TOOL;
			default -> throw new IllegalArgumentException("No tag available for "+level.name());
		};
		tag(tag).add(block.get());
	}

	private void registerSigns(SignHolder... signs)
	{
		for(SignHolder holder : signs)
		{
			tag(BlockTags.STANDING_SIGNS).add(holder.sign().get());
			tag(BlockTags.WALL_SIGNS).add(holder.wall().get());
			tag(BlockTags.CEILING_HANGING_SIGNS).add(holder.hanging().get());
			tag(BlockTags.WALL_HANGING_SIGNS).add(holder.wallHanging().get());
		}
	}

	private void checkAllRegisteredForBreaking()
	{
		List<TagKey<Block>> knownHarvestTags = ImmutableList.of(
				BlockTags.MINEABLE_WITH_AXE,
				BlockTags.MINEABLE_WITH_PICKAXE,
				BlockTags.MINEABLE_WITH_SHOVEL,
				IETags.wirecutterHarvestable,
				IETags.hammerHarvestable
		);
		Set<ResourceLocation> harvestable = knownHarvestTags.stream()
				.map(this::tag)
				.map(TagAppender::getInternalBuilder)
				.flatMap(b -> b.build().stream())
				.map(Object::toString)
				.map(ResourceLocation::tryParse)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		Set<ResourceLocation> knownNonHarvestable = Stream.of(
						Cloth.BALLOON, Cloth.CUSHION, Misc.FAKE_LIGHT, Misc.POTTED_HEMP, Misc.HEMP_PLANT
				)
				.map(BlockEntry::getId)
				.collect(Collectors.toSet());
		Set<ResourceLocation> registered = IEBlocks.REGISTER.getEntries().stream()
				.map(Holder::value)
				.filter(b -> !(b instanceof IEFluidBlock))
				.map(BuiltInRegistries.BLOCK::getKey)
				.filter(name -> !knownNonHarvestable.contains(name))
				.collect(Collectors.toSet());
		Set<ResourceLocation> notHarvestable = Sets.difference(registered, harvestable);
		if(!notHarvestable.isEmpty())
		{
			notHarvestable.forEach(rl -> IELogger.logger.error("Not harvestable: {}", rl));
			throw new RuntimeException();
		}
	}
}
