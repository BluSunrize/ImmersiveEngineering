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
import blusunrize.immersiveengineering.api.Lib.BlockSetTypes;
import blusunrize.immersiveengineering.api.Lib.WoodTypes;
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
import blusunrize.immersiveengineering.common.blocks.metal.WarningSignBlock.WarningSignIcon;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.plant.PottedHempBlock;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleBlockEntity;
import blusunrize.immersiveengineering.common.blocks.stone.PartialConcreteBlock;
import blusunrize.immersiveengineering.common.blocks.stone.SlagGravelBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.BarrelBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.CraftingTableBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO block items
public final class IEBlocks
{
	public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK, Lib.MODID);
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
			.strength(2, 100);
	private static final Supplier<Properties> STONE_DECO_REINFORCED_PROPS = () -> Block.Properties.of()
			.sound(SoundType.STONE)
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(20, 1200);
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
	private static final Supplier<Properties> STANDARD_WOOD_PROPERTIES_NO_OCCLUSION = () -> STANDARD_WOOD_PROPERTIES_NO_OVERLAY.get().noOcclusion().forceSolidOn();
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
	public static final Supplier<Properties> METAL_PROPERTIES_NO_OCCLUSION = () -> METAL_PROPERTIES_NO_OVERLAY.get().noOcclusion().forceSolidOn();
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
		public static final BlockEntry<IEBaseBlock> ALLOYBRICK = BlockEntry.simple("alloybrick", STONE_DECO_GBRICK_PROPS);
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
		public static final BlockEntry<IEBaseBlock> CONCRETE_REINFORCED = BlockEntry.simple(
				"concrete_reinforced", STONE_DECO_REINFORCED_PROPS
		);
		public static final BlockEntry<IEBaseBlock> CONCRETE_REINFORCED_TILE = BlockEntry.simple(
				"concrete_reinforced_tile", STONE_DECO_REINFORCED_PROPS
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
		public static final BlockEntry<FenceGateBlock> TREATED_FENCE_GATE = new BlockEntry<>(
				"treated_fence_gate",
				STANDARD_WOOD_PROPERTIES_NO_OVERLAY,
				blockProps -> new FenceGateBlock(blockProps, SoundEvents.FENCE_GATE_OPEN, SoundEvents.FENCE_GATE_CLOSE)
		);
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
		public static final BlockEntry<IEBaseBlock> WINDOW = new BlockEntry<>(
				"treated_window", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, WindowBlock::new
		);
		public static final BlockEntry<IEBaseBlock> CATWALK = new BlockEntry<>(
				"treated_catwalk", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, blockProps -> new CatwalkBlock(blockProps, false)
		);

		public static final BlockEntry<IEBaseBlock> CATWALK_STAIRS = new BlockEntry<>(
				"treated_catwalk_stairs", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, blockProps -> new CatwalkStairsBlock(blockProps, false)
		);

		public static final BlockEntry<DoorBlock> DOOR = new BlockEntry<>(
				"treated_door", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, blockProps -> new IEDoorBlock(BlockSetTypes.TREATED_WOOD, blockProps)
		);
		public static final BlockEntry<DoorBlock> DOOR_FRAMED = new BlockEntry<>(
				"treated_door_framed", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, blockProps -> new IEDoorBlock(BlockSetTypes.TREATED_WOOD, blockProps)
		);
		public static final BlockEntry<TrapDoorBlock> TRAPDOOR = new BlockEntry<>(
				"treated_trapdoor", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, blockProps -> new IETrapDoorBlock(BlockSetTypes.TREATED_WOOD, blockProps)
		);
		public static final BlockEntry<TrapDoorBlock> TRAPDOOR_FRAMED = new BlockEntry<>(
				"treated_trapdoor_framed", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, blockProps -> new IETrapDoorBlock(BlockSetTypes.TREATED_WOOD, blockProps)
		);

		public static final SignHolder SIGN = SignHolder.of(WoodTypes.TREATED_WOOD, 1f, MapColor.WOOD, NoteBlockInstrument.BASS, true);

		public static final BlockEntry<IEBaseBlock> BASIC_ENGINEERING = BlockEntry.simple(
				"basic_engineering", STANDARD_WOOD_PROPERTIES
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
		public static final BlockEntry<BlueprintShelfBlock> BLUEPRINT_SHELF = new BlockEntry<>(
				"blueprint_shelf", STANDARD_WOOD_PROPERTIES_NO_OCCLUSION, BlueprintShelfBlock::new
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
				"crate", STANDARD_WOOD_PROPERTIES, CrateBlock::new
		);
		public static final BlockEntry<IEEntityBlock<WoodenCrateBlockEntity>> REINFORCED_CRATE = new BlockEntry<>(
				"reinforced_crate",
				() -> Properties.of()
						.sound(SoundType.WOOD)
						.strength(2, 1200)
						.mapColor(MapColor.WOOD)
						.ignitedByLava()
						.instrument(NoteBlockInstrument.BASS),
				CrateBlock::new
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
		public static final BlockEntry<HorizontalFacingEntityBlock<MachineInterfaceBlockEntity>> MACHINE_INTERFACE = new BlockEntry<>(
				"machine_interface", STANDARD_WOOD_PROPERTIES, p -> new HorizontalFacingEntityBlock<>(IEBlockEntities.MACHINE_INTERFACE, p)
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
		public static final BlockEntry<IEBaseBlock> ENGINEERING_RESONANZ = BlockEntry.simple("resonanz_engineering", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> GENERATOR = BlockEntry.simple("generator", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> RADIATOR = BlockEntry.simple("radiator", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<FenceBlock> STEEL_FENCE = BlockEntry.fence("steel_fence", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<FenceGateBlock> STEEL_FENCE_GATE = new BlockEntry<>(
				"steel_fence_gate",
				METAL_PROPERTIES_NO_OVERLAY,
				blockProps -> new FenceGateBlock(blockProps, SoundEvents.COPPER_DOOR_OPEN, SoundEvents.COPPER_DOOR_CLOSE)
		);
		public static final BlockEntry<FenceBlock> ALU_FENCE = BlockEntry.fence("alu_fence", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<FenceGateBlock> ALU_FENCE_GATE = new BlockEntry<>(
				"alu_fence_gate",
				METAL_PROPERTIES_NO_OVERLAY,
				blockProps -> new FenceGateBlock(blockProps, SoundEvents.COPPER_DOOR_OPEN, SoundEvents.COPPER_DOOR_CLOSE)
		);
		public static final BlockEntry<WallmountBlock> STEEL_WALLMOUNT = BlockEntry.wallmount("steel_wallmount", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<WallmountBlock> ALU_WALLMOUNT = BlockEntry.wallmount("alu_wallmount", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<PostBlock> STEEL_POST = BlockEntry.post("steel_post", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<PostBlock> ALU_POST = BlockEntry.post("alu_post", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<LanternBlock> LANTERN = new BlockEntry<>("lantern", LanternBlock.PROPERTIES, LanternBlock::new);
		public static final BlockEntry<CagelampBlock> CAGELAMP = new BlockEntry<>("cagelamp", CagelampBlock.PROPERTIES, CagelampBlock::new);
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
		public static final BlockEntry<IEBaseBlock> STEEL_WINDOW = new BlockEntry<>(
				"steel_window", METAL_PROPERTIES_NO_OCCLUSION, WindowBlock::new
		);
		public static final BlockEntry<IEBaseBlock> ALU_WINDOW = new BlockEntry<>(
				"alu_window", METAL_PROPERTIES_NO_OCCLUSION, WindowBlock::new
		);
		public static final BlockEntry<IEBaseBlock> REINFORCED_WINDOW = new BlockEntry<>(
				"reinforced_window",
				() -> Properties.of()
						.mapColor(MapColor.METAL)
						.sound(SoundType.METAL)
						.strength(20, 1200)
						.requiresCorrectToolForDrops()
						.isViewBlocking((state, blockReader, pos) -> false)
						.noOcclusion()
						.forceSolidOn(),
				WindowBlock::new
		);
		public static final BlockEntry<IEBaseBlock> STEEL_CATWALK = new BlockEntry<>(
				"steel_catwalk", METAL_PROPERTIES_NO_OCCLUSION, blockProps -> new CatwalkBlock(blockProps, true)
		);
		public static final BlockEntry<IEBaseBlock> STEEL_CATWALK_STAIRS = new BlockEntry<>(
				"steel_catwalk_stairs", METAL_PROPERTIES_NO_OCCLUSION, blockProps -> new CatwalkStairsBlock(blockProps, true)
		);
		public static final BlockEntry<IEBaseBlock> ALU_CATWALK = new BlockEntry<>(
				"alu_catwalk", METAL_PROPERTIES_NO_OCCLUSION, blockProps -> new CatwalkBlock(blockProps, true)
		);
		public static final BlockEntry<IEBaseBlock> ALU_CATWALK_STAIRS = new BlockEntry<>(
				"alu_catwalk_stairs", METAL_PROPERTIES_NO_OCCLUSION, blockProps -> new CatwalkStairsBlock(blockProps, true)
		);
		public static final BlockEntry<DoorBlock> STEEL_DOOR = new BlockEntry<>(
				"steel_door", METAL_PROPERTIES_NO_OCCLUSION, blockProps -> new IEDoorBlock(BlockSetTypes.STEEL, blockProps).setLockedByRedstone()
		);
		public static final BlockEntry<TrapDoorBlock> STEEL_TRAPDOOR = new BlockEntry<>(
				"steel_trapdoor", METAL_PROPERTIES_NO_OCCLUSION, blockProps -> new IETrapDoorBlock(BlockSetTypes.STEEL, blockProps).setLockedByRedstone()
		);
		public static final SignHolder STEEL_SIGN = SignHolder.of(WoodTypes.STEEL, 3f, MapColor.METAL, NoteBlockInstrument.IRON_XYLOPHONE, false);
		public static final SignHolder ALU_SIGN = SignHolder.of(WoodTypes.ALUMINUM, 3f, MapColor.METAL, NoteBlockInstrument.IRON_XYLOPHONE, false);

		public static final Map<WarningSignIcon, BlockEntry<IEBaseBlock>> WARNING_SIGNS = new EnumMap<>(WarningSignIcon.class);

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
			for(WarningSignIcon icon : WarningSignIcon.values())
				WARNING_SIGNS.put(icon, new BlockEntry<>(
						"warning_sign_"+icon.getSerializedName(), WarningSignBlock.PROPERTIES, blockProps -> new WarningSignBlock(icon, blockProps)
				));
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
				"charging_station", METAL_PROPERTIES_DYNAMIC, p -> new HorizontalFacingEntityBlock<>(IEBlockEntities.CHARGING_STATION, p)
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
		public static final Map<DyeColor, BlockEntry<ChuteBlock>> DYED_CHUTES = new EnumMap<>(DyeColor.class);
		public static final BlockEntry<AnyFacingEntityBlock<ElectromagnetBlockEntity>> ELECTROMAGNET = new BlockEntry<>(
				"electromagnet", DEFAULT_METAL_PROPERTIES, p -> new AnyFacingEntityBlock<>(IEBlockEntities.ELECTROMAGNET, p)
		);
		public static final BlockEntry<PipeValveBlock> PIPE_VALVE = new BlockEntry<>(
				"pipe_valve", METAL_PROPERTIES_NO_OCCLUSION, PipeValveBlock::new
		);

		private static void init()
		{
			for(EnumMetals metal : new EnumMetals[]{EnumMetals.IRON, EnumMetals.STEEL, EnumMetals.ALUMINUM, EnumMetals.COPPER})
				CHUTES.put(metal, new BlockEntry<>("chute_"+metal.tagName(), METAL_PROPERTIES_DYNAMIC, ChuteBlock::new));
			for(DyeColor dye : DyeColor.values())
				DYED_CHUTES.put(dye, new BlockEntry<>("chute_colored_"+dye.getName(), METAL_PROPERTIES_DYNAMIC, ChuteBlock::new));
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
		public static final BlockEntry<BasicConnectorBlock<?>> REDSTONE_STATE_CELL = new BlockEntry<>(
				"redstone_state_cell", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IEBlockEntities.REDSTONE_STATE_CELL)
		);
		public static final BlockEntry<BasicConnectorBlock<?>> REDSTONE_TIMER = new BlockEntry<>(
				"redstone_timer", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IEBlockEntities.REDSTONE_TIMER)
		);
		public static final BlockEntry<HorizontalFacingEntityBlock<?>> REDSTONE_SWITCHBOARD = new BlockEntry<>(
				"redstone_switchboard", ConnectorBlock.PROPERTIES, p -> new HorizontalFacingEntityBlock<>(IEBlockEntities.REDSTONE_SWITCHBOARD, p)
		);
		public static final BlockEntry<AnyFacingEntityBlock<?>> SIREN = new BlockEntry<>(
				"siren", ConnectorBlock.PROPERTIES, p -> new AnyFacingEntityBlock<>(IEBlockEntities.SIREN, p)
		);
		public static final BlockEntry<FeedthroughBlock> FEEDTHROUGH = new BlockEntry<>("feedthrough", FeedthroughBlock.PROPERTIES, FeedthroughBlock::new);

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

	public static void init(IEventBus modBus)
	{
		REGISTER.register(modBus);
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
		registerSlab(StoneDecoration.CONCRETE_REINFORCED);
		registerSlab(StoneDecoration.CONCRETE_REINFORCED_TILE);
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
		registerWall(StoneDecoration.HEMPCRETE);
		registerWall(StoneDecoration.HEMPCRETE_BRICK);
		registerWall(StoneDecoration.CONCRETE);
		registerWall(StoneDecoration.CONCRETE_BRICK);
		registerWall(StoneDecoration.CONCRETE_TILE);
		registerWall(StoneDecoration.CONCRETE_LEADED);

		for(BlockEntry<?> entry : BlockEntry.ALL_ENTRIES)
		{
			if(entry==Misc.FAKE_LIGHT||entry==Misc.POTTED_HEMP||entry==StoneDecoration.CORESAMPLE||
					entry==MetalDevices.TOOLBOX||entry==Cloth.SHADER_BANNER||entry==Cloth.SHADER_BANNER_WALL||
					WoodenDecoration.SIGN.matchesEntries(entry)||MetalDecoration.STEEL_SIGN.matchesEntries(entry)||MetalDecoration.ALU_SIGN.matchesEntries(entry)||
					entry==Misc.HEMP_PLANT||entry==Connectors.POST_TRANSFORMER||IEFluids.ALL_FLUID_BLOCKS.contains(entry))
				continue;
			Function<Block, BlockItemIE> toItem;
			if(entry==Cloth.BALLOON)
				toItem = BlockItemBalloon::new;
			else if(entry==Connectors.TRANSFORMER)
				toItem = TransformerBlockItem::new;
			else if(entry==WoodenDevices.CRATE||entry==WoodenDevices.REINFORCED_CRATE)
				toItem = b -> new BlockItemIE(b, new Item.Properties().stacksTo(1));
			else
				toItem = BlockItemIE::new;
			if(entry==StoneDecoration.COKE)
				toItem = toItem.andThen(b -> b.setBurnTime(10*IEItems.COKE_BURN_TIME));
			Function<Block, BlockItemIE> finalToItem = toItem;
			IEItems.REGISTER.register(entry.getId().getPath(), () -> finalToItem.apply(entry.get()));
		}
		// Signs
		WoodenDecoration.SIGN.registerItems(IEItems.REGISTER);
		MetalDecoration.STEEL_SIGN.registerItems(IEItems.REGISTER);
		MetalDecoration.ALU_SIGN.registerItems(IEItems.REGISTER);
	}

	public record SignHolder(String baseName, BlockEntry<IESignBlocks.Standing> sign,
							 BlockEntry<IESignBlocks.Wall> wall, BlockEntry<IESignBlocks.Hanging> hanging,
							 BlockEntry<IESignBlocks.WallHanging> wallHanging)
	{
		public static SignHolder of(WoodType wood, float strength, MapColor mapColor, NoteBlockInstrument nbi, boolean ignite)
		{
			String baseName = ResourceLocation.parse(wood.name()).getPath();
			BlockEntry<IESignBlocks.Standing> sign = new BlockEntry<>(
					baseName+"_sign", buildProperties(strength, mapColor, nbi, ignite, null),
					blockProps -> new IESignBlocks.Standing(wood, blockProps)
			);
			BlockEntry<IESignBlocks.Wall> wall = new BlockEntry<>(
					baseName+"_wall_sign", buildProperties(strength, mapColor, nbi, ignite, sign::get),
					blockProps -> new IESignBlocks.Wall(wood, blockProps)
			);
			BlockEntry<IESignBlocks.Hanging> hanging = new BlockEntry<>(
					baseName+"_hanging_sign", buildProperties(strength, mapColor, nbi, ignite, null),
					blockProps -> new IESignBlocks.Hanging(wood, blockProps)
			);
			BlockEntry<IESignBlocks.WallHanging> wallHanging = new BlockEntry<>(
					baseName+"_wall_hanging_sign", buildProperties(strength, mapColor, nbi, ignite, hanging::get),
					blockProps -> new IESignBlocks.WallHanging(wood, blockProps)
			);
			return new SignHolder(baseName, sign, wall, hanging, wallHanging);
		}

		private static Supplier<BlockBehaviour.Properties> buildProperties(float strength, MapColor mapColor, NoteBlockInstrument nbi, boolean ignite, Supplier<Block> dropsLike)
		{
			return () -> {
				BlockBehaviour.Properties props = Properties.of().mapColor(mapColor).instrument(nbi).strength(strength).forceSolidOn().noCollission();
				if(ignite)
					props.ignitedByLava();
				if(dropsLike!=null)
					props.dropsLike(dropsLike.get());
				return props;
			};
		}

		public void registerItems(DeferredRegister<Item> register)
		{
			register.register(
					baseName+"_sign",
					() -> new SignItem(new Item.Properties().stacksTo(16), this.sign().get(), this.wall().get())
			);
			register.register(
					baseName+"_hanging_sign",
					() -> new HangingSignItem(this.hanging().get(), this.wallHanging.get(), new Item.Properties().stacksTo(16))
			);
		}

		public boolean matchesEntries(BlockEntry<?> entry)
		{
			return entry==sign()||entry==wall()||entry==hanging()||entry==wallHanging();
		}

		public List<BlockEntry<?>> getEntries()
		{
			return Arrays.asList(sign, wall, hanging, wallHanging);
		}

		public void mapMultiSign(Consumer<BlockEntry<?>> consumer)
		{
			consumer.accept(sign());
			consumer.accept(wall());
		}

		public void mapMultiHanging(Consumer<BlockEntry<?>> consumer)
		{
			consumer.accept(hanging());
			consumer.accept(wallHanging());
		}
	}

	// TODO replace by NFs DeferredBlock?
	public static final class BlockEntry<T extends Block> implements Supplier<T>, ItemLike
	{
		public static final Collection<BlockEntry<?>> ALL_ENTRIES = new ArrayList<>();

		private final DeferredHolder<Block, T> regObject;
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
			this.properties = () -> Properties.ofFullCopy(existing);
			this.regObject = DeferredHolder.create(Registries.BLOCK, BuiltInRegistries.BLOCK.getKey(existing));
		}

		@SuppressWarnings("unchecked")
		public BlockEntry(BlockEntry<? extends T> toCopy)
		{
			this.properties = toCopy.properties;
			this.regObject = (DeferredHolder<Block, T>)toCopy.regObject;
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
	}
}
