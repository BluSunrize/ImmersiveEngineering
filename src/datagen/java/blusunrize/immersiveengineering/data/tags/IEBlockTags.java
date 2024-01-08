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
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.fluids.IEFluidBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

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
		tag(BlockTags.WOODEN_FENCES)
				.add(WoodenDecoration.TREATED_FENCE.get());
		tag(BlockTags.PLANKS).add(WoodenDecoration.FIBERBOARD.get());
		tag(IETags.fencesSteel)
				.add(MetalDecoration.STEEL_FENCE.get());
		tag(IETags.fencesAlu)
				.add(MetalDecoration.ALU_FENCE.get());
		tag(IETags.clayBlock)
				.add(Blocks.CLAY);
		tag(IETags.glowstoneBlock)
				.add(Blocks.GLOWSTONE);
		tag(IETags.colorlessSandstoneBlocks)
				.add(Blocks.SANDSTONE)
				.add(Blocks.CUT_SANDSTONE)
				.add(Blocks.CHISELED_SANDSTONE)
				.add(Blocks.SMOOTH_SANDSTONE);
		tag(IETags.redSandstoneBlocks)
				.add(Blocks.RED_SANDSTONE)
				.add(Blocks.CUT_RED_SANDSTONE)
				.add(Blocks.CHISELED_RED_SANDSTONE)
				.add(Blocks.SMOOTH_RED_SANDSTONE);
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
		tag(Tags.Blocks.GRAVEL)
				.add(StoneDecoration.SLAG_GRAVEL.get());
		tag(BlockTags.FLOWER_POTS)
				.add(Misc.POTTED_HEMP.get());
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
				.addOptionalTag(new ResourceLocation("dynamictrees", "branches"))
				.addOptionalTag(new ResourceLocation("dynamictrees", "leaves"));
		tag(IETags.surveyToolTargets)
				.addTag(BlockTags.DIRT)
				.addTag(Tags.Blocks.GRAVEL)
				.add(Blocks.GRASS_BLOCK)
				.add(Blocks.CLAY);
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
		for(BlockEntry<?> stairs : IEBlocks.TO_WALL.values())
			tag(BlockTags.WALLS).add(stairs.get());

		/* MOD COMPAT STARTS HERE */

		// TConstruct
		tag(TagUtils.createBlockWrapper(new ResourceLocation("tconstruct:harvestable/stackable")))
				.add(Misc.HEMP_PLANT.get());
		tag(TagUtils.createBlockWrapper(new ResourceLocation("chiselsandbits:chiselable/forced")))
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
		for(RegistryObject<Block> regObject : IEBlocks.REGISTER.getEntries())
		{
			Block block = regObject.get();
			if(block instanceof ConnectorBlock<?>||block instanceof ConveyorBlock)
				tag.add(block);
		}
	}

	// TODO rockcutter and grinding disk tags are nowhere near complete at this point, they were determined based on
	//  block material in <=1.19.4
	private void registerRockcutterMineable()
	{
		IntrinsicTagAppender<Block> tag = tag(IETags.rockcutterHarvestable);
		tag.addTag(Tags.Blocks.STONE);
		tag.addTag(Tags.Blocks.GLASS);
		tag.addTag(BlockTags.ICE);
	}

	private void registerGrindingDiskMineable()
	{
		IntrinsicTagAppender<Block> tag = tag(IETags.grindingDiskHarvestable);
		tag.addTag(Tags.Blocks.STORAGE_BLOCKS);
		tag.addTag(IETags.sheetmetals);
		tag.addTag(IETags.scaffoldingSteel);
		tag.addTag(IETags.scaffoldingAlu);
	}

	private void registerAxeMineable()
	{
		IntrinsicTagAppender<Block> tag = tag(BlockTags.MINEABLE_WITH_AXE);
		registerMineable(
				tag,
				WoodenDevices.CRAFTING_TABLE,
				WoodenDevices.WORKBENCH,
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
				WoodenDecoration.TREATED_FENCE,
				WoodenDecoration.TREATED_SCAFFOLDING,
				WoodenDecoration.TREATED_POST,
				WoodenDecoration.SAWDUST,
				WoodenDecoration.FIBERBOARD,
				Cloth.SHADER_BANNER,
				Cloth.SHADER_BANNER_WALL
		);
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
				IEMultiblockLogic.MIXER
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
				MetalDecoration.LV_COIL,
				MetalDecoration.MV_COIL,
				MetalDecoration.HV_COIL,
				MetalDecoration.ENGINEERING_RS,
				MetalDecoration.ENGINEERING_HEAVY,
				MetalDecoration.ENGINEERING_LIGHT,
				MetalDecoration.GENERATOR,
				MetalDecoration.RADIATOR,
				MetalDecoration.STEEL_FENCE,
				MetalDecoration.ALU_FENCE,
				MetalDecoration.STEEL_WALLMOUNT,
				MetalDecoration.ALU_WALLMOUNT,
				MetalDecoration.STEEL_POST,
				MetalDecoration.ALU_POST,
				MetalDecoration.LANTERN,
				MetalDecoration.STEEL_SLOPE,
				MetalDecoration.ALU_SLOPE,
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
				Connectors.FEEDTHROUGH
		);
		registerMineable(tag, Metals.SHEETMETAL);
		registerMineable(tag, MetalDecoration.COLORED_SHEETMETAL);
		registerMineable(tag, MetalDecoration.METAL_LADDER);
		registerMineable(tag, MetalDecoration.STEEL_SCAFFOLDING);
		registerMineable(tag, MetalDecoration.ALU_SCAFFOLDING);
		registerMineable(tag, MetalDevices.CHUTES);
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
				.map(RegistryObject::get)
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
