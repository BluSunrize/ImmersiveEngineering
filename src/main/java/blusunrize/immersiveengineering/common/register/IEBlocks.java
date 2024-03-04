/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.*;
import blusunrize.immersiveengineering.common.blocks.cloth.*;
import blusunrize.immersiveengineering.common.blocks.generic.ScaffoldingBlock;
import blusunrize.immersiveengineering.common.blocks.generic.*;
import blusunrize.immersiveengineering.common.blocks.metal.LanternBlock;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.plant.PottedHempBlock;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleBlockEntity;
import blusunrize.immersiveengineering.common.blocks.stone.PartialConcreteBlock;
import blusunrize.immersiveengineering.common.blocks.stone.SlagGravelBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.BarrelBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.CraftingTableBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO block items
public final class IEBlocks
{
	public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Lib.MODID);
	private static final Supplier<Properties> STONE_DECO_PROPS = () -> Block.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.sound(SoundType.STONE)
			.requiresCorrectToolForDrops()
			.strength(2, 10);

	private static final Supplier<Properties> STONE_DECO_STONE_BRICK_PROPS = () -> Block.Properties.of()
			.sound(SoundType.STONE)
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.75f, 10);
	private static final Supplier<Properties> STONE_DECO_LEADED_PROPS = () -> Block.Properties.of()
			.sound(SoundType.STONE)
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2, 180);
	private static final Supplier<Properties> STONE_DECO_PROPS_NOT_SOLID = () -> Block.Properties.of()
			.sound(SoundType.STONE)
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(0.5f, 0.5f) //Glass & Tinted Glass are 0.3f,0.3f. These glasses are stronger, thus 0.5f,0.5f
			.noOcclusion();

	private static final Supplier<Properties> STONE_DECO_BRICK_PROPS = () -> Block.Properties.of()
			.sound(SoundType.NETHER_BRICKS)
			.mapColor(MapColor.COLOR_RED)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2f, 8);

	private static final Supplier<Properties> STONE_DECO_GBRICK_PROPS = () -> Block.Properties.of()
			.sound(SoundType.NETHER_BRICKS)
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2f, 8);
	private static final Supplier<Properties> SHEETMETAL_PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.METAL)
			.sound(SoundType.METAL)
			.strength(2, 2); //Cauldron props are 2,2 and sheetmetal is similar
	private static final Supplier<Properties> STANDARD_WOOD_PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.WOOD)
			.ignitedByLava()
			.instrument(NoteBlockInstrument.BASS)
			.sound(SoundType.WOOD)
			.strength(2, 5);
	private static final Supplier<Properties> STANDARD_WOOD_PROPERTIES_NO_OVERLAY =
			() -> Block.Properties.of()
					.mapColor(MapColor.WOOD)
					.ignitedByLava()
					.instrument(NoteBlockInstrument.BASS)
					.sound(SoundType.WOOD)
					.strength(2, 5)
					.isViewBlocking((state, blockReader, pos) -> false);
	private static final Supplier<Properties> STANDARD_WOOD_PROPERTIES_NO_OCCLUSION = () -> STANDARD_WOOD_PROPERTIES_NO_OVERLAY.get().noOcclusion();
	private static final Supplier<Properties> DEFAULT_METAL_PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.METAL)
			.sound(SoundType.METAL)
			.requiresCorrectToolForDrops()
			.strength(3, 15);
	private static final Supplier<Properties> METAL_PROPERTIES_NO_OVERLAY =
			() -> Block.Properties.of()
					.mapColor(MapColor.METAL)
					.sound(SoundType.METAL)
					.strength(3, 15)
					.requiresCorrectToolForDrops()
					.isViewBlocking((state, blockReader, pos) -> false);
	public static final Supplier<Properties> METAL_PROPERTIES_NO_OCCLUSION = () -> METAL_PROPERTIES_NO_OVERLAY.get().noOcclusion();
	private static final Supplier<Properties> METAL_PROPERTIES_DYNAMIC = () -> METAL_PROPERTIES_NO_OCCLUSION.get().dynamicShape();

	private IEBlocks()
	{
	}

	public static final Map<ResourceLocation, BlockEntry<SlabBlock>> TO_SLAB = new HashMap<>();
	public static final Map<ResourceLocation, BlockEntry<IEStairsBlock>> TO_STAIRS = new HashMap<>();
	public static final Map<ResourceLocation, BlockEntry<IEWallBlock>> TO_WALL = new HashMap<>();

	public static final class StoneDecoration
	{
		public static final BlockEntry<IEBaseBlock> COKEBRICK = BlockEntry.simple("cokebrick", STONE_DECO_GBRICK_PROPS);
		public static final BlockEntry<IEBaseBlock> BLASTBRICK = BlockEntry.simple("blastbrick", STONE_DECO_BRICK_PROPS);
		public static final BlockEntry<IEBaseBlock> BLASTBRICK_REINFORCED = BlockEntry.simple(
				"blastbrick_reinforced", () -> Block.Properties.of()
						.mapColor(MapColor.COLOR_RED)
						.instrument(NoteBlockInstrument.BASEDRUM)
						.sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(2.5f, 12)
		);
		public static final BlockEntry<IEBaseBlock> SLAG_BRICK = BlockEntry.simple("slag_brick", STONE_DECO_GBRICK_PROPS);
		public static final BlockEntry<IEBaseBlock> CLINKER_BRICK = BlockEntry.simple("clinker_brick", STONE_DECO_BRICK_PROPS);
		public static final BlockEntry<IEBaseBlock> CLINKER_BRICK_SILL = BlockEntry.simple("clinker_brick_sill", STONE_DECO_BRICK_PROPS);
		public static final BlockEntry<HorizontalFacingBlock> CLINKER_BRICK_QUOIN = new BlockEntry<>(
				"clinker_brick_quoin", STONE_DECO_BRICK_PROPS, HorizontalFacingBlock::new);
		public static final BlockEntry<IEBaseBlock> COKE = BlockEntry.simple(
				"coke", () -> Block.Properties.of()
						.mapColor(MapColor.STONE)
						.instrument(NoteBlockInstrument.BASEDRUM)
						.sound(SoundType.STONE).requiresCorrectToolForDrops().strength(5, 6));
		public static final BlockEntry<SlagGravelBlock> SLAG_GRAVEL = new BlockEntry<>(
				"slag_gravel",
				() -> Block.Properties.of()
						.mapColor(MapColor.STONE)
						.instrument(NoteBlockInstrument.SNARE)
						.strength(0.6F).sound(SoundType.GRAVEL),
				SlagGravelBlock::new
		);
		public static final BlockEntry<FallingBlock> GRIT_SAND = new BlockEntry<>(
				"grit_sand",
				() -> Block.Properties.of()
						.mapColor(MapColor.SAND)
						.instrument(NoteBlockInstrument.SNARE)
						.strength(0.6F).sound(SoundType.GRAVEL),
				SlagGravelBlock::new
		);

		public static final BlockEntry<IEBaseBlock> HEMPCRETE = BlockEntry.simple("hempcrete", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> HEMPCRETE_BRICK = BlockEntry.simple("hempcrete_brick", STONE_DECO_STONE_BRICK_PROPS);
		public static final BlockEntry<IEBaseBlock> HEMPCRETE_BRICK_CRACKED = BlockEntry.simple("hempcrete_brick_cracked", STONE_DECO_STONE_BRICK_PROPS);
		public static final BlockEntry<IEBaseBlock> HEMPCRETE_CHISELED = BlockEntry.simple("hempcrete_chiseled", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> HEMPCRETE_PILLAR = BlockEntry.simple("hempcrete_pillar", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> CONCRETE = BlockEntry.simple("concrete", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> CONCRETE_BRICK = BlockEntry.simple("concrete_brick", STONE_DECO_STONE_BRICK_PROPS);
		public static final BlockEntry<IEBaseBlock> CONCRETE_BRICK_CRACKED = BlockEntry.simple("concrete_brick_cracked", STONE_DECO_STONE_BRICK_PROPS);
		public static final BlockEntry<IEBaseBlock> CONCRETE_CHISELED = BlockEntry.simple("concrete_chiseled", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> CONCRETE_PILLAR = BlockEntry.simple("concrete_pillar", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> CONCRETE_TILE = BlockEntry.simple("concrete_tile", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> CONCRETE_LEADED = BlockEntry.simple(
				"concrete_leaded", STONE_DECO_LEADED_PROPS
		);
		public static final BlockEntry<IEBaseBlock> INSULATING_GLASS = BlockEntry.simple(
				"insulating_glass", STONE_DECO_PROPS_NOT_SOLID
		);
		public static final BlockEntry<IEBaseBlock> SLAG_GLASS = BlockEntry.simple(
				"slag_glass", STONE_DECO_PROPS_NOT_SOLID, shouldHave -> shouldHave.setLightOpacity(8)
		);
		public static final BlockEntry<IEBaseBlock> CONCRETE_SPRAYED = BlockEntry.simple(
				"concrete_sprayed", () -> Block.Properties.of()
						.mapColor(MapColor.STONE)
						.instrument(NoteBlockInstrument.BASEDRUM)
						.strength(.2F, 1)
						.noOcclusion());
		public static final BlockEntry<IEBaseBlock> ALLOYBRICK = BlockEntry.simple("alloybrick", STONE_DECO_STONE_BRICK_PROPS);

		//TODO possibly merge into a single block with "arbitrary" height?
		public static final BlockEntry<PartialConcreteBlock> CONCRETE_SHEET = new BlockEntry<>(
				"concrete_sheet", PartialConcreteBlock::makeProperties, props -> new PartialConcreteBlock(props, 1)
		);
		public static final BlockEntry<PartialConcreteBlock> CONCRETE_QUARTER = new BlockEntry<>(
				"concrete_quarter", PartialConcreteBlock::makeProperties, props -> new PartialConcreteBlock(props, 4)
		);
		public static final BlockEntry<PartialConcreteBlock> CONCRETE_THREE_QUARTER = new BlockEntry<>(
				"concrete_three_quarter",
				PartialConcreteBlock::makeProperties, props -> new PartialConcreteBlock(props, 12)
		);

		public static final BlockEntry<HorizontalFacingEntityBlock<CoresampleBlockEntity>> CORESAMPLE = new BlockEntry<>(
				"coresample",
				// TODO move bounds code into the block impl and get rid of dynamic shapes
				dynamicShape(STONE_DECO_PROPS_NOT_SOLID),
				p -> new HorizontalFacingEntityBlock<>(IEBlockEntities.CORE_SAMPLE, p)
		);

		public static final BlockEntry<IEBaseBlock> DUROPLAST = BlockEntry.simple(
				"duroplast", STONE_DECO_PROPS_NOT_SOLID
		);

		private static void init()
		{
		}
	}

	public static final class Metals
	{
		public static final Map<EnumMetals, BlockEntry<Block>> ORES = new EnumMap<>(EnumMetals.class);
		public static final Map<EnumMetals, BlockEntry<Block>> DEEPSLATE_ORES = new EnumMap<>(EnumMetals.class);
		public static final Map<EnumMetals, BlockEntry<Block>> RAW_ORES = new EnumMap<>(EnumMetals.class);
		public static final Map<EnumMetals, BlockEntry<Block>> STORAGE = new EnumMap<>(EnumMetals.class);
		public static final Map<EnumMetals, BlockEntry<IEBaseBlock>> SHEETMETAL = new EnumMap<>(EnumMetals.class);

		private static void init()
		{
			for(EnumMetals m : EnumMetals.values())
			{
				String name = m.tagName();
				BlockEntry<Block> storage;
				BlockEntry<Block> ore = null;
				BlockEntry<Block> deepslateOre = null;
				BlockEntry<Block> rawOre = null;
				BlockEntry<IEBaseBlock> sheetmetal = BlockEntry.simple("sheetmetal_"+name, SHEETMETAL_PROPERTIES);
				registerSlab(sheetmetal);
				SHEETMETAL.put(m, sheetmetal);
				if(m.shouldAddOre())
				{
					ore = new BlockEntry<>(BlockEntry.simple("ore_"+name,
							() -> Block.Properties.of()
									.mapColor(MapColor.STONE)
									.instrument(NoteBlockInstrument.BASEDRUM)
									.strength(3, 3)
									.requiresCorrectToolForDrops()));
					deepslateOre = new BlockEntry<>(BlockEntry.simple("deepslate_ore_"+name,
							() -> Block.Properties.of()
									.mapColor(MapColor.STONE)
									.instrument(NoteBlockInstrument.BASEDRUM)
									.mapColor(MapColor.DEEPSLATE)
									.sound(SoundType.DEEPSLATE)
									.strength(4.5f, 3)
									.requiresCorrectToolForDrops()));
					rawOre = new BlockEntry<>(BlockEntry.simple("raw_block_"+name,
							() -> Block.Properties.of()
									.mapColor(MapColor.STONE)
									.instrument(NoteBlockInstrument.BASEDRUM)
									.strength(5, 6)
									.requiresCorrectToolForDrops()));
				}
				if(!m.isVanillaMetal())
				{
					BlockEntry<IEBaseBlock> storageIE = BlockEntry.simple(
							"storage_"+name, () -> Block.Properties.of()
									.mapColor(MapColor.METAL)
									.sound(m==EnumMetals.STEEL?SoundType.NETHERITE_BLOCK: SoundType.METAL)
									.strength(5, 10)
									.requiresCorrectToolForDrops());
					registerSlab(storageIE);
					storage = new BlockEntry<>(storageIE);
				}
				else if(m==EnumMetals.IRON)
				{
					storage = new BlockEntry<>(Blocks.IRON_BLOCK);
					ore = new BlockEntry<>(Blocks.IRON_ORE);
					deepslateOre = new BlockEntry<>(Blocks.DEEPSLATE_IRON_ORE);
					rawOre = new BlockEntry<>(Blocks.RAW_IRON_BLOCK);
				}
				else if(m==EnumMetals.GOLD)
				{
					storage = new BlockEntry<>(Blocks.GOLD_BLOCK);
					ore = new BlockEntry<>(Blocks.GOLD_ORE);
					deepslateOre = new BlockEntry<>(Blocks.DEEPSLATE_GOLD_ORE);
					rawOre = new BlockEntry<>(Blocks.RAW_GOLD_BLOCK);
				}
				else if(m==EnumMetals.COPPER)
				{
					storage = new BlockEntry<>(Blocks.COPPER_BLOCK);
					ore = new BlockEntry<>(Blocks.COPPER_ORE);
					deepslateOre = new BlockEntry<>(Blocks.DEEPSLATE_COPPER_ORE);
					rawOre = new BlockEntry<>(Blocks.RAW_COPPER_BLOCK);
				}
				else
					throw new RuntimeException("Unkown vanilla metal: "+m.name());
				STORAGE.put(m, storage);
				if(ore!=null)
					ORES.put(m, ore);
				if(deepslateOre!=null)
					DEEPSLATE_ORES.put(m, deepslateOre);
				if(deepslateOre!=null)
					RAW_ORES.put(m, rawOre);
			}
		}
	}

	public static final class WoodenDecoration
	{
		public static final Map<TreatedWoodStyles, BlockEntry<IEBaseBlock>> TREATED_WOOD = new EnumMap<>(TreatedWoodStyles.class);
		public static final BlockEntry<FenceBlock> TREATED_FENCE = BlockEntry.fence("treated_fence", STANDARD_WOOD_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<ScaffoldingBlock> TREATED_SCAFFOLDING = BlockEntry.scaffolding("treated_scaffold", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION);
		public static final BlockEntry<PostBlock> TREATED_POST = BlockEntry.post("treated_post", STANDARD_WOOD_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<SawdustBlock> SAWDUST = new BlockEntry<>(
				"sawdust",
				() -> Block.Properties.of()
						.mapColor(MapColor.SAND)
						.ignitedByLava()
						.instrument(NoteBlockInstrument.BASS)
						.sound(SoundType.SAND)
						.strength(0.5F)
						.noCollission().noOcclusion(),
				SawdustBlock::new
		);
		public static final BlockEntry<IEBaseBlock> FIBERBOARD = BlockEntry.simple("fiberboard",
				() -> Block.Properties.of()
						.mapColor(MapColor.WOOD)
						.ignitedByLava()
						.instrument(NoteBlockInstrument.BASS)
						.strength(1.25f, 1)
		);

		private static void init()
		{
			for(TreatedWoodStyles style : TreatedWoodStyles.values())
			{
				BlockEntry<IEBaseBlock> baseBlock = BlockEntry.simple(
						"treated_wood_"+style.name().toLowerCase(Locale.US), STANDARD_WOOD_PROPERTIES, shouldHave -> shouldHave.setHasFlavour(true)
				);
				TREATED_WOOD.put(style, baseBlock);
				registerSlab(baseBlock);
				registerStairs(baseBlock);
			}
		}
	}

	public static final class WoodenDevices
	{
		public static final BlockEntry<CraftingTableBlock> CRAFTING_TABLE = new BlockEntry<>(
				"craftingtable", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, CraftingTableBlock::new
		);
		public static final BlockEntry<DeskBlock<ModWorkbenchBlockEntity>> WORKBENCH = new BlockEntry<>(
				"workbench", DeskBlock.PROPERTIES, p -> new DeskBlock<>(IEBlockEntities.MOD_WORKBENCH, p)
		);
		public static final BlockEntry<DeskBlock<CircuitTableBlockEntity>> CIRCUIT_TABLE = new BlockEntry<>(
				"circuit_table", DeskBlock.PROPERTIES, p -> new DeskBlock<>(IEBlockEntities.CIRCUIT_TABLE, p)
		);
		public static final BlockEntry<GunpowderBarrelBlock> GUNPOWDER_BARREL = new BlockEntry<>(
				"gunpowder_barrel", GunpowderBarrelBlock.PROPERTIES, GunpowderBarrelBlock::new
		);
		public static final BlockEntry<IEEntityBlock<?>> WOODEN_BARREL = BlockEntry.barrel("wooden_barrel", false);
		public static final BlockEntry<TurntableBlock> TURNTABLE = new BlockEntry<>("turntable", STANDARD_WOOD_PROPERTIES, TurntableBlock::new);
		public static final BlockEntry<IEEntityBlock<WoodenCrateBlockEntity>> CRATE = new BlockEntry<>(
				"crate", STANDARD_WOOD_PROPERTIES, p -> new IEEntityBlock<>(IEBlockEntities.WOODEN_CRATE, p, false)
		);
		public static final BlockEntry<IEEntityBlock<WoodenCrateBlockEntity>> REINFORCED_CRATE = new BlockEntry<>(
				"reinforced_crate",
				() -> Properties.of()
						.sound(SoundType.WOOD)
						.strength(2, 1200000)
						.mapColor(MapColor.WOOD)
						.ignitedByLava()
						.instrument(NoteBlockInstrument.BASS),
				p -> new IEEntityBlock<>(IEBlockEntities.WOODEN_CRATE, p, false)
		);
		public static final BlockEntry<IEEntityBlock<SorterBlockEntity>> SORTER = new BlockEntry<>(
				"sorter", STANDARD_WOOD_PROPERTIES, p -> new IEEntityBlock<>(IEBlockEntities.SORTER, p)
		);
		public static final BlockEntry<ItemBatcherBlock> ITEM_BATCHER = new BlockEntry<>(
				"item_batcher", STANDARD_WOOD_PROPERTIES, ItemBatcherBlock::new
		);
		public static final BlockEntry<IEEntityBlock<FluidSorterBlockEntity>> FLUID_SORTER = new BlockEntry<>(
				"fluid_sorter", STANDARD_WOOD_PROPERTIES, p -> new IEEntityBlock<>(IEBlockEntities.FLUID_SORTER, p)
		);
		public static final BlockEntry<WindmillBlock> WINDMILL = new BlockEntry<>(
				// TODO move shape into block impl and get rid of dynamic shapes
				"windmill", dynamicShape(STANDARD_WOOD_PROPERTIES_NO_OCCLUSION), WindmillBlock::new
		);
		public static final BlockEntry<WatermillBlock> WATERMILL = new BlockEntry<>(
				"watermill", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, WatermillBlock::new
		);
		//TODO move to deco?
		public static final BlockEntry<WallmountBlock> TREATED_WALLMOUNT = BlockEntry.wallmount("treated_wallmount", STANDARD_WOOD_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<HorizontalFacingEntityBlock<LogicUnitBlockEntity>> LOGIC_UNIT = new BlockEntry<>(
				"logic_unit", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, p -> new HorizontalFacingEntityBlock<>(IEBlockEntities.LOGIC_UNIT, p)
		);

		private static void init()
		{
		}
	}


	public static final class MetalDecoration
	{
		public static final BlockEntry<IEBaseBlock> LV_COIL = BlockEntry.simple("coil_lv", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> MV_COIL = BlockEntry.simple("coil_mv", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> HV_COIL = BlockEntry.simple("coil_hv", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> ENGINEERING_RS = BlockEntry.simple("rs_engineering", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> ENGINEERING_HEAVY = BlockEntry.simple("heavy_engineering", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> ENGINEERING_LIGHT = BlockEntry.simple("light_engineering", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> GENERATOR = BlockEntry.simple("generator", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> RADIATOR = BlockEntry.simple("radiator", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<FenceBlock> STEEL_FENCE = BlockEntry.fence("steel_fence", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<FenceBlock> ALU_FENCE = BlockEntry.fence("alu_fence", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<WallmountBlock> STEEL_WALLMOUNT = BlockEntry.wallmount("steel_wallmount", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<WallmountBlock> ALU_WALLMOUNT = BlockEntry.wallmount("alu_wallmount", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<PostBlock> STEEL_POST = BlockEntry.post("steel_post", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<PostBlock> ALU_POST = BlockEntry.post("alu_post", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<LanternBlock> LANTERN = new BlockEntry<>("lantern", LanternBlock.PROPERTIES, LanternBlock::new);
		public static final BlockEntry<StructuralArmBlock> STEEL_SLOPE = new BlockEntry<>(
				"steel_slope", METAL_PROPERTIES_DYNAMIC, StructuralArmBlock::new
		);
		public static final BlockEntry<StructuralArmBlock> ALU_SLOPE = new BlockEntry<>(
				"alu_slope", METAL_PROPERTIES_DYNAMIC, StructuralArmBlock::new
		);
		public static final Map<CoverType, BlockEntry<MetalLadderBlock>> METAL_LADDER = new EnumMap<>(CoverType.class);
		public static final Map<MetalScaffoldingType, BlockEntry<ScaffoldingBlock>> STEEL_SCAFFOLDING = new EnumMap<>(MetalScaffoldingType.class);
		public static final Map<MetalScaffoldingType, BlockEntry<ScaffoldingBlock>> ALU_SCAFFOLDING = new EnumMap<>(MetalScaffoldingType.class);
		public static final Map<DyeColor, BlockEntry<IEBaseBlock>> COLORED_SHEETMETAL = new EnumMap<>(DyeColor.class);

		private static void init()
		{
			for(DyeColor dye : DyeColor.values())
			{
				BlockEntry<IEBaseBlock> sheetmetal = BlockEntry.simple(
						"sheetmetal_colored_"+dye.getName(), SHEETMETAL_PROPERTIES
				);
				COLORED_SHEETMETAL.put(dye, sheetmetal);
				registerSlab(sheetmetal);
			}
			for(CoverType type : CoverType.values())
				METAL_LADDER.put(type, new BlockEntry<>(
						"metal_ladder_"+type.name().toLowerCase(Locale.US),
						METAL_PROPERTIES_NO_OCCLUSION,
						p -> new MetalLadderBlock(type, p)
				));
			for(MetalScaffoldingType type : MetalScaffoldingType.values())
			{
				String name = type.name().toLowerCase(Locale.ENGLISH);
				BlockEntry<ScaffoldingBlock> steelBlock = BlockEntry.scaffolding("steel_scaffolding_"+name, METAL_PROPERTIES_NO_OCCLUSION);
				BlockEntry<ScaffoldingBlock> aluBlock = BlockEntry.scaffolding("alu_scaffolding_"+name, METAL_PROPERTIES_NO_OCCLUSION);
				STEEL_SCAFFOLDING.put(type, steelBlock);
				ALU_SCAFFOLDING.put(type, aluBlock);
				registerSlab(steelBlock);
				registerSlab(aluBlock);
				registerStairs(steelBlock);
				registerStairs(aluBlock);
			}
		}
	}

	public static final class MetalDevices
	{
		public static final BlockEntry<RazorWireBlock> RAZOR_WIRE = new BlockEntry<>(
				"razor_wire", RazorWireBlock.PROPERTIES, RazorWireBlock::new
		);
		public static final BlockEntry<HorizontalFacingEntityBlock<ToolboxBlockEntity>> TOOLBOX = new BlockEntry<>(
				// TODO move shape into block
				"toolbox_block", dynamicShape(METAL_PROPERTIES_NO_OVERLAY), p -> new HorizontalFacingEntityBlock<>(IEBlockEntities.TOOLBOX, p)
		);
		public static final BlockEntry<IEEntityBlock<CapacitorBlockEntity>> CAPACITOR_LV = new BlockEntry<>(
				"capacitor_lv", DEFAULT_METAL_PROPERTIES, p -> new IEEntityBlock<>(IEBlockEntities.CAPACITOR_LV, p)
		);
		public static final BlockEntry<IEEntityBlock<CapacitorBlockEntity>> CAPACITOR_MV = new BlockEntry<>(
				"capacitor_mv", DEFAULT_METAL_PROPERTIES, p -> new IEEntityBlock<>(IEBlockEntities.CAPACITOR_MV, p)
		);
		public static final BlockEntry<IEEntityBlock<CapacitorBlockEntity>> CAPACITOR_HV = new BlockEntry<>(
				"capacitor_hv", DEFAULT_METAL_PROPERTIES, p -> new IEEntityBlock<>(IEBlockEntities.CAPACITOR_HV, p)
		);
		public static final BlockEntry<IEEntityBlock<CapacitorCreativeBlockEntity>> CAPACITOR_CREATIVE = new BlockEntry<>(
				"capacitor_creative", DEFAULT_METAL_PROPERTIES, p -> new IEEntityBlock<>(IEBlockEntities.CAPACITOR_CREATIVE, p)
		);
		public static final BlockEntry<IEEntityBlock<?>> BARREL = BlockEntry.barrel("metal_barrel", true);
		public static final BlockEntry<FluidPumpBlock> FLUID_PUMP = new BlockEntry<>(
				// TODO make non-dynamic
				"fluid_pump", METAL_PROPERTIES_DYNAMIC, FluidPumpBlock::new
		);
		public static final BlockEntry<IEEntityBlock<FluidPlacerBlockEntity>> FLUID_PLACER = new BlockEntry<>(
				"fluid_placer", METAL_PROPERTIES_NO_OCCLUSION, p -> new IEEntityBlock<>(IEBlockEntities.FLUID_PLACER, p)
		);
		public static final BlockEntry<BlastFurnacePreheaterBlock> BLAST_FURNACE_PREHEATER = new BlockEntry<>(
				"blastfurnace_preheater", METAL_PROPERTIES_NO_OCCLUSION, BlastFurnacePreheaterBlock::new
		);
		public static final BlockEntry<FurnaceHeaterBlock> FURNACE_HEATER = new BlockEntry<>(
				"furnace_heater", DEFAULT_METAL_PROPERTIES, FurnaceHeaterBlock::new
		);
		public static final BlockEntry<HorizontalFacingEntityBlock<DynamoBlockEntity>> DYNAMO = new BlockEntry<>(
				"dynamo", DEFAULT_METAL_PROPERTIES, p -> new HorizontalFacingEntityBlock<>(IEBlockEntities.DYNAMO, p)
		);
		public static final BlockEntry<IEEntityBlock<ThermoelectricGenBlockEntity>> THERMOELECTRIC_GEN = new BlockEntry<>(
				"thermoelectric_generator", DEFAULT_METAL_PROPERTIES, p -> new IEEntityBlock<>(IEBlockEntities.THERMOELECTRIC_GEN, p)
		);
		public static final BlockEntry<ElectricLanternBlock> ELECTRIC_LANTERN = new BlockEntry<>(
				"electric_lantern", ElectricLanternBlock.PROPERTIES, ElectricLanternBlock::new
		);
		public static final BlockEntry<HorizontalFacingEntityBlock<ChargingStationBlockEntity>> CHARGING_STATION = new BlockEntry<>(
				// TODO move shape into block impl
				"charging_station", dynamicShape(METAL_PROPERTIES_NO_OVERLAY), p -> new HorizontalFacingEntityBlock<>(IEBlockEntities.CHARGING_STATION, p)
		);
		public static final BlockEntry<FluidPipeBlock> FLUID_PIPE = new BlockEntry<>("fluid_pipe", METAL_PROPERTIES_DYNAMIC, FluidPipeBlock::new);
		public static final BlockEntry<SampleDrillBlock> SAMPLE_DRILL = new BlockEntry<>("sample_drill", METAL_PROPERTIES_NO_OCCLUSION, SampleDrillBlock::new);
		// TODO make non-dynamic
		public static final BlockEntry<TeslaCoilBlock> TESLA_COIL = new BlockEntry<>("tesla_coil", METAL_PROPERTIES_DYNAMIC, TeslaCoilBlock::new);
		public static final BlockEntry<FloodlightBlock> FLOODLIGHT = new BlockEntry<>("floodlight", FloodlightBlock.PROPERTIES, FloodlightBlock::new);
		// TODO make both turrets non-dynamic
		public static final BlockEntry<TurretBlock<TurretChemBlockEntity>> TURRET_CHEM = new BlockEntry<>(
				"turret_chem", METAL_PROPERTIES_DYNAMIC, p -> new TurretBlock<>(IEBlockEntities.TURRET_CHEM, p)
		);
		public static final BlockEntry<TurretBlock<TurretGunBlockEntity>> TURRET_GUN = new BlockEntry<>(
				"turret_gun", METAL_PROPERTIES_DYNAMIC, p -> new TurretBlock<>(IEBlockEntities.TURRET_GUN, p)
		);
		public static final BlockEntry<ClocheBlock> CLOCHE = new BlockEntry<>("cloche", METAL_PROPERTIES_NO_OCCLUSION, ClocheBlock::new);
		public static final Map<IConveyorType<?>, BlockEntry<ConveyorBlock>> CONVEYORS = new HashMap<>();
		public static final Map<EnumMetals, BlockEntry<ChuteBlock>> CHUTES = new EnumMap<>(EnumMetals.class);
		public static final BlockEntry<AnyFacingEntityBlock<ElectromagnetBlockEntity>> ELECTROMAGNET = new BlockEntry<>(
				"electromagnet", DEFAULT_METAL_PROPERTIES, p -> new AnyFacingEntityBlock<>(IEBlockEntities.ELECTROMAGNET, p)
		);
		private static void init()
		{
			for(EnumMetals metal : new EnumMetals[]{EnumMetals.IRON, EnumMetals.STEEL, EnumMetals.ALUMINUM, EnumMetals.COPPER})
				CHUTES.put(metal, new BlockEntry<>("chute_"+metal.tagName(), METAL_PROPERTIES_DYNAMIC, ChuteBlock::new));

		}

		public static void initConveyors()
		{
			Preconditions.checkState(CONVEYORS.isEmpty());
			for(IConveyorType<?> type : ConveyorHandler.getConveyorTypes())
			{
				ResourceLocation rl = type.getId();
				BlockEntry<ConveyorBlock> blockEntry = new BlockEntry<>(
						ConveyorHandler.getRegistryNameFor(rl).getPath(), ConveyorBlock.PROPERTIES, p -> new ConveyorBlock(type, p)
				);
				CONVEYORS.put(type, blockEntry);
				IEItems.REGISTER.register(blockEntry.getId().getPath(), () -> new BlockItemIE(blockEntry.get()));
			}
		}
	}

	public static final class Connectors
	{
		public static final Map<Pair<String, Boolean>, BlockEntry<BasicConnectorBlock<?>>> ENERGY_CONNECTORS = new HashMap<>();
		public static final BlockEntry<BasicConnectorBlock<?>> CONNECTOR_STRUCTURAL = new BlockEntry<>(
				"connector_structural", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IEBlockEntities.CONNECTOR_STRUCTURAL)
		);
		public static final BlockEntry<TransformerBlock> TRANSFORMER = new BlockEntry<>("transformer", ConnectorBlock.PROPERTIES, TransformerBlock::new);
		public static final BlockEntry<PostTransformerBlock> POST_TRANSFORMER = new BlockEntry<>(
				"post_transformer", ConnectorBlock.PROPERTIES, PostTransformerBlock::new
		);
		public static final BlockEntry<TransformerHVBlock> TRANSFORMER_HV = new BlockEntry<>(
				"transformer_hv", ConnectorBlock.PROPERTIES, TransformerHVBlock::new
		);
		public static final BlockEntry<BreakerSwitchBlock<?>> BREAKER_SWITCH = new BlockEntry<>(
				"breaker_switch", ConnectorBlock.PROPERTIES, p -> new BreakerSwitchBlock<>(p, IEBlockEntities.BREAKER_SWITCH)
		);
		public static final BlockEntry<BreakerSwitchBlock<?>> REDSTONE_BREAKER = new BlockEntry<>(
				"redstone_breaker", ConnectorBlock.PROPERTIES, p -> new BreakerSwitchBlock<>(p, IEBlockEntities.REDSTONE_BREAKER)
		);
		public static final BlockEntry<EnergyMeterBlock> CURRENT_TRANSFORMER = new BlockEntry<>("current_transformer", ConnectorBlock.PROPERTIES, EnergyMeterBlock::new);
		public static final BlockEntry<BasicConnectorBlock<?>> CONNECTOR_REDSTONE = new BlockEntry<>(
				"connector_redstone", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IEBlockEntities.CONNECTOR_REDSTONE)
		);
		public static final BlockEntry<BasicConnectorBlock<?>> CONNECTOR_PROBE = new BlockEntry<>(
				"connector_probe", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IEBlockEntities.CONNECTOR_PROBE)
		);
		public static final BlockEntry<BasicConnectorBlock<?>> CONNECTOR_BUNDLED = new BlockEntry<>(
				"connector_bundled", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IEBlockEntities.CONNECTOR_BUNDLED)
		);
		public static final BlockEntry<FeedthroughBlock> FEEDTHROUGH = new BlockEntry<>("feedthrough", ConnectorBlock.PROPERTIES, FeedthroughBlock::new);

		public static BlockEntry<BasicConnectorBlock<?>> getEnergyConnector(String cat, boolean relay)
		{
			return ENERGY_CONNECTORS.get(Pair.of(cat, relay));
		}

		private static void init()
		{
			for(String cat : new String[]{WireType.LV_CATEGORY, WireType.MV_CATEGORY, WireType.HV_CATEGORY})
			{
				ENERGY_CONNECTORS.put(Pair.of(cat, false), BasicConnectorBlock.forPower(cat, false));
				ENERGY_CONNECTORS.put(Pair.of(cat, true), BasicConnectorBlock.forPower(cat, true));
			}
		}
	}

	public static final class Cloth
	{
		public static final BlockEntry<CushionBlock> CUSHION = new BlockEntry<>("cushion", CushionBlock.PROPERTIES, CushionBlock::new);
		public static final BlockEntry<BalloonBlock> BALLOON = new BlockEntry<>("balloon", BalloonBlock.PROPERTIES, BalloonBlock::new);
		public static final BlockEntry<StripCurtainBlock> STRIP_CURTAIN = new BlockEntry<>("strip_curtain", StripCurtainBlock.PROPERTIES, StripCurtainBlock::new);
		public static final BlockEntry<ShaderBannerStandingBlock> SHADER_BANNER = new BlockEntry<>(
				"shader_banner", ShaderBannerBlock.PROPERTIES, ShaderBannerStandingBlock::new
		);
		public static final BlockEntry<ShaderBannerWallBlock> SHADER_BANNER_WALL = new BlockEntry<>(
				"shader_banner_wall", ShaderBannerBlock.PROPERTIES, ShaderBannerWallBlock::new
		);

		private static void init()
		{
		}
	}

	public static final class Misc
	{
		public static final BlockEntry<HempBlock> HEMP_PLANT = new BlockEntry<>("hemp", HempBlock.PROPERTIES, HempBlock::new);
		public static final BlockEntry<PottedHempBlock> POTTED_HEMP = new BlockEntry<>("potted_hemp", PottedHempBlock.PROPERTIES, PottedHempBlock::new);
		public static final BlockEntry<FakeLightBlock> FAKE_LIGHT = new BlockEntry<>("fake_light", FakeLightBlock.PROPERTIES, FakeLightBlock::new);

		private static void init()
		{
		}
	}

	private static <T extends Block & IIEBlock> void registerStairs(BlockEntry<T> fullBlock)
	{
		TO_STAIRS.put(fullBlock.getId(), new BlockEntry<>(
				"stairs_"+fullBlock.getId().getPath(),
				fullBlock::getProperties,
				p -> new IEStairsBlock(p, fullBlock)
		));
	}

	private static <T extends Block & IIEBlock> void registerSlab(BlockEntry<T> fullBlock)
	{
		TO_SLAB.put(fullBlock.getId(), new BlockEntry<>(
				"slab_"+fullBlock.getId().getPath(),
				fullBlock::getProperties,
				p -> new BlockIESlab<>(p, fullBlock)
		));
	}

	private static <T extends Block & IIEBlock> void registerWall(BlockEntry<T> fullBlock)
	{
		TO_WALL.put(fullBlock.getId(), new BlockEntry<>(
				"wall_"+fullBlock.getId().getPath(),
				fullBlock::getProperties,
				p -> new IEWallBlock(p, fullBlock)
		));
	}

	private static Supplier<BlockBehaviour.Properties> dynamicShape(Supplier<BlockBehaviour.Properties> baseProps)
	{
		return () -> baseProps.get().dynamicShape();
	}

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		StoneDecoration.init();
		Metals.init();
		WoodenDecoration.init();
		WoodenDevices.init();
		MetalDecoration.init();
		MetalDevices.init();
		Connectors.init();
		Cloth.init();
		Misc.init();
		registerSlab(StoneDecoration.COKEBRICK);
		registerSlab(StoneDecoration.BLASTBRICK);
		registerSlab(StoneDecoration.BLASTBRICK_REINFORCED);
		registerSlab(StoneDecoration.SLAG_BRICK);
		registerSlab(StoneDecoration.CLINKER_BRICK);
		registerSlab(StoneDecoration.COKE);
		registerSlab(StoneDecoration.HEMPCRETE);
		registerSlab(StoneDecoration.HEMPCRETE_BRICK);
		registerSlab(StoneDecoration.CONCRETE);
		registerSlab(StoneDecoration.CONCRETE_BRICK);
		registerSlab(StoneDecoration.CONCRETE_TILE);
		registerSlab(StoneDecoration.CONCRETE_LEADED);
		registerSlab(StoneDecoration.INSULATING_GLASS);
		registerSlab(StoneDecoration.ALLOYBRICK);
		registerStairs(StoneDecoration.SLAG_BRICK);
		registerStairs(StoneDecoration.CLINKER_BRICK);
		registerStairs(StoneDecoration.HEMPCRETE);
		registerStairs(StoneDecoration.HEMPCRETE_BRICK);
		registerStairs(StoneDecoration.CONCRETE);
		registerStairs(StoneDecoration.CONCRETE_BRICK);
		registerStairs(StoneDecoration.CONCRETE_TILE);
		registerStairs(StoneDecoration.CONCRETE_LEADED);
		registerWall(StoneDecoration.SLAG_BRICK);
		registerWall(StoneDecoration.CLINKER_BRICK);

		for(BlockEntry<?> entry : BlockEntry.ALL_ENTRIES)
		{
			if(entry==Misc.FAKE_LIGHT||entry==Misc.POTTED_HEMP||entry==StoneDecoration.CORESAMPLE||
					entry==MetalDevices.TOOLBOX||entry==Cloth.SHADER_BANNER||entry==Cloth.SHADER_BANNER_WALL||
					entry==Misc.HEMP_PLANT||entry==Connectors.POST_TRANSFORMER||IEFluids.ALL_FLUID_BLOCKS.contains(entry))
				continue;
			Function<Block, BlockItemIE> toItem;
			if(entry==Cloth.BALLOON)
				toItem = BlockItemBalloon::new;
			else if(entry==Connectors.TRANSFORMER)
				toItem = TransformerBlockItem::new;
			else if(entry==MetalDevices.CAPACITOR_LV)
				toItem = block -> new BlockItemCapacitor(block, IEServerConfig.MACHINES.lvCapConfig);
			else if(entry==MetalDevices.CAPACITOR_MV)
				toItem = block -> new BlockItemCapacitor(block, IEServerConfig.MACHINES.mvCapConfig);
			else if(entry==MetalDevices.CAPACITOR_HV)
				toItem = block -> new BlockItemCapacitor(block, IEServerConfig.MACHINES.hvCapConfig);
			else if(entry==WoodenDevices.CRATE||entry==WoodenDevices.REINFORCED_CRATE)
				toItem = BlockItemIE.BlockItemIENoInventory::new;
			else
				toItem = BlockItemIE::new;
			if(entry==StoneDecoration.COKE)
				toItem = toItem.andThen(b -> b.setBurnTime(10*IEItems.COKE_BURN_TIME));
			Function<Block, BlockItemIE> finalToItem = toItem;
			IEItems.REGISTER.register(entry.getId().getPath(), () -> finalToItem.apply(entry.get()));
		}
	}

	public static final class BlockEntry<T extends Block> implements Supplier<T>, ItemLike
	{
		public static final Collection<BlockEntry<?>> ALL_ENTRIES = new ArrayList<>();

		private final RegistryObject<T> regObject;
		private final Supplier<Properties> properties;

		public static BlockEntry<IEBaseBlock> simple(String name, Supplier<Properties> properties, Consumer<IEBaseBlock> extra)
		{
			return new BlockEntry<>(name, properties, p -> Util.make(new IEBaseBlock(p), extra));
		}

		public static BlockEntry<IEBaseBlock> simple(String name, Supplier<Properties> properties)
		{
			return simple(name, properties, $ -> {
			});
		}

		public static BlockEntry<IEEntityBlock<?>> barrel(String name, boolean metal)
		{
			return new BlockEntry<>(name, () -> BarrelBlock.getProperties(metal), p -> BarrelBlock.make(p, metal));
		}

		public static BlockEntry<ScaffoldingBlock> scaffolding(String name, Supplier<Properties> props)
		{
			return new BlockEntry<>(name, props, ScaffoldingBlock::new);
		}

		public static BlockEntry<FenceBlock> fence(String name, Supplier<Properties> props)
		{
			return new BlockEntry<>(name, props, FenceBlock::new);
		}

		public static BlockEntry<PostBlock> post(String name, Supplier<Properties> props)
		{
			return new BlockEntry<>(name, dynamicShape(props), PostBlock::new);
		}

		public static BlockEntry<WallmountBlock> wallmount(String name, Supplier<Properties> props)
		{
			return new BlockEntry<>(name, props, WallmountBlock::new);
		}

		public BlockEntry(String name, Supplier<Properties> properties, Function<Properties, T> make)
		{
			this.properties = properties;
			this.regObject = REGISTER.register(name, () -> make.apply(properties.get()));
			ALL_ENTRIES.add(this);
		}

		public BlockEntry(T existing)
		{
			this.properties = () -> Properties.copy(existing);
			this.regObject = RegistryObject.create(BuiltInRegistries.BLOCK.getKey(existing), ForgeRegistries.BLOCKS);
		}

		@SuppressWarnings("unchecked")
		public BlockEntry(BlockEntry<? extends T> toCopy)
		{
			this.properties = toCopy.properties;
			this.regObject = (RegistryObject<T>)toCopy.regObject;
		}

		@Override
		public T get()
		{
			return regObject.get();
		}

		public BlockState defaultBlockState()
		{
			return get().defaultBlockState();
		}

		public ResourceLocation getId()
		{
			return regObject.getId();
		}

		public Properties getProperties()
		{
			return properties.get();
		}

		@Nonnull
		@Override
		public Item asItem()
		{
			return get().asItem();
		}

		public RegistryObject<? extends Block> getRegObject()
		{
			return regObject;
		}
	}
}
