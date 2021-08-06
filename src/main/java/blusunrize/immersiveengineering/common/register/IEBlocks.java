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
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
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
import blusunrize.immersiveengineering.common.blocks.stone.*;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceTileEntity.CrudeBlastFurnaceTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.BarrelBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO block items
public final class IEBlocks
{
	public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Lib.MODID);
	private static final Supplier<Properties> STONE_DECO_PROPS = () -> Block.Properties.of(Material.STONE)
			.sound(SoundType.STONE)
			.requiresCorrectToolForDrops()
			.harvestTool(ToolType.PICKAXE)
			.strength(2, 10);
	private static final Supplier<Properties> STONE_DECO_LEADED_PROPS = () -> Block.Properties.of(Material.STONE)
			.sound(SoundType.STONE)
			.requiresCorrectToolForDrops()
			.harvestTool(ToolType.PICKAXE)
			.strength(2, 180);
	private static final Supplier<Properties> STONE_DECO_PROPS_NOT_SOLID = () -> Block.Properties.of(Material.STONE)
			.sound(SoundType.STONE)
			.requiresCorrectToolForDrops()
			.harvestTool(ToolType.PICKAXE)
			.strength(2, 10)
			.noOcclusion();
	private static final Supplier<Properties> SHEETMETAL_PROPERTIES = () -> Block.Properties.of(Material.METAL)
			.sound(SoundType.METAL)
			.strength(3, 10);
	private static final Supplier<Properties> STANDARD_WOOD_PROPERTIES = () -> Block.Properties.of(Material.WOOD)
			.sound(SoundType.WOOD)
			.strength(2, 5);
	private static final Supplier<Properties> STANDARD_WOOD_PROPERTIES_NO_OVERLAY =
			() -> Block.Properties.of(Material.WOOD)
					.sound(SoundType.WOOD)
					.strength(2, 5)
					.isViewBlocking((state, blockReader, pos) -> false);
	private static final Supplier<Properties> STANDARD_WOOD_PROPERTIES_NOT_SOLID = () -> STANDARD_WOOD_PROPERTIES_NO_OVERLAY.get().noOcclusion();
	private static final Supplier<Properties> DEFAULT_METAL_PROPERTIES = () -> Block.Properties.of(Material.METAL)
			.sound(SoundType.METAL)
			.requiresCorrectToolForDrops()
			.harvestTool(ToolType.PICKAXE)
			.strength(3, 15);
	private static final Supplier<Properties> METAL_PROPERTIES_NO_OVERLAY =
			() -> Block.Properties.of(Material.METAL)
					.sound(SoundType.METAL)
					.strength(3, 15)
					.requiresCorrectToolForDrops()
					.harvestTool(ToolType.PICKAXE)
					.isViewBlocking((state, blockReader, pos) -> false);
	private static final Supplier<Properties> METAL_PROPERTIES_NOT_SOLID = () -> METAL_PROPERTIES_NO_OVERLAY.get().noOcclusion();

	private IEBlocks()
	{
	}

	public static Map<ResourceLocation, BlockEntry<SlabBlock>> toSlab = new HashMap<>();
	public static Map<ResourceLocation, BlockEntry<IEStairsBlock>> toStairs = new HashMap<>();

	public static final class StoneDecoration
	{
		public static final BlockEntry<IEBaseBlock> cokebrick = BlockEntry.simple("cokebrick", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> blastbrick = BlockEntry.simple("blastbrick", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> blastbrickReinforced = BlockEntry.simple(
				"blastbrick_reinforced", STONE_DECO_PROPS
		);
		//TODO burn time for item
		public static final BlockEntry<IEBaseBlock> coke = BlockEntry.simple("coke", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> hempcrete = BlockEntry.simple("hempcrete", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> concrete = BlockEntry.simple("concrete", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> concreteTile = BlockEntry.simple("concrete_tile", STONE_DECO_PROPS);
		public static final BlockEntry<IEBaseBlock> concreteLeaded = BlockEntry.simple(
				"concrete_leaded", STONE_DECO_LEADED_PROPS
		);
		public static final BlockEntry<IEBaseBlock> insulatingGlass = BlockEntry.simple(
				"insulating_glass", STONE_DECO_PROPS_NOT_SOLID
		);
		public static final BlockEntry<IEBaseBlock> concreteSprayed = BlockEntry.simple(
				"concrete_sprayed", () -> Block.Properties.of(Material.STONE)
						.strength(.2F, 1)
						.noOcclusion(), IEBaseBlock::setHammerHarvest);
		public static final BlockEntry<IEBaseBlock> alloybrick = BlockEntry.simple("alloybrick", STONE_DECO_PROPS);

		//TODO possibly merge into a single block with "arbitrary" height?
		public static final BlockEntry<PartialConcreteBlock> concreteSheet = new BlockEntry<>(
				"concrete_sheet", PartialConcreteBlock::makeProperties, props -> new PartialConcreteBlock(props, 1)
		);
		public static final BlockEntry<PartialConcreteBlock> concreteQuarter = new BlockEntry<>(
				"concrete_quarter", PartialConcreteBlock::makeProperties, props -> new PartialConcreteBlock(props, 4)
		);
		public static final BlockEntry<PartialConcreteBlock> concreteThreeQuarter = new BlockEntry<>(
				"concrete_three_quarter",
				PartialConcreteBlock::makeProperties, props -> new PartialConcreteBlock(props, 4)
		);

		public static final BlockEntry<HorizontalFacingBlock<CoresampleTileEntity>> coresample = new BlockEntry<>(
				"coresample", STONE_DECO_PROPS_NOT_SOLID, p -> new HorizontalFacingBlock<>(IETileTypes.CORE_SAMPLE, p)
		);

		private static void init()
		{
		}
	}

	public static final class Multiblocks
	{
		public static final BlockEntry<StoneMultiBlock<CokeOvenTileEntity>> cokeOven = new BlockEntry<>(
				"coke_oven", StoneMultiBlock.properties(true), p -> new StoneMultiBlock<>(p, IETileTypes.COKE_OVEN)
		);
		public static final BlockEntry<StoneMultiBlock<CrudeBlastFurnaceTileEntity>> blastFurnace = new BlockEntry<>(
				"blast_furnace", StoneMultiBlock.properties(true), p -> new StoneMultiBlock<>(p, IETileTypes.BLAST_FURNACE)
		);
		public static final BlockEntry<StoneMultiBlock<AlloySmelterTileEntity>> alloySmelter = new BlockEntry<>(
				"alloy_smelter", StoneMultiBlock.properties(true), p -> new StoneMultiBlock<>(p, IETileTypes.ALLOY_SMELTER)
		);
		public static final BlockEntry<StoneMultiBlock<BlastFurnaceAdvancedTileEntity>> blastFurnaceAdv = new BlockEntry<>(
				"advanced_blast_furnace", StoneMultiBlock.properties(false), p -> new StoneMultiBlock<>(p, IETileTypes.BLAST_FURNACE_ADVANCED)
		);

		public static final BlockEntry<MetalMultiblockBlock<MetalPressTileEntity>> metalPress = new BlockEntry<>(
				"metal_press", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.METAL_PRESS, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<CrusherTileEntity>> crusher = new BlockEntry<>(
				"crusher", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.CRUSHER, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<SawmillTileEntity>> sawmill = new BlockEntry<>(
				"sawmill", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.SAWMILL, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<SheetmetalTankTileEntity>> tank = new BlockEntry<>(
				"tank", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.SHEETMETAL_TANK, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<SiloTileEntity>> silo = new BlockEntry<>(
				"silo", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.SILO, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<AssemblerTileEntity>> assembler = new BlockEntry<>(
				"assembler", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.ASSEMBLER, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<AutoWorkbenchTileEntity>> autoWorkbench = new BlockEntry<>(
				"auto_workbench", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.AUTO_WORKBENCH, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<BottlingMachineTileEntity>> bottlingMachine = new BlockEntry<>(
				"bottling_machine", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.BOTTLING_MACHINE, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<SqueezerTileEntity>> squeezer = new BlockEntry<>(
				"squeezer", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.SQUEEZER, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<FermenterTileEntity>> fermenter = new BlockEntry<>(
				"fermenter", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.FERMENTER, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<RefineryTileEntity>> refinery = new BlockEntry<>(
				"refinery", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.REFINERY, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<DieselGeneratorTileEntity>> dieselGenerator = new BlockEntry<>(
				"diesel_generator", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.DIESEL_GENERATOR, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<ExcavatorTileEntity>> excavator = new BlockEntry<>(
				"excavator", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.EXCAVATOR, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<BucketWheelTileEntity>> bucketWheel = new BlockEntry<>(
				"bucket_wheel", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.BUCKET_WHEEL, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<ArcFurnaceTileEntity>> arcFurnace = new BlockEntry<>(
				"arc_furnace", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.ARC_FURNACE, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<LightningrodTileEntity>> lightningrod = new BlockEntry<>(
				"lightning_rod", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.LIGHTNING_ROD, p)
		);
		public static final BlockEntry<MetalMultiblockBlock<MixerTileEntity>> mixer = new BlockEntry<>(
				"mixer", METAL_PROPERTIES_NO_OVERLAY, p -> new MetalMultiblockBlock<>(IETileTypes.MIXER, p)
		);

		private static void init()
		{
		}
	}

	public static final class Metals
	{
		public static Map<EnumMetals, BlockEntry<Block>> ores = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, BlockEntry<Block>> storage = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, BlockEntry<IEBaseBlock>> sheetmetal = new EnumMap<>(EnumMetals.class);

		private static void init()
		{
			Map<EnumMetals, Integer> oreMiningLevels = ImmutableMap.<EnumMetals, Integer>builder()
					.put(EnumMetals.COPPER, 1)
					.put(EnumMetals.ALUMINUM, 1)
					.put(EnumMetals.LEAD, 2)
					.put(EnumMetals.SILVER, 2)
					.put(EnumMetals.NICKEL, 2)
					.put(EnumMetals.URANIUM, 2)
					.build();
			Map<EnumMetals, Integer> storageMiningLevels = ImmutableMap.<EnumMetals, Integer>builder()
					.put(EnumMetals.COPPER, 1)
					.put(EnumMetals.ALUMINUM, 1)
					.put(EnumMetals.LEAD, 2)
					.put(EnumMetals.SILVER, 2)
					.put(EnumMetals.NICKEL, 2)
					.put(EnumMetals.URANIUM, 2)
					.put(EnumMetals.CONSTANTAN, 2)
					.put(EnumMetals.ELECTRUM, 2)
					.put(EnumMetals.STEEL, 2)
					.build();
			for(EnumMetals m : EnumMetals.values())
			{
				String name = m.tagName();
				BlockEntry<Block> storage;
				BlockEntry<Block> ore = null;
				BlockEntry<IEBaseBlock> sheetmetal = BlockEntry.simple("sheetmetal_"+name, SHEETMETAL_PROPERTIES);
				registerSlab(sheetmetal);
				Metals.sheetmetal.put(m, sheetmetal);
				if(m.shouldAddOre())
				{
					ore = new BlockEntry<>(BlockEntry.simple("ore_"+name,
							() -> Block.Properties.of(Material.STONE)
									.strength(3, 5)
									.requiresCorrectToolForDrops()
									.harvestTool(ToolType.PICKAXE)
									.harvestLevel(oreMiningLevels.get(m))));
				}
				if(!m.isVanillaMetal())
				{
					BlockEntry<IEBaseBlock> storageIE = BlockEntry.simple(
							"storage_"+name, () -> Block.Properties.of(Material.METAL)
							.sound(m==EnumMetals.STEEL?SoundType.NETHERITE_BLOCK: SoundType.METAL)
							.strength(5, 10)
							.requiresCorrectToolForDrops()
							.harvestTool(ToolType.PICKAXE)
							.harvestLevel(storageMiningLevels.get(m))
					);
					registerSlab(storageIE);
					storage = new BlockEntry<>(storageIE);
				}
				else if(m==EnumMetals.IRON)
				{
					storage = new BlockEntry<>(Blocks.IRON_BLOCK);
					ore = new BlockEntry<>(Blocks.IRON_ORE);
				}
				else if(m==EnumMetals.GOLD)
				{
					storage = new BlockEntry<>(Blocks.GOLD_BLOCK);
					ore = new BlockEntry<>(Blocks.GOLD_ORE);
				}
				else if(m==EnumMetals.COPPER)
				{
					storage = new BlockEntry<>(Blocks.COPPER_BLOCK);
					ore = new BlockEntry<>(Blocks.COPPER_ORE);
				}
				else
					throw new RuntimeException("Unkown vanilla metal: "+m.name());
				IEBlocks.Metals.storage.put(m, storage);
				if(ore!=null)
					IEBlocks.Metals.ores.put(m, ore);
			}
		}
	}

	public static final class WoodenDecoration
	{
		public static Map<TreatedWoodStyles, BlockEntry<IEBaseBlock>> treatedWood = new EnumMap<>(TreatedWoodStyles.class);
		public static final BlockEntry<FenceBlock> treatedFence = BlockEntry.fence("treated_fence", STANDARD_WOOD_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<ScaffoldingBlock> treatedScaffolding = BlockEntry.scaffolding("treated_scaffold", STANDARD_WOOD_PROPERTIES_NOT_SOLID);
		public static final BlockEntry<PostBlock> treatedPost = BlockEntry.post("treated_post", STANDARD_WOOD_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<SawdustBlock> sawdust = new BlockEntry<>(
				"sawdust",
				() -> Block.Properties.of(Material.WOOD, MaterialColor.SAND)
						.sound(SoundType.SAND)
						.harvestTool(ToolType.SHOVEL)
						.strength(0.5F)
						.noCollission().noOcclusion(),
				SawdustBlock::new
		);

		private static void init() {
			for(TreatedWoodStyles style : TreatedWoodStyles.values())
			{
				BlockEntry<IEBaseBlock> baseBlock = BlockEntry.simple(
						"treated_wood_"+style.name().toLowerCase(Locale.US), STANDARD_WOOD_PROPERTIES, shouldHave -> shouldHave.setHasFlavour(true)
				);
				WoodenDecoration.treatedWood.put(style, baseBlock);
				registerSlab(baseBlock);
				registerStairs(baseBlock);
			}
		}
	}

	public static final class WoodenDevices
	{
		public static final BlockEntry<HorizontalFacingBlock<CraftingTableTileEntity>> craftingTable = new BlockEntry<>(
				"craftingtable", STANDARD_WOOD_PROPERTIES_NOT_SOLID, p -> new HorizontalFacingBlock<>(IETileTypes.CRAFTING_TABLE, p)
		);
		public static final BlockEntry<DeskBlock<ModWorkbenchTileEntity>> workbench = new BlockEntry<>(
				"workbench", DeskBlock.PROPERTIES, p -> new DeskBlock<>(IETileTypes.MOD_WORKBENCH, p)
		);
		public static final BlockEntry<DeskBlock<CircuitTableTileEntity>> circuitTable = new BlockEntry<>(
				"circuit_table", DeskBlock.PROPERTIES, p -> new DeskBlock<>(IETileTypes.CIRCUIT_TABLE, p)
		);
		public static final BlockEntry<GunpowderBarrelBlock> gunpowderBarrel = new BlockEntry<>(
				"gunpowder_barrel", GunpowderBarrelBlock.PROPERTIES, GunpowderBarrelBlock::new
		);
		public static final BlockEntry<GenericTileBlock<?>> woodenBarrel = BlockEntry.barrel("wooden_barrel", false);
		public static final BlockEntry<TurntableBlock> turntable = new BlockEntry<>("turntable", STANDARD_WOOD_PROPERTIES, TurntableBlock::new);
		public static final BlockEntry<GenericTileBlock<WoodenCrateTileEntity>> crate = new BlockEntry<>(
				"crate", STANDARD_WOOD_PROPERTIES, p -> new GenericTileBlock<>(IETileTypes.WOODEN_CRATE, p)
		);
		public static final BlockEntry<GenericTileBlock<WoodenCrateTileEntity>> reinforcedCrate = new BlockEntry<>(
				"reinforced_crate",
				() -> Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2, 1200000),
				p -> new GenericTileBlock<>(IETileTypes.WOODEN_CRATE, p)
		);
		public static final BlockEntry<GenericTileBlock<SorterTileEntity>> sorter = new BlockEntry<>(
				"sorter", STANDARD_WOOD_PROPERTIES, p -> new GenericTileBlock<>(IETileTypes.SORTER, p)
		);
		public static final BlockEntry<ItemBatcherBlock> itemBatcher = new BlockEntry<>(
				"item_batcher", STANDARD_WOOD_PROPERTIES, ItemBatcherBlock::new
		);
		public static final BlockEntry<GenericTileBlock<FluidSorterTileEntity>> fluidSorter = new BlockEntry<>(
				"fluid_sorter", STANDARD_WOOD_PROPERTIES, p -> new GenericTileBlock<>(IETileTypes.FLUID_SORTER, p)
		);
		public static final BlockEntry<WindmillBlock> windmill = new BlockEntry<>(
				"windmill", STANDARD_WOOD_PROPERTIES_NOT_SOLID, WindmillBlock::new
		);
		public static final BlockEntry<WatermillBlock> watermill = new BlockEntry<>(
				"watermill", STANDARD_WOOD_PROPERTIES_NOT_SOLID, WatermillBlock::new
		);
		//TODO move to deco?
		public static final BlockEntry<WallmountBlock> treatedWallmount = BlockEntry.wallmount("treated_wallmount", STANDARD_WOOD_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<HorizontalFacingBlock<LogicUnitTileEntity>> logicUnit = new BlockEntry<>(
				"logic_unit", STANDARD_WOOD_PROPERTIES_NOT_SOLID, p -> new HorizontalFacingBlock<>(IETileTypes.LOGIC_UNIT, p)
		);

		private static void init()
		{
		}
	}


	public static final class MetalDecoration
	{
		public static final BlockEntry<IEBaseBlock> lvCoil = BlockEntry.simple("coil_lv", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> mvCoil = BlockEntry.simple("coil_mv", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> hvCoil = BlockEntry.simple("coil_hv", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> engineeringRS = BlockEntry.simple("rs_engineering", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> engineeringHeavy = BlockEntry.simple("heavy_engineering", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> engineeringLight = BlockEntry.simple("light_engineering", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> generator = BlockEntry.simple("generator", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<IEBaseBlock> radiator = BlockEntry.simple("radiator", DEFAULT_METAL_PROPERTIES);
		public static final BlockEntry<FenceBlock> steelFence = BlockEntry.fence("steel_fence", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<FenceBlock> aluFence = BlockEntry.fence("alu_fence", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<WallmountBlock> steelWallmount = BlockEntry.wallmount("steel_wallmount", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<WallmountBlock> aluWallmount = BlockEntry.wallmount("alu_wallmount", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<PostBlock> steelPost = BlockEntry.post("steel_post", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<PostBlock> aluPost = BlockEntry.post("alu_post", METAL_PROPERTIES_NO_OVERLAY);
		public static final BlockEntry<LanternBlock> lantern = new BlockEntry<>("lantern", LanternBlock.PROPERTIES, LanternBlock::new);
		public static final BlockEntry<StructuralArmBlock> slopeSteel = new BlockEntry<>(
				"steel_slope", DEFAULT_METAL_PROPERTIES, StructuralArmBlock::new
		);
		public static final BlockEntry<StructuralArmBlock> slopeAlu = new BlockEntry<>(
				"alu_slope", DEFAULT_METAL_PROPERTIES, StructuralArmBlock::new
		);
		public static Map<CoverType, BlockEntry<MetalLadderBlock>> metalLadder = new EnumMap<>(CoverType.class);
		public static Map<MetalScaffoldingType, BlockEntry<ScaffoldingBlock>> steelScaffolding = new EnumMap<>(MetalScaffoldingType.class);
		public static Map<MetalScaffoldingType, BlockEntry<ScaffoldingBlock>> aluScaffolding = new EnumMap<>(MetalScaffoldingType.class);
		public static Map<DyeColor, BlockEntry<IEBaseBlock>> coloredSheetmetal = new EnumMap<>(DyeColor.class);

		private static void init() {
			for(DyeColor dye : DyeColor.values())
			{
				BlockEntry<IEBaseBlock> sheetmetal = BlockEntry.simple(
						"sheetmetal_colored_"+dye.getName(), SHEETMETAL_PROPERTIES
				);
				coloredSheetmetal.put(dye, sheetmetal);
				registerSlab(sheetmetal);
			}
			for(CoverType type : CoverType.values())
				metalLadder.put(type, new BlockEntry<>(
						"metal_ladder_"+type.name().toLowerCase(Locale.US),
						METAL_PROPERTIES_NOT_SOLID,
						p -> new MetalLadderBlock(type, p)
				));
			for(MetalScaffoldingType type : MetalScaffoldingType.values())
			{
				String name = type.name().toLowerCase(Locale.ENGLISH);
				BlockEntry<ScaffoldingBlock> steelBlock = BlockEntry.scaffolding("steel_scaffolding_"+name, METAL_PROPERTIES_NOT_SOLID);
				BlockEntry<ScaffoldingBlock> aluBlock = BlockEntry.scaffolding("alu_scaffolding_"+name, METAL_PROPERTIES_NOT_SOLID);
				steelScaffolding.put(type, steelBlock);
				aluScaffolding.put(type, aluBlock);
				registerSlab(steelBlock);
				registerSlab(aluBlock);
				registerStairs(steelBlock);
				registerStairs(aluBlock);
			}
		}
	}

	public static final class MetalDevices
	{
		public static final BlockEntry<RazorWireBlock> razorWire = new BlockEntry<>(
				"razor_wire", RazorWireBlock.PROPERTIES, RazorWireBlock::new
		);
		public static final BlockEntry<HorizontalFacingBlock<ToolboxTileEntity>> toolbox = new BlockEntry<>(
				"toolbox_block", METAL_PROPERTIES_NO_OVERLAY, p -> new HorizontalFacingBlock<>(IETileTypes.TOOLBOX, p)
		);
		public static final BlockEntry<GenericTileBlock<CapacitorTileEntity>> capacitorLV = new BlockEntry<>(
				"capacitor_lv", DEFAULT_METAL_PROPERTIES, p -> new GenericTileBlock<>(IETileTypes.CAPACITOR_LV, p)
		);
		public static final BlockEntry<GenericTileBlock<CapacitorTileEntity>> capacitorMV = new BlockEntry<>(
				"capacitor_mv", DEFAULT_METAL_PROPERTIES, p -> new GenericTileBlock<>(IETileTypes.CAPACITOR_MV, p)
		);
		public static final BlockEntry<GenericTileBlock<CapacitorTileEntity>> capacitorHV = new BlockEntry<>(
				"capacitor_hv", DEFAULT_METAL_PROPERTIES, p -> new GenericTileBlock<>(IETileTypes.CAPACITOR_HV, p)
		);
		public static final BlockEntry<GenericTileBlock<CapacitorCreativeTileEntity>> capacitorCreative = new BlockEntry<>(
				"capacitor_creative", DEFAULT_METAL_PROPERTIES, p -> new GenericTileBlock<>(IETileTypes.CAPACITOR_CREATIVE, p)
		);
		public static final BlockEntry<GenericTileBlock<?>> barrel = BlockEntry.barrel("metal_barrel", true);
		public static final BlockEntry<FluidPumpBlock> fluidPump = new BlockEntry<>(
				"fluid_pump", METAL_PROPERTIES_NOT_SOLID, FluidPumpBlock::new
		);
		public static final BlockEntry<GenericTileBlock<FluidPlacerTileEntity>> fluidPlacer = new BlockEntry<>(
				"fluid_placer", METAL_PROPERTIES_NOT_SOLID, p -> new GenericTileBlock<>(IETileTypes.FLUID_PLACER, p)
		);
		public static final BlockEntry<BlastFurnacePreheaterBlock> blastFurnacePreheater = new BlockEntry<>(
				"blastfurnace_preheater", METAL_PROPERTIES_NOT_SOLID, BlastFurnacePreheaterBlock::new
		);
		public static final BlockEntry<FurnaceHeaterBlock> furnaceHeater = new BlockEntry<>(
				"furnace_heater", DEFAULT_METAL_PROPERTIES, FurnaceHeaterBlock::new
		);
		public static final BlockEntry<HorizontalFacingBlock<DynamoTileEntity>> dynamo = new BlockEntry<>(
				"dynamo", DEFAULT_METAL_PROPERTIES, p -> new HorizontalFacingBlock<>(IETileTypes.DYNAMO, p)
		);
		public static final BlockEntry<GenericTileBlock<ThermoelectricGenTileEntity>> thermoelectricGen = new BlockEntry<>(
				"thermoelectric_generator", DEFAULT_METAL_PROPERTIES, p -> new GenericTileBlock<>(IETileTypes.THERMOELECTRIC_GEN, p)
		);
		public static final BlockEntry<ElectricLanternBlock> electricLantern = new BlockEntry<>(
				"electric_lantern", ElectricLanternBlock.PROPERTIES, ElectricLanternBlock::new
		);
		public static final BlockEntry<HorizontalFacingBlock<ChargingStationTileEntity>> chargingStation = new BlockEntry<>(
				"charging_station", METAL_PROPERTIES_NO_OVERLAY, p -> new HorizontalFacingBlock<>(IETileTypes.CHARGING_STATION, p)
		);
		public static final BlockEntry<FluidPipeBlock> fluidPipe = new BlockEntry<>("fluid_pipe", METAL_PROPERTIES_NO_OVERLAY, FluidPipeBlock::new);
		public static final BlockEntry<SampleDrillBlock> sampleDrill = new BlockEntry<>("sample_drill", METAL_PROPERTIES_NOT_SOLID, SampleDrillBlock::new);
		public static final BlockEntry<TeslaCoilBlock> teslaCoil = new BlockEntry<>("tesla_coil", METAL_PROPERTIES_NOT_SOLID, TeslaCoilBlock::new);
		public static final BlockEntry<FloodlightBlock> floodlight = new BlockEntry<>("floodlight", FloodlightBlock.PROPERTIES, FloodlightBlock::new);
		public static final BlockEntry<TurretBlock<TurretChemTileEntity>> turretChem = new BlockEntry<>(
				"turret_chem", METAL_PROPERTIES_NOT_SOLID, p -> new TurretBlock<>(IETileTypes.TURRET_CHEM, p)
		);
		public static final BlockEntry<TurretBlock<TurretGunTileEntity>> turretGun = new BlockEntry<>(
				"turret_gun", METAL_PROPERTIES_NOT_SOLID, p -> new TurretBlock<>(IETileTypes.TURRET_GUN, p)
		);
		public static final BlockEntry<ClocheBlock> cloche = new BlockEntry<>("cloche", METAL_PROPERTIES_NOT_SOLID, ClocheBlock::new);
		public static final Map<ResourceLocation, BlockEntry<ConveyorBlock>> CONVEYORS = new HashMap<>();
		public static Map<EnumMetals, BlockEntry<ChuteBlock>> chutes = new EnumMap<>(EnumMetals.class);

		private static void init()
		{
			for(EnumMetals metal : new EnumMetals[]{EnumMetals.IRON, EnumMetals.STEEL, EnumMetals.ALUMINUM, EnumMetals.COPPER})
				MetalDevices.chutes.put(metal, new BlockEntry<>("chute_"+metal.tagName(), METAL_PROPERTIES_NOT_SOLID, ChuteBlock::new));

		}

		public static void initConveyors()
		{
			Preconditions.checkState(CONVEYORS.isEmpty());
			for(ResourceLocation rl : ConveyorHandler.classRegistry.keySet())
			{
				BlockEntry<ConveyorBlock> entry = new BlockEntry<>(
						ConveyorHandler.getRegistryNameFor(rl).getPath(), ConveyorBlock.PROPERTIES, p -> new ConveyorBlock(rl, p)
				);
				CONVEYORS.put(rl, entry);
				IEItems.REGISTER.register(entry.getId().getPath(), () -> new BlockItemIE(entry.get()));
			}
		}
	}

	public static final class Connectors
	{
		public static final Map<Pair<String, Boolean>, BlockEntry<BasicConnectorBlock<?>>> ENERGY_CONNECTORS = new HashMap<>();
		public static final BlockEntry<BasicConnectorBlock<?>> connectorStructural = new BlockEntry<>(
				"connector_structural", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IETileTypes.CONNECTOR_STRUCTURAL)
		);
		public static final BlockEntry<TransformerBlock> transformer = new BlockEntry<>("transformer", ConnectorBlock.PROPERTIES, TransformerBlock::new);
		public static final BlockEntry<PostTransformerBlock> postTransformer = new BlockEntry<>(
				"post_transformer", ConnectorBlock.PROPERTIES, PostTransformerBlock::new
		);
		public static final BlockEntry<TransformerHVBlock> transformerHV = new BlockEntry<>(
				"transformer_hv", ConnectorBlock.PROPERTIES, TransformerHVBlock::new
		);
		public static final BlockEntry<BreakerSwitchBlock<?>> breakerswitch = new BlockEntry<>(
			"breaker_switch", ConnectorBlock.PROPERTIES, p -> new BreakerSwitchBlock<>(p, IETileTypes.BREAKER_SWITCH)
		);
		public static final BlockEntry<BreakerSwitchBlock<?>> redstoneBreaker = new BlockEntry<>(
				"redstone_breaker", ConnectorBlock.PROPERTIES, p -> new BreakerSwitchBlock<>(p, IETileTypes.REDSTONE_BREAKER)
		);
		public static final BlockEntry<EnergyMeterBlock> currentTransformer = new BlockEntry<>("current_transformer", ConnectorBlock.PROPERTIES, EnergyMeterBlock::new);
		public static final BlockEntry<BasicConnectorBlock<?>> connectorRedstone = new BlockEntry<>(
				"connector_redstone", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IETileTypes.CONNECTOR_REDSTONE)
		);
		public static final BlockEntry<BasicConnectorBlock<?>> connectorProbe = new BlockEntry<>(
				"connector_probe", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IETileTypes.CONNECTOR_PROBE)
		);
		public static final BlockEntry<BasicConnectorBlock<?>> connectorBundled = new BlockEntry<>(
				"connector_bundled", ConnectorBlock.PROPERTIES, p -> new BasicConnectorBlock<>(p, IETileTypes.CONNECTOR_BUNDLED)
		);
		public static final BlockEntry<FeedthroughBlock> feedthrough = new BlockEntry<>("feedthrough", ConnectorBlock.PROPERTIES, FeedthroughBlock::new);

		public static BlockEntry<BasicConnectorBlock<?>> getEnergyConnector(String cat, boolean relay)
		{
			return ENERGY_CONNECTORS.get(new ImmutablePair<>(cat, relay));
		}

		private static void init()
		{
			for(String cat : new String[]{WireType.LV_CATEGORY, WireType.MV_CATEGORY, WireType.HV_CATEGORY})
			{
				Connectors.ENERGY_CONNECTORS.put(
						new ImmutablePair<>(cat, false), BasicConnectorBlock.forPower(cat, false)
				);
				Connectors.ENERGY_CONNECTORS.put(
						new ImmutablePair<>(cat, true), BasicConnectorBlock.forPower(cat, true)
				);
			}
		}
	}

	public static final class Cloth
	{
		public static final BlockEntry<CushionBlock> cushion = new BlockEntry<>("cushion", CushionBlock.PROPERTIES, CushionBlock::new);
		public static final BlockEntry<BalloonBlock> balloon = new BlockEntry<>("balloon", BalloonBlock.PROPERTIES, BalloonBlock::new);
		public static final BlockEntry<StripCurtainBlock> curtain = new BlockEntry<>("strip_curtain", StripCurtainBlock.PROPERTIES, StripCurtainBlock::new);
		public static final BlockEntry<ShaderBannerStandingBlock> shaderBanner = new BlockEntry<>(
				"shader_banner", ShaderBannerBlock.PROPERTIES, ShaderBannerStandingBlock::new
		);
		public static final BlockEntry<ShaderBannerWallBlock> shaderBannerWall = new BlockEntry<>(
				"shader_banner_wall", ShaderBannerBlock.PROPERTIES, ShaderBannerWallBlock::new
		);

		private static void init()
		{
		}
	}

	public static final class Misc
	{
		public static final BlockEntry<HempBlock> hempPlant = new BlockEntry<>("hemp", HempBlock.PROPERTIES, HempBlock::new);
		public static final BlockEntry<PottedHempBlock> pottedHemp = new BlockEntry<>("potted_hemp", PottedHempBlock.PROPERTIES, PottedHempBlock::new);
		public static final BlockEntry<FakeLightBlock> fakeLight = new BlockEntry<>("fake_light", FakeLightBlock.PROPERTIES, FakeLightBlock::new);

		private static void init()
		{
		}
	}

	private static <T extends Block & IIEBlock> void registerStairs(BlockEntry<T> fullBlock)
	{
		toStairs.put(fullBlock.getId(), new BlockEntry<>(
				"stairs_"+fullBlock.getId().getPath(),
				fullBlock::getProperties,
				p -> new IEStairsBlock(p, fullBlock)
		));
	}

	private static <T extends Block & IIEBlock> void registerSlab(BlockEntry<T> fullBlock)
	{
		toSlab.put(fullBlock.getId(), new BlockEntry<>(
				"slab_"+fullBlock.getId().getPath(),
				fullBlock::getProperties,
				p -> new BlockIESlab<>(p, fullBlock)
		));
	}

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		StoneDecoration.init();
		Multiblocks.init();
		Metals.init();
		WoodenDecoration.init();
		WoodenDevices.init();
		MetalDecoration.init();
		MetalDevices.init();
		Connectors.init();
		Cloth.init();
		Misc.init();
		registerSlab(StoneDecoration.cokebrick);
		registerSlab(StoneDecoration.blastbrick);
		registerSlab(StoneDecoration.blastbrickReinforced);
		registerSlab(StoneDecoration.coke);
		registerSlab(StoneDecoration.hempcrete);
		registerSlab(StoneDecoration.concrete);
		registerSlab(StoneDecoration.concreteTile);
		registerSlab(StoneDecoration.concreteLeaded);
		registerSlab(StoneDecoration.insulatingGlass);
		registerSlab(StoneDecoration.alloybrick);
		registerStairs(StoneDecoration.hempcrete);
		registerStairs(StoneDecoration.concrete);
		registerStairs(StoneDecoration.concreteTile);
		registerStairs(StoneDecoration.concreteLeaded);

		for(BlockEntry<?> entry : BlockEntry.ALL_ENTRIES)
			if(entry==Cloth.balloon)
				IEItems.REGISTER.register(entry.getId().getPath(), () -> new BlockItemBalloon(entry.get()));
			else if(entry==Connectors.transformer)
				IEItems.REGISTER.register(entry.getId().getPath(), () -> new TransformerBlockItem(entry.get()));
				//TODO lantern was vanilla BlockItem?
			else if(entry!=Misc.fakeLight&&entry!=Misc.pottedHemp&&entry!=StoneDecoration.coresample&&
					entry!=MetalDevices.toolbox&&entry!=Cloth.shaderBanner&&entry!=Cloth.shaderBannerWall&&
					entry!=Misc.hempPlant&&entry!=Connectors.postTransformer&&!IEFluids.ALL_FLUID_BLOCKS.contains(entry))
				IEItems.REGISTER.register(entry.getId().getPath(), () -> new BlockItemIE(entry.get()));
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
			return simple(name, properties, $ -> {});
		}

		public static BlockEntry<GenericTileBlock<?>> barrel(String name, boolean metal)
		{
			return new BlockEntry<>(name, () -> BarrelBlock.getProperties(metal), p -> BarrelBlock.make(p, metal));
		}

		public static BlockEntry<ScaffoldingBlock> scaffolding(String name, Supplier<Properties> props) {
			return new BlockEntry<>(name, props, ScaffoldingBlock::new);
		}

		public static BlockEntry<FenceBlock> fence(String name, Supplier<Properties> props)
		{
			return new BlockEntry<>(name, props, FenceBlock::new);
		}

		public static BlockEntry<PostBlock> post(String name, Supplier<Properties> props) {
			return new BlockEntry<>(name, props, PostBlock::new);
		}

		public static BlockEntry<WallmountBlock> wallmount(String name, Supplier<Properties> props) {
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
			this.regObject = RegistryObject.of(existing.getRegistryName(), ForgeRegistries.BLOCKS);
		}

		@SuppressWarnings("unchecked")
		public BlockEntry(BlockEntry<? extends T> toCopy)
		{
			this.properties = toCopy.properties;
			this.regObject = (RegistryObject<T>) toCopy.regObject;
		}

		@Override
		public T get()
		{
			return regObject.get();
		}

		public BlockState defaultBlockState() {
			return get().defaultBlockState();
		}

		public ResourceLocation getId() {
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
