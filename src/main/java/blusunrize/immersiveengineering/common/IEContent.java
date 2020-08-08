/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.*;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.DefaultFurnaceAdapter;
import blusunrize.immersiveengineering.api.wires.NetHandlerCapability;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.client.utils.ClocheRenderFunctions;
import blusunrize.immersiveengineering.common.IEConfig.Ores.OreConfig;
import blusunrize.immersiveengineering.common.blocks.*;
import blusunrize.immersiveengineering.common.blocks.FakeLightBlock.FakeLightTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.cloth.*;
import blusunrize.immersiveengineering.common.blocks.generic.*;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.stone.*;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.crafting.IngredientFluidStack;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Molds;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.IEShaders;
import blusunrize.immersiveengineering.common.util.fluids.ConcreteFluid;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import blusunrize.immersiveengineering.common.util.fluids.PotionFluid;
import blusunrize.immersiveengineering.common.util.loot.IELootFunctions;
import blusunrize.immersiveengineering.common.wires.IEWireTypes;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.OreRetrogenFeature;
import blusunrize.immersiveengineering.common.world.Villages;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.Field;
import java.util.*;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.common.util.fluids.IEFluid.createBuilder;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class IEContent
{
	public static List<Block> registeredIEBlocks = new ArrayList<>();
	public static List<Item> registeredIEItems = new ArrayList<>();
	public static List<Class<? extends TileEntity>> registeredIETiles = new ArrayList<>();
	public static List<Fluid> registeredIEFluids = new ArrayList<>();

	public static IEFluid fluidCreosote;
	public static IEFluid fluidPlantoil;
	public static IEFluid fluidEthanol;
	public static IEFluid fluidBiodiesel;
	public static IEFluid fluidConcrete;
	public static IEFluid fluidHerbicide;
	public static Fluid fluidPotion;

	public static final Feature<OreFeatureConfig> ORE_RETROGEN = new OreRetrogenFeature(OreFeatureConfig::deserialize);

	public static void modConstruction()
	{
		/*BULLETS*/
		BulletItem.initBullets();
		/*WIRES*/
		IEWireTypes.modConstruction();
		/*CONVEYORS*/
		ConveyorHandler.registerMagnetSupression((entity, iConveyorTile) -> {
			CompoundNBT data = entity.getPersistentData();
			if(!data.getBoolean(Lib.MAGNET_PREVENT_NBT))
				data.putBoolean(Lib.MAGNET_PREVENT_NBT, true);
		}, (entity, iConveyorTile) -> {
			entity.getPersistentData().remove(Lib.MAGNET_PREVENT_NBT);
		});
		ConveyorHandler.registerConveyorHandler(BasicConveyor.NAME, BasicConveyor.class, BasicConveyor::new);
		ConveyorHandler.registerConveyorHandler(RedstoneConveyor.NAME, RedstoneConveyor.class, RedstoneConveyor::new);
		ConveyorHandler.registerConveyorHandler(DropConveyor.NAME, DropConveyor.class, DropConveyor::new);
		ConveyorHandler.registerConveyorHandler(VerticalConveyor.NAME, VerticalConveyor.class, VerticalConveyor::new);
		ConveyorHandler.registerConveyorHandler(SplitConveyor.NAME, SplitConveyor.class, SplitConveyor::new);
		ConveyorHandler.registerConveyorHandler(ExtractConveyor.NAME, ExtractConveyor.class, ExtractConveyor::new);
		ConveyorHandler.registerConveyorHandler(CoveredConveyor.NAME, CoveredConveyor.class, CoveredConveyor::new);
		ConveyorHandler.registerConveyorHandler(DropCoveredConveyor.NAME, DropCoveredConveyor.class, DropCoveredConveyor::new);
		ConveyorHandler.registerConveyorHandler(VerticalCoveredConveyor.NAME, VerticalCoveredConveyor.class, VerticalCoveredConveyor::new);
		ConveyorHandler.registerConveyorHandler(ExtractCoveredConveyor.NAME, ExtractCoveredConveyor.class, ExtractCoveredConveyor::new);
		ConveyorHandler.registerConveyorHandler(SplitCoveredConveyor.NAME, SplitCoveredConveyor.class, SplitCoveredConveyor::new);
		ConveyorHandler.registerSubstitute(new ResourceLocation(MODID, "conveyor"), new ResourceLocation(MODID, "uncontrolled"));
		/*SHADERS*/
		ShaderRegistry.rarityWeightMap.put(Rarity.COMMON, 9);
		ShaderRegistry.rarityWeightMap.put(Rarity.UNCOMMON, 7);
		ShaderRegistry.rarityWeightMap.put(Rarity.RARE, 5);
		ShaderRegistry.rarityWeightMap.put(Rarity.EPIC, 3);
		ShaderRegistry.rarityWeightMap.put(Lib.RARITY_MASTERWORK, 1);

		fluidCreosote = new IEFluid("creosote", new ResourceLocation("immersiveengineering:block/fluid/creosote_still"),
				new ResourceLocation("immersiveengineering:block/fluid/creosote_flow"), createBuilder(1100, 3000));
		fluidPlantoil = new IEFluid("plantoil", new ResourceLocation("immersiveengineering:block/fluid/plantoil_still"),
				new ResourceLocation("immersiveengineering:block/fluid/plantoil_flow"), createBuilder(925, 2000));
		fluidEthanol = new IEFluid("ethanol", new ResourceLocation("immersiveengineering:block/fluid/ethanol_still"),
				new ResourceLocation("immersiveengineering:block/fluid/ethanol_flow"), createBuilder(789, 1000));
		fluidBiodiesel = new IEFluid("biodiesel", new ResourceLocation("immersiveengineering:block/fluid/biodiesel_still"),
				new ResourceLocation("immersiveengineering:block/fluid/biodiesel_flow"), createBuilder(789, 1000));
		fluidConcrete = new ConcreteFluid();
		fluidPotion = new PotionFluid();
		fluidHerbicide = new IEFluid("herbicide", new ResourceLocation("immersiveengineering:block/fluid/herbicide_still"),
				new ResourceLocation("immersiveengineering:block/fluid/herbicide_flow"), createBuilder(789, 1000));

		Block.Properties sheetmetalProperties = Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3, 10);
		ImmersiveEngineering.proxy.registerContainersAndScreens();

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
			Block storage;
			Block ore = null;
			Item nugget;
			Item ingot;
			Item plate = new IEBaseItem("plate_"+name);
			Item dust = new IEBaseItem("dust_"+name);
			IEBaseBlock sheetmetal = new IEBaseBlock("sheetmetal_"+name, sheetmetalProperties, BlockItemIE::new);
			addSlabFor(sheetmetal);
			if(m.shouldAddOre())
			{
				ore = new IEBaseBlock("ore_"+name,
						Block.Properties.create(Material.ROCK)
								.hardnessAndResistance(3, 5)
								.harvestTool(ToolType.PICKAXE)
								.harvestLevel(oreMiningLevels.get(m)), BlockItemIE::new);
			}
			if(!m.isVanillaMetal())
			{
				storage = new IEBaseBlock("storage_"+name, Block.Properties.create(Material.IRON)
						.sound(SoundType.METAL)
						.hardnessAndResistance(5, 10)
						.harvestTool(ToolType.PICKAXE)
						.harvestLevel(storageMiningLevels.get(m)), BlockItemIE::new);
				nugget = new IEBaseItem("nugget_"+name);
				ingot = new IEBaseItem("ingot_"+name);
				addSlabFor((IEBaseBlock)storage);
			}
			else if(m==EnumMetals.IRON)
			{
				storage = Blocks.IRON_BLOCK;
				ore = Blocks.IRON_ORE;
				nugget = Items.IRON_NUGGET;
				ingot = Items.IRON_INGOT;
			}
			else if(m==EnumMetals.GOLD)
			{
				storage = Blocks.GOLD_BLOCK;
				ore = Blocks.GOLD_ORE;
				nugget = Items.GOLD_NUGGET;
				ingot = Items.GOLD_INGOT;
			}
			else
				throw new RuntimeException("Unkown vanilla metal: "+m.name());
			IEBlocks.Metals.storage.put(m, storage);
			if(ore!=null)
				IEBlocks.Metals.ores.put(m, ore);
			IEBlocks.Metals.sheetmetal.put(m, sheetmetal);
			IEItems.Metals.plates.put(m, plate);
			IEItems.Metals.nuggets.put(m, nugget);
			IEItems.Metals.ingots.put(m, ingot);
			IEItems.Metals.dusts.put(m, dust);
		}
		Block.Properties stoneDecoProps = Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 10);
		Block.Properties stoneDecoLeadedProps = Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 180);

		StoneDecoration.cokebrick = new IEBaseBlock("cokebrick", stoneDecoProps, BlockItemIE::new);
		StoneDecoration.blastbrick = new IEBaseBlock("blastbrick", stoneDecoProps, BlockItemIE::new);
		StoneDecoration.blastbrickReinforced = new IEBaseBlock("blastbrick_reinforced", stoneDecoProps, BlockItemIE::new);
		StoneDecoration.coke = new IEBaseBlock("coke", stoneDecoProps,
				(b, prop) -> new BlockItemIE(b, prop).setBurnTime(3200*10));
		StoneDecoration.hempcrete = new IEBaseBlock("hempcrete", stoneDecoProps, BlockItemIE::new);
		StoneDecoration.concrete = new IEBaseBlock("concrete", stoneDecoProps, BlockItemIE::new);
		StoneDecoration.concreteTile = new IEBaseBlock("concrete_tile", stoneDecoProps, BlockItemIE::new);
		StoneDecoration.concreteLeaded = new IEBaseBlock("concrete_leaded", stoneDecoLeadedProps, BlockItemIE::new);
		StoneDecoration.alloybrick = new IEBaseBlock("alloybrick", stoneDecoProps, BlockItemIE::new);
		StoneDecoration.concreteThreeQuarter = new PartialConcreteBlock("concrete_three_quarter", 12);
		StoneDecoration.concreteQuarter = new PartialConcreteBlock("concrete_quarter", 4);
		StoneDecoration.concreteSheet = new PartialConcreteBlock("concrete_sheet", 1);

		IEBlocks.StoneDecoration.insulatingGlass = new IEBaseBlock("insulating_glass", stoneDecoProps, BlockItemIE::new)
				.setBlockLayer(BlockRenderLayer.TRANSLUCENT)
				.setNotNormalBlock();
		IEBlocks.StoneDecoration.concreteSprayed = new IEBaseBlock("concrete_sprayed", Block.Properties.create(Material.ROCK).hardnessAndResistance(.2F, 1), BlockItemIE::new)
				.setNotNormalBlock()
				.setHammerHarvest()
				.setBlockLayer(BlockRenderLayer.CUTOUT);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.cokebrick);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.blastbrick);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.blastbrickReinforced);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.coke);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.hempcrete);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.concrete);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.concreteTile);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.concreteLeaded);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.insulatingGlass);
		addSlabFor((IEBaseBlock)IEBlocks.StoneDecoration.alloybrick);

		StoneDecoration.hempcreteStairs = new IEStairsBlock("stairs_hempcrete", stoneDecoProps, (IEBaseBlock)StoneDecoration.hempcrete);
		StoneDecoration.concreteStairs[0] = new IEStairsBlock("stairs_concrete", stoneDecoProps, (IEBaseBlock)StoneDecoration.concrete);
		StoneDecoration.concreteStairs[1] = new IEStairsBlock("stairs_concrete_tile", stoneDecoProps, (IEBaseBlock)StoneDecoration.concreteTile);
		StoneDecoration.concreteStairs[2] = new IEStairsBlock("stairs_concrete_leaded", stoneDecoLeadedProps, (IEBaseBlock)StoneDecoration.concreteLeaded);
		StoneDecoration.coresample = new GenericTileBlock("coresample", () -> CoresampleTileEntity.TYPE,
				stoneDecoProps, (b, p) -> null, IEProperties.FACING_HORIZONTAL)
				.setNotNormalBlock();

		Block.Properties standardWoodProperties = Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2, 5);
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			IEBaseBlock baseBlock = new IEBaseBlock("treated_wood_"+style.name().toLowerCase(), standardWoodProperties, BlockItemIE::new)
					.setHasFlavour(true);
			WoodenDecoration.treatedWood.put(style, baseBlock);
			addSlabFor(baseBlock);
			WoodenDecoration.treatedStairs.put(style,
					new IEStairsBlock("stairs_treated_wood_"+style.name().toLowerCase(), standardWoodProperties, baseBlock));
		}
		WoodenDecoration.treatedFence = new IEFenceBlock("treated_fence", standardWoodProperties);
		WoodenDecoration.treatedScaffolding = new ScaffoldingBlock("treated_scaffold", standardWoodProperties);

		WoodenDevices.craftingTable = new GenericTileBlock("craftingtable", () -> CraftingTableTileEntity.TYPE,
				standardWoodProperties, IEProperties.FACING_HORIZONTAL)
				.setNotNormalBlock();
		WoodenDevices.workbench = new ModWorkbenchBlock("workbench");
		WoodenDevices.gunpowderBarrel = new GunpowderBarrelBlock("gunpowder_barrel");
		WoodenDevices.woodenBarrel = new BarrelBlock("wooden_barrel", false);
		WoodenDevices.turntable = new TurntableBlock("turntable");
		WoodenDevices.crate = new CrateBlock("crate", false);
		WoodenDevices.reinforcedCrate = new CrateBlock("reinforced_crate", true);

		WoodenDevices.sorter = new SorterBlock("sorter", false);
		WoodenDevices.itemBatcher = new GenericTileBlock("item_batcher", () -> ItemBatcherTileEntity.TYPE,
				standardWoodProperties, IEProperties.FACING_ALL);
		WoodenDevices.fluidSorter = new SorterBlock("fluid_sorter", true);
		WoodenDevices.windmill = new WindmillBlock("windmill");
		WoodenDevices.watermill = new WatermillBlock("watermill");
		WoodenDecoration.treatedPost = new PostBlock("treated_post", standardWoodProperties);
		WoodenDevices.treatedWallmount = new WallmountBlock("treated_wallmount", standardWoodProperties);
		IEBlocks.Misc.hempPlant = new HempBlock("hemp");

		Cloth.cushion = new CushionBlock();
		Cloth.balloon = new BalloonBlock();
		Cloth.curtain = new StripCurtainBlock();
		Cloth.shaderBanner = new ShaderBannerStandingBlock();
		Cloth.shaderBannerWall = new ShaderBannerWallBlock();

		Misc.fakeLight = new FakeLightBlock();


		Block.Properties defaultMetalProperties = Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3, 15);
		MetalDecoration.lvCoil = new IEBaseBlock("coil_lv", defaultMetalProperties, BlockItemIE::new);
		MetalDecoration.mvCoil = new IEBaseBlock("coil_mv", defaultMetalProperties, BlockItemIE::new);
		MetalDecoration.hvCoil = new IEBaseBlock("coil_hv", defaultMetalProperties, BlockItemIE::new);
		MetalDecoration.engineeringRS = new IEBaseBlock("rs_engineering", defaultMetalProperties, BlockItemIE::new);
		MetalDecoration.engineeringHeavy = new IEBaseBlock("heavy_engineering", defaultMetalProperties, BlockItemIE::new);
		MetalDecoration.engineeringLight = new IEBaseBlock("light_engineering", defaultMetalProperties, BlockItemIE::new);
		MetalDecoration.generator = new IEBaseBlock("generator", defaultMetalProperties, BlockItemIE::new);
		MetalDecoration.radiator = new IEBaseBlock("radiator", defaultMetalProperties, BlockItemIE::new);
		MetalDecoration.steelFence = new IEFenceBlock("steel_fence", defaultMetalProperties);
		MetalDecoration.aluFence = new IEFenceBlock("alu_fence", defaultMetalProperties);
		MetalDecoration.steelWallmount = new WallmountBlock("steel_wallmount", defaultMetalProperties);
		MetalDecoration.aluWallmount = new WallmountBlock("alu_wallmount", defaultMetalProperties);
		MetalDecoration.steelPost = new PostBlock("steel_post", defaultMetalProperties);
		MetalDecoration.aluPost = new PostBlock("alu_post", defaultMetalProperties);
		MetalDecoration.lantern = new LanternBlock("lantern");
		MetalDecoration.slopeSteel = new StructuralArmBlock("steel_slope");
		MetalDecoration.slopeAlu = new StructuralArmBlock("alu_slope");
		for(CoverType t : CoverType.values())
			MetalDecoration.metalLadder.put(t, new MetalLadderBlock(t));
		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			String name = type.name().toLowerCase(Locale.ENGLISH);
			IEBaseBlock steelBlock = new ScaffoldingBlock("steel_scaffolding_"+name, defaultMetalProperties);
			IEBaseBlock aluBlock = new ScaffoldingBlock("alu_scaffolding_"+name, defaultMetalProperties);
			MetalDecoration.steelScaffolding.put(type, steelBlock);
			MetalDecoration.aluScaffolding.put(type, aluBlock);
			MetalDecoration.steelScaffoldingStair.put(type, new IEStairsBlock("stairs_steel_scaffolding_"+name,
					defaultMetalProperties, steelBlock).setRenderLayer(BlockRenderLayer.CUTOUT));
			MetalDecoration.aluScaffoldingStair.put(type, new IEStairsBlock("stairs_alu_scaffolding_"+name,
					defaultMetalProperties, aluBlock).setRenderLayer(BlockRenderLayer.CUTOUT));
			addSlabFor(steelBlock);
			addSlabFor(aluBlock);
		}
		for(String cat : new String[]{WireType.LV_CATEGORY, WireType.MV_CATEGORY, WireType.HV_CATEGORY})
		{
			Block connector = new PowerConnectorBlock(cat, false);
			Block relay;
			if(!WireType.HV_CATEGORY.equals(cat))
				relay = new PowerConnectorBlock(cat, true);
			else
				relay = new PowerConnectorBlock(cat, true, BlockRenderLayer.TRANSLUCENT, BlockRenderLayer.SOLID);
			Connectors.ENERGY_CONNECTORS.put(new ImmutablePair<>(cat, false), connector);
			Connectors.ENERGY_CONNECTORS.put(new ImmutablePair<>(cat, true), relay);
		}

		Connectors.connectorStructural = new MiscConnectorBlock("connector_structural", () -> ConnectorStructuralTileEntity.TYPE);
		Connectors.postTransformer = new PostTransformerBlock();
		Connectors.transformer = new TransformerBlock();
		Connectors.transformerHV = new TransformerHVBlock();
		Connectors.breakerswitch = new MiscConnectorBlock("breaker_switch", () -> BreakerSwitchTileEntity.TYPE,
				IEProperties.ACTIVE, IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
		Connectors.redstoneBreaker = new MiscConnectorBlock("redstone_breaker", () -> RedstoneBreakerTileEntity.TYPE,
				IEProperties.ACTIVE, IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
		Connectors.currentTransformer = new EnergyMeterBlock();
		Connectors.connectorRedstone = new MiscConnectorBlock("connector_redstone", () -> ConnectorRedstoneTileEntity.TYPE);
		Connectors.connectorProbe = new MiscConnectorBlock("connector_probe", () -> ConnectorProbeTileEntity.TYPE,
				BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT, BlockRenderLayer.SOLID);
		Connectors.connectorBundled = new MiscConnectorBlock("connector_bundled", () -> ConnectorBundledTileEntity.TYPE,
				BlockRenderLayer.CUTOUT, BlockRenderLayer.SOLID);

		Connectors.feedthrough = new FeedthroughBlock();

		MetalDevices.fluidPlacer = new GenericTileBlock("fluid_placer", () -> FluidPlacerTileEntity.TYPE,
				defaultMetalProperties)
				.setBlockLayer(BlockRenderLayer.CUTOUT)
				.setNotNormalBlock();
		MetalDevices.razorWire = new MiscConnectorBlock("razor_wire", () -> RazorWireTileEntity.TYPE,
				IEProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED);
		MetalDevices.toolbox = new GenericTileBlock("toolbox_block", () -> ToolboxTileEntity.TYPE, defaultMetalProperties,
				(b, p) -> null, IEProperties.FACING_HORIZONTAL)
				.setNotNormalBlock();
		MetalDevices.capacitorLV = new GenericTileBlock("capacitor_lv", () -> CapacitorLVTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.capacitorMV = new GenericTileBlock("capacitor_mv", () -> CapacitorMVTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.capacitorHV = new GenericTileBlock("capacitor_hv", () -> CapacitorHVTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.capacitorCreative = new GenericTileBlock("capacitor_creative", () -> CapacitorCreativeTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.barrel = new BarrelBlock("metal_barrel", true);
		MetalDevices.fluidPump = new FluidPumpBlock();
		MetalDevices.blastFurnacePreheater = new BlastFurnacePreheaterBlock();
		MetalDevices.furnaceHeater = new GenericTileBlock("furnace_heater", () -> FurnaceHeaterTileEntity.TYPE,
				defaultMetalProperties, IEProperties.ACTIVE, IEProperties.FACING_ALL)
				.setBlockLayer(BlockRenderLayer.CUTOUT);
		MetalDevices.dynamo = new GenericTileBlock("dynamo", () -> DynamoTileEntity.TYPE, defaultMetalProperties, IEProperties.FACING_HORIZONTAL);
		MetalDevices.thermoelectricGen = new GenericTileBlock("thermoelectric_generator", () -> ThermoelectricGenTileEntity.TYPE,
				defaultMetalProperties);
		MetalDevices.electricLantern = new ElectricLanternBlock("electric_lantern", () -> ElectricLanternTileEntity.TYPE,
				IEProperties.FACING_TOP_DOWN, IEProperties.ACTIVE, BlockStateProperties.WATERLOGGED);
		MetalDevices.chargingStation = new GenericTileBlock("charging_station", () -> ChargingStationTileEntity.TYPE,
				defaultMetalProperties, IEProperties.FACING_HORIZONTAL)
				.setNotNormalBlock()
				.setBlockLayer(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		MetalDevices.fluidPipe = new GenericTileBlock("fluid_pipe", () -> FluidPipeTileEntity.TYPE, defaultMetalProperties)
				.setNotNormalBlock()
				.setBlockLayer(BlockRenderLayer.CUTOUT);
		MetalDevices.sampleDrill = new SampleDrillBlock();
		MetalDevices.teslaCoil = new TeslaCoilBlock();
		MetalDevices.floodlight = new FloodlightBlock("floodlight", () -> FloodlightTileEntity.TYPE);
		MetalDevices.turretChem = new TurretBlock("turret_chem", () -> TurretChemTileEntity.TYPE);
		MetalDevices.turretGun = new TurretBlock("turret_gun", () -> TurretGunTileEntity.TYPE);
		MetalDevices.cloche = new ClocheBlock();
		for(EnumMetals metal : new EnumMetals[]{EnumMetals.IRON, EnumMetals.STEEL, EnumMetals.ALUMINUM, EnumMetals.COPPER})
			MetalDevices.chutes.put(metal, new GenericTileBlock("chute_"+metal.tagName(), () -> ChuteTileEntity.TYPE,
					defaultMetalProperties, IEProperties.FACING_HORIZONTAL)
					.setNotNormalBlock());

		Multiblocks.cokeOven = new StoneMultiBlock("coke_oven", () -> CokeOvenTileEntity.TYPE);
		Multiblocks.blastFurnace = new StoneMultiBlock("blast_furnace", () -> BlastFurnaceTileEntity.TYPE);
		Multiblocks.alloySmelter = new StoneMultiBlock("alloy_smelter", () -> AlloySmelterTileEntity.TYPE);
		Multiblocks.blastFurnaceAdv = new StoneMultiBlock("advanced_blast_furnace", () -> BlastFurnaceAdvancedTileEntity.TYPE);
		Multiblocks.crusher = new MetalMultiblockBlock("crusher", () -> CrusherTileEntity.TYPE);
		Multiblocks.silo = new MetalMultiblockBlock("silo", () -> SiloTileEntity.TYPE);
		Multiblocks.tank = new MetalMultiblockBlock("tank", () -> SheetmetalTankTileEntity.TYPE)
				.setBlockLayer(BlockRenderLayer.CUTOUT_MIPPED);
		Multiblocks.arcFurnace = new MetalMultiblockBlock("arc_furnace", () -> ArcFurnaceTileEntity.TYPE);
		Multiblocks.assembler = new MetalMultiblockBlock("assembler", () -> AssemblerTileEntity.TYPE);
		Multiblocks.autoWorkbench = new MetalMultiblockBlock("auto_workbench", () -> AutoWorkbenchTileEntity.TYPE);
		Multiblocks.bucketWheel = new MetalMultiblockBlock("bucket_wheel", () -> BucketWheelTileEntity.TYPE);
		Multiblocks.excavator = new MetalMultiblockBlock("excavator", () -> ExcavatorTileEntity.TYPE);
		Multiblocks.metalPress = new MetalMultiblockBlock("metal_press", () -> MetalPressTileEntity.TYPE);
		Multiblocks.bottlingMachine = new MetalMultiblockBlock("bottling_machine", () -> BottlingMachineTileEntity.TYPE);
		//TODO multilayer models? .setBlockLayer(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		Multiblocks.fermenter = new MetalMultiblockBlock("fermenter", () -> FermenterTileEntity.TYPE);
		Multiblocks.squeezer = new MetalMultiblockBlock("squeezer", () -> SqueezerTileEntity.TYPE);
		Multiblocks.mixer = new MetalMultiblockBlock("mixer", () -> MixerTileEntity.TYPE);
		Multiblocks.refinery = new MetalMultiblockBlock("refinery", () -> RefineryTileEntity.TYPE);
		Multiblocks.dieselGenerator = new MetalMultiblockBlock("diesel_generator", () -> DieselGeneratorTileEntity.TYPE);
		Multiblocks.lightningrod = new MetalMultiblockBlock("lightning_rod", () -> LightningrodTileEntity.TYPE);

		Tools.hammer = new HammerItem();
		Tools.wirecutter = new WirecutterItem();
		Tools.screwdriver = new ScrewdriverItem();
		Tools.voltmeter = new VoltmeterItem();
		Tools.manual = new ManualItem();
		IEItems.Tools.steelPick = IETools.createPickaxe(Lib.MATERIAL_Steel, "pickaxe_steel");
		IEItems.Tools.steelShovel = IETools.createShovel(Lib.MATERIAL_Steel, "shovel_steel");
		IEItems.Tools.steelAxe = IETools.createAxe(Lib.MATERIAL_Steel, "axe_steel");
		IEItems.Tools.steelHoe = IETools.createHoe(Lib.MATERIAL_Steel, "hoe_steel");
		IEItems.Tools.steelSword = IETools.createSword(Lib.MATERIAL_Steel, "sword_steel");
		for(EquipmentSlotType slot : EquipmentSlotType.values())
			if(slot.getSlotType()==Group.ARMOR)
				IEItems.Tools.steelArmor.put(slot, new SteelArmorItem(slot));
		Tools.toolbox = new ToolboxItem();
		IEItems.Misc.hempSeeds = new IESeedItem(Misc.hempPlant);
		IEItems.Ingredients.stickTreated = new IEBaseItem("stick_treated");
		IEItems.Ingredients.stickIron = new IEBaseItem("stick_iron");
		IEItems.Ingredients.stickSteel = new IEBaseItem("stick_steel");
		IEItems.Ingredients.stickAluminum = new IEBaseItem("stick_aluminum");
		IEItems.Ingredients.hempFiber = new IEBaseItem("hemp_fiber");
		IEItems.Ingredients.hempFabric = new IEBaseItem("hemp_fabric");
		IEItems.Ingredients.coalCoke = new IEBaseItem("coal_coke")
				.setBurnTime(3200);
		IEItems.Ingredients.slag = new IEBaseItem("slag");
		IEItems.Ingredients.componentIron = new IEBaseItem("component_iron");
		IEItems.Ingredients.componentSteel = new IEBaseItem("component_steel");
		IEItems.Ingredients.waterwheelSegment = new IEBaseItem("waterwheel_segment");
		IEItems.Ingredients.windmillBlade = new IEBaseItem("windmill_blade");
		IEItems.Ingredients.windmillSail = new IEBaseItem("windmill_sail");
		IEItems.Ingredients.woodenGrip = new IEBaseItem("wooden_grip");
		IEItems.Ingredients.gunpartBarrel = new RevolverpartItem("gunpart_barrel");
		IEItems.Ingredients.gunpartDrum = new RevolverpartItem("gunpart_drum");
		IEItems.Ingredients.gunpartHammer = new RevolverpartItem("gunpart_hammer");
		IEItems.Ingredients.dustCoke = new IEBaseItem("dust_coke");
		IEItems.Ingredients.dustHopGraphite = new IEBaseItem("dust_hop_graphite");
		IEItems.Ingredients.ingotHopGraphite = new IEBaseItem("ingot_hop_graphite");
		IEItems.Ingredients.wireCopper = new IEBaseItem("wire_copper");
		IEItems.Ingredients.wireElectrum = new IEBaseItem("wire_electrum");
		IEItems.Ingredients.wireAluminum = new IEBaseItem("wire_aluminum");
		IEItems.Ingredients.wireSteel = new IEBaseItem("wire_steel");
		IEItems.Ingredients.dustSaltpeter = new IEBaseItem("dust_saltpeter");
		IEItems.Ingredients.dustSulfur = new IEBaseItem("dust_sulfur");
		IEItems.Ingredients.electronTube = new IEBaseItem("electron_tube");
		IEItems.Ingredients.circuitBoard = new IEBaseItem("circuit_board");
		IEItems.Ingredients.emptyCasing = new IEBaseItem("empty_casing");
		IEItems.Ingredients.emptyShell = new IEBaseItem("empty_shell");
		for(WireType t : WireType.getIEWireTypes())
			IEItems.Misc.wireCoils.put(t, new WireCoilItem(t));
		Item.Properties moldProperties = new Item.Properties().maxStackSize(1);
		Molds.moldPlate = new IEBaseItem("mold_plate", moldProperties);
		Molds.moldGear = new IEBaseItem("mold_gear", moldProperties);
		Molds.moldRod = new IEBaseItem("mold_rod", moldProperties);
		Molds.moldBulletCasing = new IEBaseItem("mold_bullet_casing", moldProperties);
		Molds.moldWire = new IEBaseItem("mold_wire", moldProperties);
		Molds.moldPacking4 = new IEBaseItem("mold_packing_4", moldProperties);
		Molds.moldPacking9 = new IEBaseItem("mold_packing_9", moldProperties);
		Molds.moldUnpacking = new IEBaseItem("mold_unpacking", moldProperties);
		IEItems.Misc.graphiteElectrode = new GraphiteElectrodeItem();
		IEItems.Misc.coresample = new CoresampleItem();
		Tools.drill = new DrillItem();
		Tools.drillheadIron = new DrillheadItem(DrillheadItem.IRON);
		Tools.drillheadSteel = new DrillheadItem(DrillheadItem.STEEL);
		Tools.buzzsaw = new BuzzsawItem();
		Tools.sawblade = new SawbladeItem("sawblade", 10000, 8f, 9f);
		Tools.rockcutter = new RockcutterItem("rockcutter", 5000, 5f, 9f);
		Tools.surveyTools = new SurveyToolsItem();
		Weapons.revolver = new RevolverItem();
		Weapons.speedloader = new SpeedloaderItem();
		Weapons.chemthrower = new ChemthrowerItem();
		Weapons.railgun = new RailgunItem();
		for(ResourceLocation bulletType : BulletHandler.getAllKeys())
		{
			IBullet bullet = BulletHandler.getBullet(bulletType);
			if(bullet.isProperCartridge())
				Weapons.bullets.put(bullet, new BulletItem(bullet));
		}
		IEItems.Misc.powerpack = new PowerpackItem();
		for(ToolUpgrade upgrade : ToolUpgrade.values())
			IEItems.Misc.toolUpgrades.put(upgrade, new ToolUpgradeItem(upgrade));
		IEItems.Misc.jerrycan = new JerrycanItem();
		IEItems.Misc.shader = new ShaderItem();
		IEItems.Misc.blueprint = new EngineersBlueprintItem();
		IEItems.Misc.earmuffs = new EarmuffsItem();
		for(EquipmentSlotType slot : EquipmentSlotType.values())
			if(slot.getSlotType()==Group.ARMOR)
				IEItems.Misc.faradaySuit.put(slot, new FaradaySuitItem(slot));
		IEItems.Misc.fluorescentTube = new FluorescentTubeItem();
		IEItems.Misc.shield = new IEShieldItem();
		IEItems.Misc.skyhook = new SkyhookItem();
		IEItems.Misc.maintenanceKit = new MaintenanceKitItem();
		IEItems.Misc.cartWoodenCrate = new IEMinecartItem("woodencrate")
		{
			@Override
			public IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack)
			{
				return new CrateMinecartEntity(CrateMinecartEntity.TYPE, world, x, y, z);
			}
		};
		IEItems.Misc.cartReinforcedCrate = new IEMinecartItem("reinforcedcrate")
		{
			@Override
			public IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack)
			{
				return new ReinforcedCrateMinecartEntity(ReinforcedCrateMinecartEntity.TYPE, world, x, y, z);
			}
		};
		IEItems.Misc.cartWoodenBarrel = new IEMinecartItem("woodenbarrel")
		{
			@Override
			public IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack)
			{
				return new BarrelMinecartEntity(BarrelMinecartEntity.TYPE, world, x, y, z);
			}
		};
		IEItems.Misc.cartMetalBarrel = new IEMinecartItem("metalbarrel")
		{
			@Override
			public IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack)
			{
				return new MetalBarrelMinecartEntity(MetalBarrelMinecartEntity.TYPE, world, x, y, z);
			}
		};


		IEItems.Misc.iconBirthday = new FakeIconItem("birthday");
		IEItems.Misc.iconLucky = new FakeIconItem("lucky");
		IEItems.Misc.iconDrillbreak = new FakeIconItem("drillbreak");

		ConveyorHandler.createConveyorBlocks();
		BulletHandler.emptyCasing = new ItemStack(Ingredients.emptyCasing);
		BulletHandler.emptyShell = new ItemStack(Ingredients.emptyShell);
		IEWireTypes.setup();
		DataSerializers.registerSerializer(IEFluid.OPTIONAL_FLUID_STACK);

		ClocheRenderFunctions.init();

		IELootFunctions.preInit();
		IEShaders.preInit();
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		checkNonNullNames(registeredIEBlocks);
		for(Block block : registeredIEBlocks)
			event.getRegistry().register(block);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		for(Rarity r : ShaderRegistry.rarityWeightMap.keySet())
			IEItems.Misc.shaderBag.put(r, new ShaderBagItem(r));
		checkNonNullNames(registeredIEItems);
		for(Item item : registeredIEItems)
			event.getRegistry().register(item);
	}

	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event)
	{
		event.getRegistry().register(ORE_RETROGEN.setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, "ore_retro")));
	}

	private static <T extends IForgeRegistryEntry<T>> void checkNonNullNames(Collection<T> coll)
	{
		int numNull = 0;
		for(T b : coll)
			if(b.getRegistryName()==null)
			{
				IELogger.logger.info("Null name for {} (class {})", b, b.getClass());
				++numNull;
			}
		if(numNull > 0)
			System.exit(1);
	}

	@SubscribeEvent
	public static void registerFluids(RegistryEvent.Register<Fluid> event)
	{
		checkNonNullNames(registeredIEFluids);
		for(Fluid fluid : registeredIEFluids)
			event.getRegistry().register(fluid);
	}

	@SubscribeEvent
	public static void missingItems(RegistryEvent.MissingMappings<Item> event)
	{
		Set<String> knownMissing = ImmutableSet.of(
				"fluidethanol",
				"fluidconcrete",
				"fluidbiodiesel",
				"fluidplantoil",
				"fluidcreosote"
		);
		for(Mapping<Item> missing : event.getMappings())
			if(knownMissing.contains(missing.key.getPath()))
				missing.ignore();
	}


	@SubscribeEvent
	public static void registerPotions(RegistryEvent.Register<Effect> event)
	{
		/*POTIONS*/
		IEPotions.init();
	}

	private static <T extends Block & IIEBlock> BlockIESlab addSlabFor(T b)
	{
		BlockIESlab<T> ret = new BlockIESlab<>(
				"slab_"+b.getRegistryName().getPath(),
				Block.Properties.from(b),
				BlockItemIE::new,
				b
		);
		IEBlocks.toSlab.put(b, ret);
		return ret;
	}

	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event)
	{
		event.getRegistry().registerAll(
				ChemthrowerShotEntity.TYPE,
				FluorescentTubeEntity.TYPE,
				IEExplosiveEntity.TYPE,
				RailgunShotEntity.TYPE,
				RevolvershotEntity.TYPE,
				RevolvershotFlareEntity.TYPE,
				RevolvershotHomingEntity.TYPE,
				SkylineHookEntity.TYPE,
				WolfpackShotEntity.TYPE,
				CrateMinecartEntity.TYPE,
				ReinforcedCrateMinecartEntity.TYPE,
				BarrelMinecartEntity.TYPE,
				MetalBarrelMinecartEntity.TYPE,
				SawbladeEntity.TYPE
		);
	}

	@SubscribeEvent
	public static void registerTEs(RegistryEvent.Register<TileEntityType<?>> event)
	{
		EnergyConnectorTileEntity.registerConnectorTEs(event);
		ConveyorHandler.registerConveyorTEs(event);

		registerTile(BalloonTileEntity.class, event, Cloth.balloon);
		registerTile(StripCurtainTileEntity.class, event, Cloth.curtain);
		registerTile(ShaderBannerTileEntity.class, event, Cloth.shaderBanner, Cloth.shaderBannerWall);

		registerTile(CokeOvenTileEntity.class, event, Multiblocks.cokeOven);
		registerTile(BlastFurnaceTileEntity.class, event, Multiblocks.blastFurnace);
		registerTile(BlastFurnaceAdvancedTileEntity.class, event, Multiblocks.blastFurnaceAdv);
		registerTile(CoresampleTileEntity.class, event, StoneDecoration.coresample);
		registerTile(AlloySmelterTileEntity.class, event, Multiblocks.alloySmelter);

		registerTile(CraftingTableTileEntity.class, event, WoodenDevices.craftingTable);
		registerTile(WoodenCrateTileEntity.class, event, WoodenDevices.crate);
		registerTile(WoodenBarrelTileEntity.class, event, WoodenDevices.woodenBarrel);
		registerTile(ModWorkbenchTileEntity.class, event, WoodenDevices.workbench);
		registerTile(SorterTileEntity.class, event, WoodenDevices.sorter);
		registerTile(ItemBatcherTileEntity.class, event, WoodenDevices.itemBatcher);
		registerTile(TurntableTileEntity.class, event, WoodenDevices.turntable);
		registerTile(FluidSorterTileEntity.class, event, WoodenDevices.fluidSorter);
		registerTile(WatermillTileEntity.class, event, WoodenDevices.watermill);
		registerTile(WindmillTileEntity.class, event, WoodenDevices.windmill);

		registerTile(RazorWireTileEntity.class, event, MetalDevices.razorWire);
		registerTile(ToolboxTileEntity.class, event, MetalDevices.toolbox);
		registerTile(StructuralArmTileEntity.class, event, MetalDecoration.slopeAlu, MetalDecoration.slopeSteel);

		registerTile(ConnectorStructuralTileEntity.class, event, Connectors.connectorStructural);
		registerTile(TransformerTileEntity.class, event, Connectors.transformer);
		registerTile(PostTransformerTileEntity.class, event, Connectors.postTransformer);
		registerTile(TransformerHVTileEntity.class, event, Connectors.transformerHV);
		registerTile(BreakerSwitchTileEntity.class, event, Connectors.breakerswitch);
		registerTile(RedstoneBreakerTileEntity.class, event, Connectors.redstoneBreaker);
		registerTile(EnergyMeterTileEntity.class, event, Connectors.currentTransformer);
		registerTile(ConnectorRedstoneTileEntity.class, event, Connectors.connectorRedstone);
		registerTile(ConnectorProbeTileEntity.class, event, Connectors.connectorProbe);
		registerTile(ConnectorBundledTileEntity.class, event, Connectors.connectorBundled);
		registerTile(FeedthroughTileEntity.class, event, Connectors.feedthrough);

		registerTile(CapacitorLVTileEntity.class, event, MetalDevices.capacitorLV);
		registerTile(CapacitorMVTileEntity.class, event, MetalDevices.capacitorMV);
		registerTile(CapacitorHVTileEntity.class, event, MetalDevices.capacitorHV);
		registerTile(CapacitorCreativeTileEntity.class, event, MetalDevices.capacitorCreative);
		registerTile(MetalBarrelTileEntity.class, event, MetalDevices.barrel);
		registerTile(FluidPumpTileEntity.class, event, MetalDevices.fluidPump);
		registerTile(FluidPlacerTileEntity.class, event, MetalDevices.fluidPlacer);

		registerTile(BlastFurnacePreheaterTileEntity.class, event, MetalDevices.blastFurnacePreheater);
		registerTile(FurnaceHeaterTileEntity.class, event, MetalDevices.furnaceHeater);
		registerTile(DynamoTileEntity.class, event, MetalDevices.dynamo);
		registerTile(ThermoelectricGenTileEntity.class, event, MetalDevices.thermoelectricGen);
		registerTile(ElectricLanternTileEntity.class, event, MetalDevices.electricLantern);
		registerTile(ChargingStationTileEntity.class, event, MetalDevices.chargingStation);
		registerTile(FluidPipeTileEntity.class, event, MetalDevices.fluidPipe);
		registerTile(SampleDrillTileEntity.class, event, MetalDevices.sampleDrill);
		registerTile(TeslaCoilTileEntity.class, event, MetalDevices.teslaCoil);
		registerTile(FloodlightTileEntity.class, event, MetalDevices.floodlight);
		registerTile(TurretChemTileEntity.class, event, MetalDevices.turretChem);
		registerTile(TurretGunTileEntity.class, event, MetalDevices.turretGun);
		registerTile(ClocheTileEntity.class, event, MetalDevices.cloche);
		registerTile(ChuteTileEntity.class, event, MetalDevices.chutes.values().toArray(new Block[0]));

		registerTile(MetalPressTileEntity.class, event, Multiblocks.metalPress);
		registerTile(CrusherTileEntity.class, event, Multiblocks.crusher);
		registerTile(SheetmetalTankTileEntity.class, event, Multiblocks.tank);
		registerTile(SiloTileEntity.class, event, Multiblocks.silo);
		registerTile(AssemblerTileEntity.class, event, Multiblocks.assembler);
		registerTile(AutoWorkbenchTileEntity.class, event, Multiblocks.autoWorkbench);
		registerTile(BottlingMachineTileEntity.class, event, Multiblocks.bottlingMachine);
		registerTile(SqueezerTileEntity.class, event, Multiblocks.squeezer);
		registerTile(FermenterTileEntity.class, event, Multiblocks.fermenter);
		registerTile(RefineryTileEntity.class, event, Multiblocks.refinery);
		registerTile(DieselGeneratorTileEntity.class, event, Multiblocks.dieselGenerator);
		registerTile(BucketWheelTileEntity.class, event, Multiblocks.bucketWheel);
		registerTile(ExcavatorTileEntity.class, event, Multiblocks.excavator);
		registerTile(ArcFurnaceTileEntity.class, event, Multiblocks.arcFurnace);
		registerTile(LightningrodTileEntity.class, event, Multiblocks.lightningrod);
		registerTile(MixerTileEntity.class, event, Multiblocks.mixer);
		registerTile(FakeLightTileEntity.class, event, Misc.fakeLight);
	}

	public static void init()
	{
		/*WORLDGEN*/
		addConfiguredWorldgen(Metals.ores.get(EnumMetals.COPPER), "copper", IEConfig.ORES.ore_copper);
		addConfiguredWorldgen(Metals.ores.get(EnumMetals.ALUMINUM), "bauxite", IEConfig.ORES.ore_bauxite);
		addConfiguredWorldgen(Metals.ores.get(EnumMetals.LEAD), "lead", IEConfig.ORES.ore_lead);
		addConfiguredWorldgen(Metals.ores.get(EnumMetals.SILVER), "silver", IEConfig.ORES.ore_silver);
		addConfiguredWorldgen(Metals.ores.get(EnumMetals.NICKEL), "nickel", IEConfig.ORES.ore_nickel);
		addConfiguredWorldgen(Metals.ores.get(EnumMetals.URANIUM), "uranium", IEConfig.ORES.ore_uranium);
		IEWorldGen.registerMineralVeinGen();

		CapabilityShader.register();
		NetHandlerCapability.register();
		CapabilitySkyhookData.register();
		ShaderRegistry.itemShader = IEItems.Misc.shader;
		ShaderRegistry.itemShaderBag = IEItems.Misc.shaderBag;
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.revolver));
		ShaderRegistry.itemExamples.add(new ItemStack(Tools.drill));
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.chemthrower));
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.railgun));
		ShaderRegistry.itemExamples.add(new ItemStack(IEItems.Misc.shield));

		/*BANNERS*/
		addBanner("hammer", "hmr", new ItemStack(Tools.hammer));
		addBanner("bevels", "bvl", "plateIron");
		addBanner("ornate", "orn", "dustSilver");
		addBanner("treated_wood", "twd", "plankTreatedWood");
		addBanner("windmill", "wnd", new ItemStack[]{new ItemStack(WoodenDevices.windmill)});
		ItemStack wolfpackCartridge = new ItemStack(BulletHandler.getBulletItem(BulletItem.WOLFPACK));
		addBanner("wolf_r", "wlfr", wolfpackCartridge, 1);
		addBanner("wolf_l", "wlfl", wolfpackCartridge, -1);
		addBanner("wolf", "wlf", wolfpackCartridge, 0, 0);

		/*ASSEMBLER RECIPE ADAPTERS*/
		//Fluid Ingredients
		AssemblerHandler.registerSpecialQueryConverters((o) ->
		{
			if(o instanceof IngredientFluidStack)
				return new RecipeQuery(((IngredientFluidStack)o).getFluid(), ((IngredientFluidStack)o).getFluid().getAmount());
			else return null;
		});

		DieselHandler.registerFuel(fluidBiodiesel, 125);
		DieselHandler.registerDrillFuel(fluidBiodiesel);

		fluidCreosote.block.setEffect(IEPotions.flammable, 100, 0);
		fluidEthanol.block.setEffect(Effects.NAUSEA, 70, 0);
		fluidBiodiesel.block.setEffect(IEPotions.flammable, 100, 1);
		fluidConcrete.block.setEffect(Effects.SLOWNESS, 20, 3);

		ExcavatorHandler.mineralVeinYield = IEConfig.MACHINES.excavator_yield.get();
		ExcavatorHandler.initialVeinDepletion = IEConfig.MACHINES.excavator_initial_depletion.get();
		ExcavatorHandler.mineralNoiseThreshold = IEConfig.MACHINES.excavator_theshold.get();

		ChemthrowerEffects.register();

		RailgunHandler.registerProjectileProperties(Ingredient.fromTag(IETags.ironRod), 15, 1.25).setColourMap(new int[][]{{0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868}});
		RailgunHandler.registerProjectileProperties(Ingredient.fromTag(IETags.aluminumRod), 13, 1.05).setColourMap(new int[][]{{0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868}});
		RailgunHandler.registerProjectileProperties(Ingredient.fromTag(IETags.steelRod), 18, 1.25).setColourMap(new int[][]{{0xb4b4b4, 0xb4b4b4, 0xb4b4b4, 0x7a7a7a, 0x555555, 0x555555}});
		RailgunHandler.registerProjectileProperties(new ItemStack(IEItems.Misc.graphiteElectrode), 24, .9).setColourMap(new int[][]{{0x242424, 0x242424, 0x242424, 0x171717, 0x171717, 0x0a0a0a}});

		ExternalHeaterHandler.defaultFurnaceEnergyCost = IEConfig.MACHINES.heater_consumption.get();
		ExternalHeaterHandler.defaultFurnaceSpeedupCost = IEConfig.MACHINES.heater_speedupConsumption.get();
		ExternalHeaterHandler.registerHeatableAdapter(FurnaceTileEntity.class, new DefaultFurnaceAdapter());

		ThermoelectricHandler.registerSourceInKelvin(Blocks.MAGMA_BLOCK, 1300);
		//TODO tags?
		ThermoelectricHandler.registerSourceInKelvin(Blocks.ICE, 273);
		ThermoelectricHandler.registerSourceInKelvin(Blocks.PACKED_ICE, 200);
		ThermoelectricHandler.registerSourceInKelvin(IETags.getTagsFor(EnumMetals.URANIUM).storage, 2000);
		//ThermoelectricHandler.registerSourceInKelvin(new ResourceLocation("forge:storage_blocks/yellorium"), 2000);
		//ThermoelectricHandler.registerSourceInKelvin(new ResourceLocation("forge:storage_blocks/plutonium"), 4000);
		//ThermoelectricHandler.registerSourceInKelvin(new ResourceLocation("forge:storage_blocks/blutonium"), 4000);

		/*MULTIBLOCKS*/
		IEMultiblocks.init();
		MultiblockHandler.registerMultiblock(IEMultiblocks.FEEDTHROUGH);
		MultiblockHandler.registerMultiblock(IEMultiblocks.LIGHTNING_ROD);
		MultiblockHandler.registerMultiblock(IEMultiblocks.DIESEL_GENERATOR);
		MultiblockHandler.registerMultiblock(IEMultiblocks.REFINERY);
		MultiblockHandler.registerMultiblock(IEMultiblocks.MIXER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.SQUEEZER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.FERMENTER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.BOTTLING_MACHINE);
		MultiblockHandler.registerMultiblock(IEMultiblocks.COKE_OVEN);
		MultiblockHandler.registerMultiblock(IEMultiblocks.ALLOY_SMELTER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.BLAST_FURNACE);
		MultiblockHandler.registerMultiblock(IEMultiblocks.CRUSHER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.ADVANCED_BLAST_FURNACE);
		MultiblockHandler.registerMultiblock(IEMultiblocks.METAL_PRESS);
		MultiblockHandler.registerMultiblock(IEMultiblocks.ASSEMBLER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.AUTO_WORKBENCH);
		MultiblockHandler.registerMultiblock(IEMultiblocks.EXCAVATOR);
		MultiblockHandler.registerMultiblock(IEMultiblocks.BUCKET_WHEEL);
		MultiblockHandler.registerMultiblock(IEMultiblocks.ARC_FURNACE);
		MultiblockHandler.registerMultiblock(IEMultiblocks.SILO);
		MultiblockHandler.registerMultiblock(IEMultiblocks.SHEETMETAL_TANK);
		MultiblockHandler.registerMultiblock(IEMultiblocks.EXCAVATOR_DEMO);

		/*BLOCK ITEMS FROM CRATES*/
		IEApi.forbiddenInCrates.add((stack) -> {
			if(stack.getItem()==Tools.toolbox)
				return true;
			if(stack.getItem()==WoodenDevices.crate.asItem())
				return true;
			if(stack.getItem()==WoodenDevices.reinforcedCrate.asItem())
				return true;
			return Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock;
		});

		FluidPipeTileEntity.initCovers();
		LocalNetworkHandler.register(EnergyTransferHandler.ID, EnergyTransferHandler::new);
		LocalNetworkHandler.register(RedstoneNetworkHandler.ID, RedstoneNetworkHandler::new);
		LocalNetworkHandler.register(WireDamageHandler.ID, WireDamageHandler::new);
	}

	public static void postInit()
	{
		Villages.init();
	}

	public static <T extends TileEntity> void registerTile(Class<T> tile, Register<TileEntityType<?>> event, Block... valid)
	{
		String s = tile.getSimpleName();
		s = s.substring(0, s.indexOf("TileEntity")).toLowerCase(Locale.ENGLISH);
		Set<Block> validSet = new HashSet<>(Arrays.asList(valid));
		TileEntityType<T> type = new TileEntityType<>(() -> {
			try
			{
				return tile.newInstance();
			} catch(InstantiationException|IllegalAccessException e)
			{
				e.printStackTrace();
			}
			return null;
		}, validSet, null);
		type.setRegistryName(MODID, s);
		event.getRegistry().register(type);
		try
		{
			Field typeField = tile.getField("TYPE");
			typeField.set(null, type);
		} catch(NoSuchFieldException|IllegalAccessException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		registeredIETiles.add(tile);
	}

	public static void addConfiguredWorldgen(Block state, String name, OreConfig config)
	{
		if(config!=null&&config.veinSize.get() > 0)
			IEWorldGen.addOreGen(name, state.getDefaultState(), config.veinSize.get(),
					config.minY.get(),
					config.maxY.get(),
					config.veinsPerChunk.get());
	}

	public static void addBanner(String name, String id, Object item, int... offset)
	{
		name = MODID+"_"+name;
		id = "ie_"+id;
		ItemStack craftingStack = ItemStack.EMPTY;
		if(item instanceof ItemStack&&(offset==null||offset.length < 1))
			craftingStack = (ItemStack)item;
		/*TODO
		BannerPattern e = EnumHelper.addEnum(BannerPattern.class, name.toUpperCase(), new Class[]{String.class, String.class, ItemStack.class}, name, id, craftingStack);
		if(craftingStack.isEmpty())
			RecipeBannerAdvanced.addAdvancedPatternRecipe(e, ApiUtils.createIngredientStack(item), offset);

		 */
	}
}
