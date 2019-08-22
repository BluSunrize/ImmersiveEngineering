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
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.energy.wires.NetHandlerCapability;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.WireDamageHandler;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.*;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Extinguish;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.DefaultFurnaceAdapter;
import blusunrize.immersiveengineering.common.blocks.*;
import blusunrize.immersiveengineering.common.blocks.FakeLightBlock.FakeLightTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.TileDropLootFunction.Serializer;
import blusunrize.immersiveengineering.common.blocks.cloth.*;
import blusunrize.immersiveengineering.common.blocks.generic.*;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.*;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.stone.*;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingThreadHandler;
import blusunrize.immersiveengineering.common.crafting.IngredientFluidStack;
import blusunrize.immersiveengineering.common.crafting.MixerRecipePotion;
import blusunrize.immersiveengineering.common.crafting.RecipeBannerAdvanced;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.ItemBullet.WolfpackBullet;
import blusunrize.immersiveengineering.common.items.ItemBullet.WolfpackPartBullet;
import blusunrize.immersiveengineering.common.items.tools.ItemIEAxe;
import blusunrize.immersiveengineering.common.items.tools.ItemIEPickaxe;
import blusunrize.immersiveengineering.common.items.tools.ItemIEShovel;
import blusunrize.immersiveengineering.common.items.tools.ItemIESword;
import blusunrize.immersiveengineering.common.util.IEFluid;
import blusunrize.immersiveengineering.common.util.IEFluid.FluidPotion;
import blusunrize.immersiveengineering.common.util.IELootFunctions;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.IEVillagerHandler;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.VillageEngineersHouse;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.potion.*;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.IRegistryDelegate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

@Mod.EventBusSubscriber
public class IEContent
{
	public static ArrayList<Block> registeredIEBlocks = new ArrayList<Block>();
	public static ArrayList<Item> registeredIEItems = new ArrayList<Item>();
	public static List<Class<? extends TileEntity>> registeredIETiles = new ArrayList<>();

	public static Fluid fluidCreosote;
	public static Fluid fluidPlantoil;
	public static Fluid fluidEthanol;
	public static Fluid fluidBiodiesel;
	public static Fluid fluidConcrete;

	public static Fluid fluidPotion;

	static
	{
		LootFunctionManager.registerFunction(new Serializer());

		fluidCreosote = setupFluid(new Fluid("creosote", new ResourceLocation("immersiveengineering:blocks/fluid/creosote_still"), new ResourceLocation("immersiveengineering:blocks/fluid/creosote_flow")).setDensity(1100).setViscosity(3000));
		fluidPlantoil = setupFluid(new Fluid("plantoil", new ResourceLocation("immersiveengineering:blocks/fluid/plantoil_still"), new ResourceLocation("immersiveengineering:blocks/fluid/plantoil_flow")).setDensity(925).setViscosity(2000));
		fluidEthanol = setupFluid(new Fluid("ethanol", new ResourceLocation("immersiveengineering:blocks/fluid/ethanol_still"), new ResourceLocation("immersiveengineering:blocks/fluid/ethanol_flow")).setDensity(789).setViscosity(1000));
		fluidBiodiesel = setupFluid(new Fluid("biodiesel", new ResourceLocation("immersiveengineering:blocks/fluid/biodiesel_still"), new ResourceLocation("immersiveengineering:blocks/fluid/biodiesel_flow")).setDensity(789).setViscosity(1000));
		fluidConcrete = setupFluid(new Fluid("concrete", new ResourceLocation("immersiveengineering:blocks/fluid/concrete_still"), new ResourceLocation("immersiveengineering:blocks/fluid/concrete_flow")).setDensity(2400).setViscosity(4000));
		fluidPotion = setupFluid(new FluidPotion("potion", new ResourceLocation("immersiveengineering:blocks/fluid/potion_still"), new ResourceLocation("immersiveengineering:blocks/fluid/potion_flow")));

		Block.Properties storageProperties = Block.Properties.create(Material.IRON).hardnessAndResistance(5, 10);
		Block.Properties sheetmetalProperties = Block.Properties.create(Material.IRON).hardnessAndResistance(3, 10);
		for(EnumMetals m : EnumMetals.values())
		{
			String name = m.name().toLowerCase();
			Block storage;
			Block ore;
			Block sheetmetal = new IEBaseBlock("sheetmetal_"+name, sheetmetalProperties, ItemBlockIEBase.class)
					.setOpaque(true);
			addSlabFor(sheetmetal);
			if(!m.isVanillaMetal())
			{
				ore = new IEBaseBlock("ore_"+m.name().toLowerCase(), Block.Properties.create(Material.ROCK)
						.hardnessAndResistance(3, 5), ItemBlockIEBase.class)
						.setOpaque(true);
				storage = new IEBaseBlock("storage_"+m.name().toLowerCase(), storageProperties, ItemBlockIEBase.class)
						.setOpaque(true);
				addSlabFor(storage);
			}
			else if(m==EnumMetals.IRON)
			{
				storage = Blocks.IRON_BLOCK;
				ore = Blocks.IRON_ORE;
			}
			else if(m==EnumMetals.GOLD)
			{
				storage = Blocks.GOLD_BLOCK;
				ore = Blocks.GOLD_ORE;
			}
			else
			{
				throw new RuntimeException("Unkown vanilla metal: "+m.name());
			}
			IEBlocks.Metals.storage.put(m, storage);
			IEBlocks.Metals.ores.put(m, ore);
			IEBlocks.Metals.sheetmetal.put(m, sheetmetal);
		}
		Block.Properties stoneDecoProps = Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 10);
		Block.Properties stoneDecoLeadedProps = Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 180);

		IEBlocks.StoneDecoration.cokebrick = new IEBaseBlock("cokebrick", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.blastbrick = new IEBaseBlock("blastbrick", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.blastbrickReinforced = new IEBaseBlock("blastbrick_reinforced", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.coke = new IEBaseBlock("coke", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.hempcrete = new IEBaseBlock("hempcrete", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.concrete = new IEBaseBlock("concrete", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.concreteTile = new IEBaseBlock("concrete_tile", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.concreteLeaded = new IEBaseBlock("concrete_leaded", stoneDecoLeadedProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.alloybrick = new IEBaseBlock("alloybrick", stoneDecoProps, ItemBlockIEBase.class);

		IEBlocks.StoneDecoration.insulatingGlass = new IEBaseBlock("insulating_glass", stoneDecoProps, ItemBlockIEBase.class)
		{
			@Override
			public int getOpacity(BlockState p_200011_1_, IBlockReader p_200011_2_, BlockPos p_200011_3_)
			{
				return 0;
			}
		}
				.setBlockLayer(BlockRenderLayer.TRANSLUCENT)
				.setNotNormalBlock();
		IEBlocks.StoneDecoration.concreteSprayed = new IEBaseBlock("concrete_sprayed", Block.Properties.create(Material.ROCK).hardnessAndResistance(.2F, 1), ItemBlockIEBase.class)
		{

			@Override
			public ResourceLocation getLootTable()
			{
				return new ResourceLocation(ImmersiveEngineering.MODID, "empty");
			}

			@Override
			public int getOpacity(BlockState p_200011_1_, IBlockReader p_200011_2_, BlockPos p_200011_3_)
			{
				return 0;
			}
		}.setNotNormalBlock().setHammerHarvest().setBlockLayer(BlockRenderLayer.CUTOUT);
		addSlabFor(IEBlocks.StoneDecoration.cokebrick);
		addSlabFor(IEBlocks.StoneDecoration.blastbrick);
		addSlabFor(IEBlocks.StoneDecoration.blastbrickReinforced);
		addSlabFor(IEBlocks.StoneDecoration.coke);
		addSlabFor(IEBlocks.StoneDecoration.hempcrete);
		addSlabFor(IEBlocks.StoneDecoration.concrete);
		addSlabFor(IEBlocks.StoneDecoration.concreteTile);
		addSlabFor(IEBlocks.StoneDecoration.concreteLeaded);
		addSlabFor(IEBlocks.StoneDecoration.insulatingGlass);
		addSlabFor(IEBlocks.StoneDecoration.concreteSprayed);
		addSlabFor(IEBlocks.StoneDecoration.alloybrick);

		StoneDecoration.hempcreteStairs = new IEStairsBlock("stone_decoration_stairs_hempcrete", StoneDecoration.hempcrete.getDefaultState(), stoneDecoProps);
		StoneDecoration.concreteStairs[0] = new IEStairsBlock("stone_decoration_stairs_concrete", StoneDecoration.concrete.getDefaultState(), stoneDecoProps);
		StoneDecoration.concreteStairs[1] = new IEStairsBlock("stone_decoration_stairs_concrete_tile", StoneDecoration.concreteTile.getDefaultState(), stoneDecoProps);
		StoneDecoration.concreteStairs[2] = new IEStairsBlock("stone_decoration_stairs_concrete_leaded", StoneDecoration.concreteLeaded.getDefaultState(), stoneDecoLeadedProps);

		Multiblocks.cokeOven = new StoneMultiBlock("coke_oven", () -> CokeOvenTileEntity.TYPE);
		Multiblocks.blastFurnace = new StoneMultiBlock("coke_oven", () -> BlastFurnaceTileEntity.TYPE);
		Multiblocks.alloySmelter = new StoneMultiBlock("coke_oven", () -> AlloySmelterTileEntity.TYPE);
		Multiblocks.blastFurnaceAdv = new StoneMultiBlock("coke_oven", () -> BlastFurnaceAdvancedTileEntity.TYPE);

		Block.Properties standardWoodProperties = Block.Properties.create(Material.WOOD).hardnessAndResistance(2, 5);
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			Block baseBlock = new IEBaseBlock("treated_wood_"+style.name().toLowerCase(), standardWoodProperties, ItemBlockIEBase.class)
					.setOpaque(true)
					.setHasFlavour(true);
			WoodenDecoration.treatedWood.put(style, baseBlock);
			addSlabFor(baseBlock);
			WoodenDecoration.treatedStairs.put(style,
					new IEStairsBlock("treated_wood_stairs_"+style.name().toLowerCase(), baseBlock.getDefaultState(), standardWoodProperties)
							.setHasFlavour(true));
		}
		WoodenDecoration.treatedFence = new IEFenceBlock("treated_fence", standardWoodProperties);
		WoodenDecoration.treatedScaffolding = new ScaffoldingBlock("treated_scaffold", standardWoodProperties);


		WoodenDevices.workbench = new ModWorkbenchBlock("workbench");
		WoodenDevices.gunpowderBarrel = new GunpowderBarrelBlock("gunpowder_barrel");
		WoodenDevices.woodenBarrel = new BarrelBlock("wooden_barrel", false);
		WoodenDevices.turntable = new TurntableBlock("turntable");
		WoodenDevices.crate = new CrateBlock("crate", false);
		WoodenDevices.reinforcedCrate = new CrateBlock("reinforced_crate", true);
		WoodenDevices.sorter = new SorterBlock("sorter", false);
		WoodenDevices.fluidSorter = new SorterBlock("fluid_sorter", true);
		WoodenDevices.windmill = new WindmillBlock("windmill");
		WoodenDevices.watermill = new WatermillBlock("watermill");
		WoodenDevices.treatedPost = new PostBlock("treated_post", standardWoodProperties);
		WoodenDevices.treatedWallmount = new WallmountBlock("treated_wallmount", standardWoodProperties);
		IEBlocks.Misc.crop = new HempBlock("hemp");

		Cloth.cushion = new CushionBlock();
		Cloth.balloon = new BalloonBlock();
		Cloth.curtain = new StripCurtainBlock();
		Cloth.shaderBanner = new ShaderBannerBlock();

		Misc.fakeLight = new FakeLightBlock();


		Block.Properties defaultMetalProperties = Block.Properties.create(Material.IRON).hardnessAndResistance(3, 15);
		MetalDecoration.lvCoil = new IEBaseBlock("coil_lv", defaultMetalProperties, ItemBlockIEBase.class);
		MetalDecoration.mvCoil = new IEBaseBlock("coil_mv", defaultMetalProperties, ItemBlockIEBase.class);
		MetalDecoration.hvCoil = new IEBaseBlock("coil_hv", defaultMetalProperties, ItemBlockIEBase.class);
		MetalDecoration.engineeringRS = new IEBaseBlock("rs_engineering", defaultMetalProperties, ItemBlockIEBase.class);
		MetalDecoration.engineeringHeavy = new IEBaseBlock("heavy_engineering", defaultMetalProperties, ItemBlockIEBase.class);
		MetalDecoration.engineeringLight = new IEBaseBlock("light_engineering", defaultMetalProperties, ItemBlockIEBase.class);
		MetalDecoration.generator = new IEBaseBlock("generator", defaultMetalProperties, ItemBlockIEBase.class);
		MetalDecoration.radiator = new IEBaseBlock("radiator", defaultMetalProperties, ItemBlockIEBase.class);
		MetalDecoration.steelFence = new IEFenceBlock("steel_fence", defaultMetalProperties);
		MetalDecoration.aluFence = new IEFenceBlock("steel_fence", defaultMetalProperties);
		MetalDecoration.steelWallmount = new WallmountBlock("steel_wallmount", defaultMetalProperties);
		MetalDecoration.aluWallmount = new WallmountBlock("alu_wallmount", defaultMetalProperties);
		MetalDecoration.steelPost = new PostBlock("steel_post", defaultMetalProperties);
		MetalDecoration.aluPost = new PostBlock("alu_post", defaultMetalProperties);
		MetalDecoration.lantern = new LanternBlock("lantern");
		MetalDecoration.slopeSteel = new StructuralArmBlock("steel_slope");
		MetalDecoration.slopeAlu = new StructuralArmBlock("alu_slope");
		MetalDecoration.metalLadder = new MetalLadderBlock();
		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			String name = type.name().toLowerCase();
			Block steelBlock = new ScaffoldingBlock("steel_scaffolding_"+name, defaultMetalProperties);
			Block aluBlock = new ScaffoldingBlock("alu_scaffolding_"+name, defaultMetalProperties);
			MetalDecoration.steelScaffolding.put(type, steelBlock);
			MetalDecoration.aluScaffolding.put(type, aluBlock);
			MetalDecoration.steelScaffoldingStair.put(type, new IEStairsBlock("steel_scaffolding_stairs_"+name,
					steelBlock.getDefaultState(), defaultMetalProperties));
			MetalDecoration.aluScaffoldingStair.put(type, new IEStairsBlock("alu_scaffolding_stairs_"+name,
					aluBlock.getDefaultState(), defaultMetalProperties));
			addSlabFor(steelBlock);
			addSlabFor(aluBlock);
		}
		for(String cat : new String[]{WireType.LV_CATEGORY, WireType.MV_CATEGORY, WireType.HV_CATEGORY})
		{
			Block connector = new PowerConnectorBlock(cat, false);
			Block relay = new PowerConnectorBlock(cat, true);
			Connectors.ENERGY_CONNECTORS.put(new ImmutablePair<>(cat, false), connector);
			Connectors.ENERGY_CONNECTORS.put(new ImmutablePair<>(cat, true), relay);
		}

		Connectors.connectorStructural = new MiscConnectorBlock("connector_structural", () -> ConnectorStructuralTileEntity.TYPE);
		Connectors.transformer = new MiscConnectorBlock("transformer", () -> TransformerTileEntity.TYPE);
		Connectors.transformerHV = new MiscConnectorBlock("transformer_hv", () -> TransformerHVTileEntity.TYPE);
		Connectors.breakerswitch = new MiscConnectorBlock("breaker_switch", () -> BreakerSwitchTileEntity.TYPE);
		Connectors.redstoneBreaker = new MiscConnectorBlock("redstone_breaker", () -> RedstoneBreakerTileEntity.TYPE);
		Connectors.energyMeter = new MiscConnectorBlock("current_transformer", () -> EnergyMeterTileEntity.TYPE);
		Connectors.connectorRedstone = new MiscConnectorBlock("connector_redstone", () -> ConnectorRedstoneTileEntity.TYPE);
		Connectors.connectorProbe = new MiscConnectorBlock("connector_probe", () -> ConnectorProbeTileEntity.TYPE);
		Connectors.feedthrough = new MiscConnectorBlock("feedthrough", () -> FeedthroughTileEntity.TYPE);

		MetalDevices.razorWire = new MiscConnectorBlock("razor_wire", () -> RazorWireTileEntity.TYPE);
		MetalDevices.toolbox = new GenericTileBlock("toolbox_block", () -> ToolboxTileEntity.TYPE, defaultMetalProperties,
				null, new IProperty[0]);
		MetalDevices.capacitorLV = new GenericTileBlock("capacitor_lv", () -> CapacitorLVTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.capacitorMV = new GenericTileBlock("capacitor_mv", () -> CapacitorMVTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.capacitorHV = new GenericTileBlock("capacitor_hv", () -> CapacitorHVTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.capacitorCreative = new GenericTileBlock("capacitor_hv", () -> CapacitorHVTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.barrel = new BarrelBlock("barrel", false);
		//MetalDevices.fluidPump = ;
		//MetalDevices.fluidPlacer = ;
		MetalDevices.blastFurnacePreheater = new GenericTileBlock("blastfurnace_preheater", () -> BlastFurnacePreheaterTileEntity.TYPE,
				defaultMetalProperties);
		MetalDevices.furnaceHeater = new GenericTileBlock("furnace_heater", () -> FurnaceHeaterTileEntity.TYPE,
				defaultMetalProperties);
		MetalDevices.dynamo = new GenericTileBlock("dynamo", () -> DynamoTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.thermoelectricGen = new GenericTileBlock("thermoelectric_generator", () -> ThermoelectricGenTileEntity.TYPE,
				defaultMetalProperties);
		MetalDevices.electricLantern = new MiscConnectorBlock("electric_lantern", () -> ElectricLanternTileEntity.TYPE);
		MetalDevices.chargingStation = new GenericTileBlock("charging_station", () -> ChargingStationTileEntity.TYPE,
				defaultMetalProperties);
		//MetalDevices.fluidPipe = ;
		MetalDevices.sampleDrill = new GenericTileBlock("sample_drill", () -> SampleDrillTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.teslaCoil = new GenericTileBlock("tesla_coil", () -> TeslaCoilTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.floodlight = new MiscConnectorBlock("floodlight", () -> FloodlightTileEntity.TYPE);
		MetalDevices.turretChem = new GenericTileBlock("turret_chem", () -> TurretChemTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.turretGun = new GenericTileBlock("turret_gun", () -> TurretGunTileEntity.TYPE, defaultMetalProperties);
		MetalDevices.belljar = new GenericTileBlock("cloche", () -> BelljarTileEntity.TYPE, defaultMetalProperties);

		blockMetalMultiblock = new MetalMultiblocksBlock();

		blockFluidCreosote = new BlockIEFluid("fluidCreosote", fluidCreosote, Material.WATER).setFlammability(40, 400);
		blockFluidPlantoil = new BlockIEFluid("fluidPlantoil", fluidPlantoil, Material.WATER);
		blockFluidEthanol = new BlockIEFluid("fluidEthanol", fluidEthanol, Material.WATER).setFlammability(60, 600);
		blockFluidBiodiesel = new BlockIEFluid("fluidBiodiesel", fluidBiodiesel, Material.WATER).setFlammability(60, 200);
		blockFluidConcrete = new BlockIEFluidConcrete("fluidConcrete", fluidConcrete, Material.WATER);

		itemTool = new ItemWirecutter();
		itemSteelPick = new ItemIEPickaxe(Lib.MATERIAL_Steel, "pickaxe_steel", "pickaxe", "ingotSteel");
		itemSteelShovel = new ItemIEShovel(Lib.MATERIAL_Steel, "shovel_steel", "shovel", "ingotSteel");
		itemSteelAxe = new ItemIEAxe(Lib.MATERIAL_Steel, "axe_steel", "axe", "ingotSteel");
		itemSteelSword = new ItemIESword(Lib.MATERIAL_Steel, "sword_steel", "ingotSteel");
		itemToolbox = new ItemToolbox();
		itemWireCoil = new ItemWireCoil();
		WireType.ieWireCoil = itemWireCoil;
		itemSeeds = new ItemIESeed(blockCrop, "hemp");
		if(IEConfig.hempSeedWeight > 0)
			MinecraftForge.addGrassSeed(new ItemStack(itemSeeds), IEConfig.hempSeedWeight);
		itemDrill = new ItemDrill();
		itemDrillhead = new ItemDrillhead();
		itemJerrycan = new ItemJerrycan();
		itemBlueprint = new ItemEngineersBlueprint().setRegisterSubModels(false);
		BlueprintCraftingRecipe.itemBlueprint = itemBlueprint;
		itemRevolver = new ItemRevolver();
		itemSpeedloader = new ItemSpeedloader();
		itemBullet = new ItemBullet();
		itemChemthrower = new ItemChemthrower();
		itemRailgun = new ItemRailgun();
		itemSkyhook = new ItemSkyhook();
		itemToolUpgrades = new ItemToolUpgrade();
		itemShader = new ItemShader();
		itemShaderBag = new ItemShaderBag();
		itemEarmuffs = new ItemEarmuffs();
		itemCoresample = new ItemCoresample();
		itemGraphiteElectrode = new ItemGraphiteElectrode();
		ItemFaradaySuit.mat = EnumHelper.addArmorMaterial("faradayChains", "immersiveengineering:faradaySuit", 1, new int[]{1, 3, 2, 1}, 0, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0);
		for(int i = 0; i < itemsFaradaySuit.length; i++)
			itemsFaradaySuit[i] = new ItemFaradaySuit(EquipmentSlotType.values()[2+i]);
		itemFluorescentTube = new ItemFluorescentTube();
		itemPowerpack = new ItemPowerpack();
		itemShield = new ItemIEShield();
		itemMaintenanceKit = new ItemMaintenanceKit();

		itemFakeIcons = new ItemIEBase("fake_icon", 1, "birthday", "lucky", "drillbreak")
		{
			@Override
			public void fillItemGroup(CreativeTabs tab, NonNullList<ItemStack> list)
			{
			}
		};
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		ConveyorHandler.registerConveyorBlocks(event);
		for(Block block : registeredIEBlocks)
			event.getRegistry().register(block.setRegistryName(createRegistryName(block.getTranslationKey())));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		for(Item item : registeredIEItems)
			event.getRegistry().register(item.setRegistryName(createRegistryName(item.getTranslationKey())));

		registerOres();
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

	private static Block addSlabFor(Block b)
	{
		BlockIESlab ret = new BlockIESlab("slab_"+b.getRegistryName().getPath(), Block.Properties.from(b), ItemBlockIESlabs.class);
		IEBlocks.toSlab.put(b, ret);
		return ret;
	}

	public static void registerTEs(RegistryEvent.Register<TileEntityType<?>> event)
	{
		EnergyConnectorTileEntity.registerConnectorTEs(event);
		ConveyorHandler.registerConveyorTEs(event);

		registerTile(BalloonTileEntity.class, event, Cloth.balloon);
		registerTile(StripCurtainTileEntity.class, event, Cloth.curtain);
		registerTile(ShaderBannerTileEntity.class, event, Cloth.shaderBanner);

		registerTile(CokeOvenTileEntity.class, event, Multiblocks.cokeOven);
		registerTile(BlastFurnaceTileEntity.class, event, Multiblocks.blastFurnace);
		registerTile(BlastFurnaceAdvancedTileEntity.class, event, Multiblocks.blastFurnaceAdv);
		registerTile(CoresampleTileEntity.class, event, StoneDecoration.coresample);
		registerTile(AlloySmelterTileEntity.class, event, Multiblocks.alloySmelter);

		registerTile(WoodenCrateTileEntity.class, event, WoodenDevices.crate);
		registerTile(WoodenBarrelTileEntity.class, event, WoodenDevices.woodenBarrel);
		registerTile(ModWorkbenchTileEntity.class, event, WoodenDevices.workbench);
		registerTile(SorterTileEntity.class, event, WoodenDevices.sorter);
		registerTile(TurntableTileEntity.class, event, WoodenDevices.turntable);
		registerTile(FluidSorterTileEntity.class, event, WoodenDevices.fluidSorter);
		registerTile(WatermillTileEntity.class, event, WoodenDevices.watermill);
		registerTile(WindmillTileEntity.class, event, WoodenDevices.windmill);
		registerTile(PostTileEntity.class, event, WoodenDevices.treatedPost, MetalDecoration.aluPost,
				MetalDecoration.steelPost);

		registerTile(LadderTileEntity.class, event, MetalDecoration.metalLadder);
		registerTile(LanternTileEntity.class, event, MetalDecoration.lantern);
		registerTile(RazorWireTileEntity.class, event, MetalDevices.razorWire);
		registerTile(ToolboxTileEntity.class, event, MetalDevices.toolbox);
		registerTile(StructuralArmTileEntity.class, event, MetalDecoration.slopeAlu, MetalDecoration.slopeSteel);

		registerTile(ConnectorStructuralTileEntity.class, event, Connectors.connectorStructural);
		registerTile(TransformerTileEntity.class, event, Connectors.transformer);
		registerTile(TransformerHVTileEntity.class, event, Connectors.transformerHV);
		registerTile(BreakerSwitchTileEntity.class, event, Connectors.breakerswitch);
		registerTile(RedstoneBreakerTileEntity.class, event, Connectors.redstoneBreaker);
		registerTile(EnergyMeterTileEntity.class, event, Connectors.energyMeter);
		registerTile(ConnectorRedstoneTileEntity.class, event, Connectors.connectorRedstone);
		registerTile(ConnectorProbeTileEntity.class, event, Connectors.connectorProbe);
		registerTile(FeedthroughTileEntity.class, event, Connectors.feedthrough);

		registerTile(CapacitorLVTileEntity.class, event, MetalDevices.capacitorLV);
		registerTile(CapacitorMVTileEntity.class, event, MetalDevices.capacitorMV);
		registerTile(CapacitorHVTileEntity.class, event, MetalDevices.capacitorHV);
		registerTile(CapacitorCreativeTileEntity.class, event, MetalDevices.capacitorCreative);
		registerTile(MetalBarrelTileEntity.class, event, MetalDevices.barrel);
		//registerTile(FluidPumpTileEntity.class, event, MetalDevices.fluidPump);
		//registerTile(FluidPlacerTileEntity.class, event, MetalDevices.fluidPipe);

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
		registerTile(BelljarTileEntity.class, event, MetalDevices.belljar);

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
		//		registerTile(TileEntitySkycrateDispenser.class);
		registerTile(FakeLightTileEntity.class, event, Misc.fakeLight);
		EnergyConnectorTileEntity.registerConnectorTEs(event);
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
	{
		/*CRAFTING*/
		IERecipes.initCraftingRecipes(event.getRegistry());

		/*FURNACE*/
		IERecipes.initFurnaceRecipes();

		/*BLUEPRINTS*/
		IERecipes.initBlueprintRecipes();

		/*BELLJAR*/
		BelljarHandler.init();

		/*EXCAVATOR*/
		ExcavatorHandler.mineralVeinCapacity = IEConfig.MACHINES.excavator_depletion;
		ExcavatorHandler.mineralChance = IEConfig.MACHINES.excavator_chance;
		ExcavatorHandler.defaultDimensionBlacklist = IEConfig.MACHINES.excavator_dimBlacklist;
		String sulfur = OreDictionary.doesOreNameExist("oreSulfur")?"oreSulfur": "dustSulfur";
		ExcavatorHandler.addMineral("Iron", 25, .1f, new String[]{"oreIron", "oreNickel", "oreTin", "denseoreIron"}, new float[]{.5f, .25f, .20f, .05f});
		ExcavatorHandler.addMineral("Magnetite", 25, .1f, new String[]{"oreIron", "oreGold"}, new float[]{.85f, .15f});
		ExcavatorHandler.addMineral("Pyrite", 20, .1f, new String[]{"oreIron", sulfur}, new float[]{.5f, .5f});
		ExcavatorHandler.addMineral("Bauxite", 20, .2f, new String[]{"oreAluminum", "oreTitanium", "denseoreAluminum"}, new float[]{.90f, .05f, .05f});
		ExcavatorHandler.addMineral("Copper", 30, .2f, new String[]{"oreCopper", "oreGold", "oreNickel", "denseoreCopper"}, new float[]{.65f, .25f, .05f, .05f});
		if(OreDictionary.doesOreNameExist("oreTin"))
			ExcavatorHandler.addMineral("Cassiterite", 15, .2f, new String[]{"oreTin", "denseoreTin"}, new float[]{.95f, .05f});
		ExcavatorHandler.addMineral("Gold", 20, .3f, new String[]{"oreGold", "oreCopper", "oreNickel", "denseoreGold"}, new float[]{.65f, .25f, .05f, .05f});
		ExcavatorHandler.addMineral("Nickel", 20, .3f, new String[]{"oreNickel", "orePlatinum", "oreIron", "denseoreNickel"}, new float[]{.85f, .05f, .05f, .05f});
		if(OreDictionary.doesOreNameExist("orePlatinum"))
			ExcavatorHandler.addMineral("Platinum", 5, .35f, new String[]{"orePlatinum", "oreNickel", "", "oreIridium", "denseorePlatinum"}, new float[]{.40f, .30f, .15f, .1f, .05f});
		ExcavatorHandler.addMineral("Uranium", 10, .35f, new String[]{"oreUranium", "oreLead", "orePlutonium", "denseoreUranium"}, new float[]{.55f, .3f, .1f, .05f}).addReplacement("oreUranium", "oreYellorium");
		ExcavatorHandler.addMineral("Quartzite", 5, .3f, new String[]{"oreQuartz", "oreCertusQuartz"}, new float[]{.6f, .4f});
		ExcavatorHandler.addMineral("Galena", 15, .2f, new String[]{"oreLead", "oreSilver", "oreSulfur", "denseoreLead", "denseoreSilver"}, new float[]{.40f, .40f, .1f, .05f, .05f});
		ExcavatorHandler.addMineral("Lead", 10, .15f, new String[]{"oreLead", "oreSilver", "denseoreLead"}, new float[]{.55f, .4f, .05f});
		ExcavatorHandler.addMineral("Silver", 10, .2f, new String[]{"oreSilver", "oreLead", "denseoreSilver"}, new float[]{.55f, .4f, .05f});
		ExcavatorHandler.addMineral("Lapis", 10, .2f, new String[]{"oreLapis", "oreIron", sulfur, "denseoreLapis"}, new float[]{.65f, .275f, .025f, .05f});
		ExcavatorHandler.addMineral("Cinnabar", 15, .1f, new String[]{"oreRedstone", "denseoreRedstone", "oreRuby", "oreCinnabar", sulfur}, new float[]{.75f, .05f, .05f, .1f, .05f});
		ExcavatorHandler.addMineral("Coal", 25, .1f, new String[]{"oreCoal", "denseoreCoal", "oreDiamond", "oreEmerald"}, new float[]{.92f, .1f, .015f, .015f});
		ExcavatorHandler.addMineral("Silt", 25, .05f, new String[]{"blockClay", "sand", "gravel"}, new float[]{.5f, .3f, .2f});

		/*MULTIBLOCK RECIPES*/
		CokeOvenRecipe.addRecipe(new ItemStack(itemMaterial, 1, 6), new ItemStack(Items.COAL), 1800, 500);
		CokeOvenRecipe.addRecipe(new ItemStack(blockStoneDecoration, 1, 3), "blockCoal", 1800*9, 5000);
		CokeOvenRecipe.addRecipe(new ItemStack(Items.COAL, 1, 1), "logWood", 900, 250);

		IERecipes.initBlastFurnaceRecipes();

		IERecipes.initMetalPressRecipes();

		IERecipes.initAlloySmeltingRecipes();

		IERecipes.initCrusherRecipes();

		IERecipes.initArcSmeltingRecipes();

		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 80), ItemStack.EMPTY, Items.WHEAT_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 60), ItemStack.EMPTY, Items.BEETROOT_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 40), ItemStack.EMPTY, Items.PUMPKIN_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 20), ItemStack.EMPTY, Items.MELON_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 120), ItemStack.EMPTY, itemSeeds, 6400);
		SqueezerRecipe.addRecipe(null, new ItemStack(itemMaterial, 1, 18), new ItemStack(itemMaterial, 8, 17), 19200);
		Fluid fluidBlood = FluidRegistry.getFluid("blood");
		if(fluidBlood!=null)
			SqueezerRecipe.addRecipe(new FluidStack(fluidBlood, 5), new ItemStack(Items.LEATHER), new ItemStack(Items.ROTTEN_FLESH), 6400);

		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.REEDS, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.MELON, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.APPLE, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, "cropPotato", 6400);

		RefineryRecipe.addRecipe(new FluidStack(fluidBiodiesel, 16), new FluidStack(fluidPlantoil, 8), new FluidStack(fluidEthanol, 8), 80);

		MixerRecipe.addRecipe(new FluidStack(fluidConcrete, 500), new FluidStack(FluidRegistry.WATER, 500), new Object[]{"sand", "sand", Items.CLAY_BALL, "gravel"}, 3200);

		BottlingMachineRecipe.addRecipe(new ItemStack(Blocks.SPONGE, 1, 1), new ItemStack(Blocks.SPONGE, 1, 0), new FluidStack(FluidRegistry.WATER, 1000));

		IECompatModule.doModulesRecipes();

		/*ORE DICT CRAWLING*/
		IERecipes.postInitOreDictRecipes();
	}

	private static ResourceLocation createRegistryName(String unlocalized)
	{
		unlocalized = unlocalized.substring(unlocalized.indexOf("immersive"));
		unlocalized = unlocalized.replaceFirst("\\.", ":");
		return new ResourceLocation(unlocalized);
	}

	public static Fluid setupFluid(Fluid fluid)
	{
		FluidRegistry.addBucketForFluid(fluid);
		if(!FluidRegistry.registerFluid(fluid))
			return FluidRegistry.getFluid(fluid.getName());
		return fluid;
	}

	public static void preInit()
	{
		WireType.init();
		/*CONVEYORS*/
		ConveyorHandler.registerMagnetSupression((entity, iConveyorTile) -> {
			CompoundNBT data = entity.getEntityData();
			if(!data.getBoolean(Lib.MAGNET_PREVENT_NBT))
				data.putBoolean(Lib.MAGNET_PREVENT_NBT, true);
		}, (entity, iConveyorTile) -> {
			entity.getEntityData().remove(Lib.MAGNET_PREVENT_NBT);
		});
		ConveyorHandler.registerConveyorHandler(BasicConveyor.NAME, BasicConveyor.class, (tileEntity) -> new BasicConveyor());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "uncontrolled"), UncontrolledConveyor.class, (tileEntity) -> new UncontrolledConveyor());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "dropper"), DropConveyor.class, (tileEntity) -> new DropConveyor());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "vertical"), VerticalConveyor.class, (tileEntity) -> new VerticalConveyor());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "splitter"), SplitConveyor.class, (tileEntity) -> new SplitConveyor(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): Direction.NORTH));
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "extract"), ExtractConveyor.class, (tileEntity) -> new ExtractConveyor(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): Direction.NORTH));
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "covered"), CoveredConveyor.class, (tileEntity) -> new CoveredConveyor());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "droppercovered"), DropCoveredConveyor.class, (tileEntity) -> new DropCoveredConveyor());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "verticalcovered"), VerticalCoveredConveyor.class, (tileEntity) -> new VerticalCoveredConveyor());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "extractcovered"), ExtractCoveredConveyor.class, (tileEntity) -> new ExtractCoveredConveyor(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): Direction.NORTH));
		ConveyorHandler.registerSubstitute(new ResourceLocation(MODID, "conveyor"), new ResourceLocation(MODID, "uncontrolled"));

		/*BULLETS*/
		ItemBullet.initBullets();

		DataSerializers.registerSerializer(IEFluid.OPTIONAL_FLUID_STACK);

		IELootFunctions.preInit();
	}

	public static void preInitEnd()
	{
		/*WOLFPACK BULLETS*/
		if(!BulletHandler.homingCartridges.isEmpty())
		{
			BulletHandler.registerBullet("wolfpack", new WolfpackBullet());
			BulletHandler.registerBullet("wolfpackPart", new WolfpackPartBullet());
		}
	}

	public static void registerOres()
	{
		/*ORE DICTIONARY*/
		registerToOreDict("ore", blockOre);
		registerToOreDict("block", blockStorage);
		registerToOreDict("slab", blockStorageSlabs);
		registerToOreDict("blockSheetmetal", blockSheetmetal);
		registerToOreDict("slabSheetmetal", blockSheetmetalSlabs);
		registerToOreDict("", itemMetal);
		OreDictionary.registerOre("stickTreatedWood", new ItemStack(itemMaterial, 1, 0));
		OreDictionary.registerOre("stickIron", new ItemStack(itemMaterial, 1, 1));
		OreDictionary.registerOre("stickSteel", new ItemStack(itemMaterial, 1, 2));
		OreDictionary.registerOre("stickAluminum", new ItemStack(itemMaterial, 1, 3));
		OreDictionary.registerOre("fiberHemp", new ItemStack(itemMaterial, 1, 4));
		OreDictionary.registerOre("fabricHemp", new ItemStack(itemMaterial, 1, 5));
		OreDictionary.registerOre("fuelCoke", new ItemStack(itemMaterial, 1, 6));
		OreDictionary.registerOre("itemSlag", new ItemStack(itemMaterial, 1, 7));
		OreDictionary.registerOre("dustCoke", new ItemStack(itemMaterial, 1, 17));
		OreDictionary.registerOre("dustHOPGraphite", new ItemStack(itemMaterial, 1, 18));
		OreDictionary.registerOre("ingotHOPGraphite", new ItemStack(itemMaterial, 1, 19));
		OreDictionary.registerOre("wireCopper", new ItemStack(itemMaterial, 1, 20));
		OreDictionary.registerOre("wireElectrum", new ItemStack(itemMaterial, 1, 21));
		OreDictionary.registerOre("wireAluminum", new ItemStack(itemMaterial, 1, 22));
		OreDictionary.registerOre("wireSteel", new ItemStack(itemMaterial, 1, 23));
		OreDictionary.registerOre("dustSaltpeter", new ItemStack(itemMaterial, 1, 24));
		OreDictionary.registerOre("dustSulfur", new ItemStack(itemMaterial, 1, 25));
		OreDictionary.registerOre("electronTube", new ItemStack(itemMaterial, 1, 26));

		OreDictionary.registerOre("plankTreatedWood", new ItemStack(blockTreatedWood, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("slabTreatedWood", new ItemStack(blockTreatedWoodSlabs, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("fenceTreatedWood", new ItemStack(blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta()));
		OreDictionary.registerOre("scaffoldingTreatedWood", new ItemStack(blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta()));
		OreDictionary.registerOre("blockFuelCoke", new ItemStack(blockStoneDecoration, 1, BlockTypes_StoneDecoration.COKE.getMeta()));
		OreDictionary.registerOre("concrete", new ItemStack(blockStoneDecoration, 1, BlockTypes_StoneDecoration.CONCRETE.getMeta()));
		OreDictionary.registerOre("concrete", new ItemStack(blockStoneDecoration, 1, BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()));
		OreDictionary.registerOre("fenceSteel", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta()));
		OreDictionary.registerOre("fenceAluminum", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta()));
		//Vanilla OreDict
		OreDictionary.registerOre("blockClay", new ItemStack(Blocks.CLAY));
		OreDictionary.registerOre("bricksStone", new ItemStack(Blocks.STONEBRICK));
		OreDictionary.registerOre("blockIce", new ItemStack(Blocks.ICE));
		OreDictionary.registerOre("blockPackedIce", new ItemStack(Blocks.PACKED_ICE));
		OreDictionary.registerOre("craftingTableWood", new ItemStack(Blocks.CRAFTING_TABLE));
		OreDictionary.registerOre("rodBlaze", new ItemStack(Items.BLAZE_ROD));
		OreDictionary.registerOre("charcoal", new ItemStack(Items.COAL, 1, 1));
	}

	private static ArcRecyclingThreadHandler arcRecycleThread;

	public static void init()
	{

		/*ARC FURNACE RECYCLING*/
		if(IEConfig.MACHINES.arcfurnace_recycle)
		{
			arcRecycleThread = new ArcRecyclingThreadHandler();
			arcRecycleThread.start();
		}

		/*MINING LEVELS*/
		blockOre.setHarvestLevel("pickaxe", 1, blockOre.getStateFromMeta(BlockTypes_Ore.COPPER.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 1, blockOre.getStateFromMeta(BlockTypes_Ore.ALUMINUM.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 2, blockOre.getStateFromMeta(BlockTypes_Ore.LEAD.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 2, blockOre.getStateFromMeta(BlockTypes_Ore.SILVER.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 2, blockOre.getStateFromMeta(BlockTypes_Ore.NICKEL.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 2, blockOre.getStateFromMeta(BlockTypes_Ore.URANIUM.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 1, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.COPPER.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 1, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.ALUMINUM.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.LEAD.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.SILVER.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.NICKEL.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.URANIUM.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.CONSTANTAN.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.ELECTRUM.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.STEEL.getMeta()));

		/*WORLDGEN*/
		addConfiguredWorldgen(blockOre.getStateFromMeta(0), "copper", IEConfig.Ores.ore_copper);
		addConfiguredWorldgen(blockOre.getStateFromMeta(1), "bauxite", IEConfig.Ores.ore_bauxite);
		addConfiguredWorldgen(blockOre.getStateFromMeta(2), "lead", IEConfig.Ores.ore_lead);
		addConfiguredWorldgen(blockOre.getStateFromMeta(3), "silver", IEConfig.Ores.ore_silver);
		addConfiguredWorldgen(blockOre.getStateFromMeta(4), "nickel", IEConfig.Ores.ore_nickel);
		addConfiguredWorldgen(blockOre.getStateFromMeta(5), "uranium", IEConfig.Ores.ore_uranium);

		/*ENTITIES*/
		int i = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "revolverShot"), RevolvershotEntity.class, "revolverShot", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "skylineHook"), SkylineHookEntity.class, "skylineHook", i++, ImmersiveEngineering.instance, 64, 1, true);
		//EntityRegistry.registerModEntity(EntitySkycrate.class, "skylineCrate", 2, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "revolverShotHoming"), RevolvershotHomingEntity.class, "revolverShotHoming", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "revolverShotWolfpack"), WolfpackShotEntity.class, "revolverShotWolfpack", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "chemthrowerShot"), ChemthrowerShotEntity.class, "chemthrowerShot", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "railgunShot"), RailgunShotEntity.class, "railgunShot", i++, ImmersiveEngineering.instance, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "revolverShotFlare"), RevolvershotFlareEntity.class, "revolverShotFlare", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "explosive"), IEExplosiveEntity.class, "explosive", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "fluorescentTube"), FluorescentTubeEntity.class, "fluorescentTube", i++, ImmersiveEngineering.instance, 64, 1, true);
		CapabilityShader.register();
		NetHandlerCapability.register();
		CapabilitySkyhookData.register();
		ShaderRegistry.itemShader = IEContent.itemShader;
		ShaderRegistry.itemShaderBag = IEContent.itemShaderBag;
		ShaderRegistry.itemExamples.add(new ItemStack(IEContent.itemRevolver));
		ShaderRegistry.itemExamples.add(new ItemStack(IEContent.itemDrill));
		ShaderRegistry.itemExamples.add(new ItemStack(IEContent.itemChemthrower));
		ShaderRegistry.itemExamples.add(new ItemStack(IEContent.itemRailgun));

		/*SMELTING*/
		itemMaterial.setBurnTime(6, 3200);
		Item itemBlockStoneDecoration = Item.getItemFromBlock(blockStoneDecoration);
		if(itemBlockStoneDecoration instanceof ItemBlockIEBase)
			((ItemBlockIEBase)itemBlockStoneDecoration).setBurnTime(3, 3200*10);

		/*BANNERS*/
		addBanner("hammer", "hmr", new ItemStack(itemTool, 1, 0));
		addBanner("bevels", "bvl", "plateIron");
		addBanner("ornate", "orn", "dustSilver");
		addBanner("treated_wood", "twd", "plankTreatedWood");
		addBanner("windmill", "wnd", new ItemStack[]{new ItemStack(blockWoodenDevice1, 1, BlockTypes_WoodenDevice1.WINDMILL.getMeta())});
		if(!BulletHandler.homingCartridges.isEmpty())
		{
			ItemStack wolfpackCartridge = BulletHandler.getBulletStack("wolfpack");
			addBanner("wolf_r", "wlfr", wolfpackCartridge, 1);
			addBanner("wolf_l", "wlfl", wolfpackCartridge, -1);
			addBanner("wolf", "wlf", wolfpackCartridge, 0, 0);
		}

		/*ASSEMBLER RECIPE ADAPTERS*/
		//Fluid Ingredients
		AssemblerHandler.registerSpecialQueryConverters((o) ->
		{
			if(o instanceof IngredientFluidStack)
				return new RecipeQuery(((IngredientFluidStack)o).getFluid(), ((IngredientFluidStack)o).getFluid().amount);
			else return null;
		});

		DieselHandler.registerFuel(fluidBiodiesel, 125);
		DieselHandler.registerFuel(FluidRegistry.getFluid("fuel"), 375);
		DieselHandler.registerFuel(FluidRegistry.getFluid("diesel"), 175);
		DieselHandler.registerDrillFuel(fluidBiodiesel);
		DieselHandler.registerDrillFuel(FluidRegistry.getFluid("fuel"));
		DieselHandler.registerDrillFuel(FluidRegistry.getFluid("diesel"));

		blockFluidCreosote.setPotionEffects(new EffectInstance(IEPotions.flammable, 100, 0));
		blockFluidEthanol.setPotionEffects(new EffectInstance(Effects.NAUSEA, 40, 0));
		blockFluidBiodiesel.setPotionEffects(new EffectInstance(IEPotions.flammable, 100, 1));
		blockFluidConcrete.setPotionEffects(new EffectInstance(Effects.SLOWNESS, 20, 3, false, false));

		ChemthrowerHandler.registerEffect(FluidRegistry.WATER, new ChemthrowerEffect_Extinguish());

		ChemthrowerHandler.registerEffect(fluidPotion, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
			{
				if(fluid.tag!=null)
				{
					List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluid.tag);
					for(EffectInstance e : effects)
					{
						EffectInstance newEffect = new EffectInstance(e.getPotion(), (int)Math.ceil(e.getDuration()*.05), e.getAmplifier());
						newEffect.setCurativeItems(new ArrayList(e.getCurativeItems()));
						target.addPotionEffect(newEffect);
					}
				}
			}

			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
			{

			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}
		});

		ChemthrowerHandler.registerEffect(fluidConcrete, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
			{
				hit(target.world, target.getPosition(), Direction.UP);
			}

			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
			{
				BlockState hit = world.getBlockState(mop.getBlockPos());
				if(hit.getBlock()!=blockStoneDecoration||hit.getBlock().getMetaFromState(hit)!=BlockTypes_StoneDecoration.CONCRETE_SPRAYED.getMeta())
				{
					BlockPos pos = mop.getBlockPos().offset(mop.sideHit);
					if(!world.isAirBlock(pos))
						return;
					AxisAlignedBB aabb = new AxisAlignedBB(pos);
					List<ChemthrowerShotEntity> otherProjectiles = world.getEntitiesWithinAABB(ChemthrowerShotEntity.class, aabb);
					if(otherProjectiles.size() >= 8)
						hit(world, pos, mop.sideHit);
				}
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}

			private void hit(World world, BlockPos pos, Direction side)
			{
				AxisAlignedBB aabb = new AxisAlignedBB(pos);
				List<ChemthrowerShotEntity> otherProjectiles = world.getEntitiesWithinAABB(ChemthrowerShotEntity.class, aabb);
				for(ChemthrowerShotEntity shot : otherProjectiles)
					shot.setDead();
				world.setBlockState(pos, blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE_SPRAYED.getMeta()));
				for(LivingEntity living : world.getEntitiesWithinAABB(LivingEntity.class, aabb))
					living.addPotionEffect(new EffectInstance(IEPotions.concreteFeet, Integer.MAX_VALUE));
			}
		});

		ChemthrowerHandler.registerEffect(fluidCreosote, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 0));
		ChemthrowerHandler.registerFlammable(fluidCreosote);
		ChemthrowerHandler.registerEffect(fluidBiodiesel, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable(fluidBiodiesel);
		ChemthrowerHandler.registerFlammable(fluidEthanol);
		ChemthrowerHandler.registerEffect("oil", new ChemthrowerEffect_Potion(null, 0, new EffectInstance(IEPotions.flammable, 140, 0), new EffectInstance(Effects.BLINDNESS, 80, 1)));
		ChemthrowerHandler.registerFlammable("oil");
		ChemthrowerHandler.registerEffect("fuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 100, 1));
		ChemthrowerHandler.registerFlammable("fuel");
		ChemthrowerHandler.registerEffect("diesel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable("diesel");
		ChemthrowerHandler.registerEffect("kerosene", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 100, 1));
		ChemthrowerHandler.registerFlammable("kerosene");
		ChemthrowerHandler.registerEffect("biofuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable("biofuel");
		ChemthrowerHandler.registerEffect("rocket_fuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 2));
		ChemthrowerHandler.registerFlammable("rocket_fuel");

		RailgunHandler.registerProjectileProperties(new IngredientStack("stickIron"), 15, 1.25).setColourMap(new int[][]{{0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868}});
		RailgunHandler.registerProjectileProperties(new IngredientStack("stickAluminum"), 13, 1.05).setColourMap(new int[][]{{0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868}});
		RailgunHandler.registerProjectileProperties(new IngredientStack("stickSteel"), 18, 1.25).setColourMap(new int[][]{{0xb4b4b4, 0xb4b4b4, 0xb4b4b4, 0x7a7a7a, 0x555555, 0x555555}});
		RailgunHandler.registerProjectileProperties(new ItemStack(itemGraphiteElectrode), 24, .9).setColourMap(new int[][]{{0x242424, 0x242424, 0x242424, 0x171717, 0x171717, 0x0a0a0a}});

		ExternalHeaterHandler.defaultFurnaceEnergyCost = IEConfig.MACHINES.heater_consumption;
		ExternalHeaterHandler.defaultFurnaceSpeedupCost = IEConfig.MACHINES.heater_speedupConsumption;
		ExternalHeaterHandler.registerHeatableAdapter(FurnaceTileEntity.class, new DefaultFurnaceAdapter());

		BelljarHandler.DefaultPlantHandler hempBelljarHandler = new BelljarHandler.DefaultPlantHandler()
		{
			private HashSet<ComparableItemStack> validSeeds = new HashSet<>();

			@Override
			protected HashSet<ComparableItemStack> getSeedSet()
			{
				return validSeeds;
			}

			@Override
			@OnlyIn(Dist.CLIENT)
			public BlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				int age = Math.min(4, Math.round(growth*4));
				if(age==4)
					return new BlockState[]{blockCrop.getStateFromMeta(age), blockCrop.getStateFromMeta(age+1)};
				return new BlockState[]{blockCrop.getStateFromMeta(age)};
			}

			@Override
			@OnlyIn(Dist.CLIENT)
			public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				return .6875f;
			}
		};
		BelljarHandler.registerHandler(hempBelljarHandler);
		hempBelljarHandler.register(new ItemStack(itemSeeds), new ItemStack[]{new ItemStack(itemMaterial, 4, 4), new ItemStack(itemSeeds, 2)}, new ItemStack(Blocks.DIRT), blockCrop.getDefaultState());

		ThermoelectricHandler.registerSource(new IngredientStack(new ItemStack(Blocks.MAGMA)), 1300);
		ThermoelectricHandler.registerSourceInKelvin("blockIce", 273);
		ThermoelectricHandler.registerSourceInKelvin("blockPackedIce", 200);
		ThermoelectricHandler.registerSourceInKelvin("blockUranium", 2000);
		ThermoelectricHandler.registerSourceInKelvin("blockYellorium", 2000);
		ThermoelectricHandler.registerSourceInKelvin("blockPlutonium", 4000);
		ThermoelectricHandler.registerSourceInKelvin("blockBlutonium", 4000);

		/*MULTIBLOCKS*/
		MultiblockHandler.registerMultiblock(MultiblockCokeOven.instance);
		MultiblockHandler.registerMultiblock(MultiblockAlloySmelter.instance);
		MultiblockHandler.registerMultiblock(MultiblockBlastFurnace.instance);
		MultiblockHandler.registerMultiblock(MultiblockBlastFurnaceAdvanced.instance);
		MultiblockHandler.registerMultiblock(MultiblockMetalPress.instance);
		MultiblockHandler.registerMultiblock(MultiblockCrusher.instance);
		MultiblockHandler.registerMultiblock(MultiblockSheetmetalTank.instance);
		MultiblockHandler.registerMultiblock(MultiblockSilo.instance);
		MultiblockHandler.registerMultiblock(MultiblockAssembler.instance);
		MultiblockHandler.registerMultiblock(MultiblockAutoWorkbench.instance);
		MultiblockHandler.registerMultiblock(MultiblockBottlingMachine.instance);
		MultiblockHandler.registerMultiblock(MultiblockSqueezer.instance);
		MultiblockHandler.registerMultiblock(MultiblockFermenter.instance);
		MultiblockHandler.registerMultiblock(MultiblockRefinery.instance);
		MultiblockHandler.registerMultiblock(MultiblockDieselGenerator.instance);
		MultiblockHandler.registerMultiblock(MultiblockExcavator.instance);
		MultiblockHandler.registerMultiblock(MultiblockBucketWheel.instance);
		MultiblockHandler.registerMultiblock(MultiblockArcFurnace.instance);
		MultiblockHandler.registerMultiblock(MultiblockLightningrod.instance);
		MultiblockHandler.registerMultiblock(MultiblockMixer.instance);
		MultiblockHandler.registerMultiblock(MultiblockFeedthrough.instance);

		/*VILLAGE*/
		IEVillagerHandler.initIEVillagerHouse();
		IEVillagerHandler.initIEVillagerTrades();

		/*LOOT*/
		if(IEConfig.villagerHouse)
			LootTables.register(VillageEngineersHouse.woodenCrateLoot);
		for(ResourceLocation rl : EventHandler.lootInjections)
			LootTables.register(rl);

		//		//Railcraft Compat
		//		if(Loader.isModLoaded("Railcraft"))
		//		{
		//			Block rcCube = GameRegistry.findBlock("Railcraft", "cube");
		//			if(rcCube!=null)
		//				OreDictionary.registerOre("blockFuelCoke", new ItemStack(rcCube,1,0));
		//		}

		/*BLOCK ITEMS FROM CRATES*/
		IEApi.forbiddenInCrates.add((stack) -> {
			if(stack.getItem()==IEContent.itemToolbox)
				return true;
			if(stack.getItem()==IEContent.itemToolbox)
				return true;
			if(OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0, 1, 0), stack, true))
				return true;
			if(OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0, 1, 5), stack, true))
				return true;
			return stack.getItem() instanceof ItemShulkerBox;
		});

		FluidPipeTileEntity.initCovers();
		IEDataFixers.register();
		LocalNetworkHandler.register(EnergyTransferHandler.ID, EnergyTransferHandler.class);
		LocalNetworkHandler.register(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
		LocalNetworkHandler.register(WireDamageHandler.ID, WireDamageHandler.class);
	}

	public static void postInit()
	{
		/*POTIONS*/
		try
		{
			//Blame Forge for this mess. They stopped ATs from working on MixPredicate and its fields by modifying them with patches
			//without providing a usable way to look up the vanilla potion recipes
			String mixPredicateName = "net.minecraft.potion.PotionHelper$MixPredicate";
			Class<?> mixPredicateClass = Class.forName(mixPredicateName);
			Field output = ReflectionHelper.findField(mixPredicateClass,
					ObfuscationReflectionHelper.remapFieldNames(mixPredicateName, "field_185200_c"));
			Field reagent = ReflectionHelper.findField(mixPredicateClass,
					ObfuscationReflectionHelper.remapFieldNames(mixPredicateName, "field_185199_b"));
			Field input = ReflectionHelper.findField(mixPredicateClass,
					ObfuscationReflectionHelper.remapFieldNames(mixPredicateName, "field_185198_a"));
			output.setAccessible(true);
			reagent.setAccessible(true);
			input.setAccessible(true);
			for(Object mixPredicate : PotionHelper.POTION_TYPE_CONVERSIONS)
				//noinspection unchecked
				MixerRecipePotion.registerPotionRecipe(((IRegistryDelegate<Potion>)output.get(mixPredicate)).get(),
						((IRegistryDelegate<Potion>)input.get(mixPredicate)).get(),
						ApiUtils.createIngredientStack(reagent.get(mixPredicate)));
		} catch(Exception x)
		{
			x.printStackTrace();
		}
		for(IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes())
			if(recipe instanceof AbstractBrewingRecipe)
			{
				IngredientStack ingredientStack = ApiUtils.createIngredientStack(((AbstractBrewingRecipe)recipe).getIngredient());
				ItemStack input = ((AbstractBrewingRecipe)recipe).getInput();
				ItemStack output = ((AbstractBrewingRecipe)recipe).getOutput();
				if(input.getItem()==Items.POTIONITEM&&output.getItem()==Items.POTIONITEM)
					MixerRecipePotion.registerPotionRecipe(PotionUtils.getPotionFromItem(output), PotionUtils.getPotionFromItem(input), ingredientStack);
			}
		if(arcRecycleThread!=null)
		{
			try
			{
				arcRecycleThread.join();
				arcRecycleThread.finishUp();
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void refreshFluidReferences()
	{
		fluidCreosote = FluidRegistry.getFluid("creosote");
		fluidPlantoil = FluidRegistry.getFluid("plantoil");
		fluidEthanol = FluidRegistry.getFluid("ethanol");
		fluidBiodiesel = FluidRegistry.getFluid("biodiesel");
		fluidConcrete = FluidRegistry.getFluid("concrete");
		fluidPotion = FluidRegistry.getFluid("potion");
	}

	public static void registerToOreDict(String type, ItemIEBase item, int... metas)
	{
		if(metas==null||metas.length < 1)
		{
			for(int meta = 0; meta < item.getSubNames().length; meta++)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getSubNames()[meta];
					name = createOreDictName(name);
					if(type!=null&&!type.isEmpty())
						name = name.substring(0, 1).toUpperCase()+name.substring(1);
					OreDictionary.registerOre(type+name, new ItemStack(item, 1, meta));
				}
		}
		else
		{
			for(int meta : metas)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getSubNames()[meta];
					name = createOreDictName(name);
					if(type!=null&&!type.isEmpty())
						name = name.substring(0, 1).toUpperCase()+name.substring(1);
					OreDictionary.registerOre(type+name, new ItemStack(item, 1, meta));
				}
		}
	}

	private static String createOreDictName(String name)
	{
		String upperName = name.toUpperCase();
		StringBuilder sb = new StringBuilder();
		boolean nextCapital = false;
		for(int i = 0; i < name.length(); i++)
		{
			if(name.charAt(i)=='_')
			{
				nextCapital = true;
			}
			else
			{
				char nextChar = name.charAt(i);
				if(nextCapital)
				{
					nextChar = upperName.charAt(i);
					nextCapital = false;
				}
				sb.append(nextChar);
			}
		}
		return sb.toString();
	}

	public static void registerToOreDict(String type, IEBaseBlock item, int... metas)
	{
		if(metas==null||metas.length < 1)
		{
			for(int meta = 0; meta < item.getMetaEnums().length; meta++)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getMetaEnums()[meta].toString();
					if(type!=null&&!type.isEmpty())
						name = name.substring(0, 1).toUpperCase(Locale.ENGLISH)+name.substring(1).toLowerCase(Locale.ENGLISH);
					OreDictionary.registerOre(type+name, new ItemStack(item, 1, meta));
				}
		}
		else
		{
			for(int meta : metas)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getMetaEnums()[meta].toString();
					if(type!=null&&!type.isEmpty())
						name = name.substring(0, 1).toUpperCase(Locale.ENGLISH)+name.substring(1).toLowerCase(Locale.ENGLISH);
					OreDictionary.registerOre(type+name, new ItemStack(item, 1, meta));
				}
		}
	}

	public static void registerOre(String type, ItemStack ore, ItemStack ingot, ItemStack dust, ItemStack nugget, ItemStack plate, ItemStack block, ItemStack slab, ItemStack sheet, ItemStack slabSheet)
	{
		if(!ore.isEmpty())
			OreDictionary.registerOre("ore"+type, ore);
		if(!ingot.isEmpty())
			OreDictionary.registerOre("ingot"+type, ingot);
		if(!dust.isEmpty())
			OreDictionary.registerOre("dust"+type, dust);
		if(!nugget.isEmpty())
			OreDictionary.registerOre("nugget"+type, nugget);
		if(!plate.isEmpty())
			OreDictionary.registerOre("plate"+type, plate);
		if(!block.isEmpty())
			OreDictionary.registerOre("block"+type, block);
		if(!slab.isEmpty())
			OreDictionary.registerOre("slab"+type, slab);
		if(!sheet.isEmpty())
			OreDictionary.registerOre("blockSheetmetal"+type, sheet);
		if(!slabSheet.isEmpty())
			OreDictionary.registerOre("slabSheetmetal"+type, slabSheet);
	}

	public static <T extends TileEntity> void registerTile(Class<T> tile, Register<TileEntityType<?>> event, Block... valid)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		Set<Block> validSet = new HashSet<>(Arrays.asList(valid));
		TileEntityType<T> type = new TileEntityType<>(() -> {
			try
			{
				return (T)tile.newInstance();
			} catch(InstantiationException|IllegalAccessException e)
			{
				e.printStackTrace();
			}
			return null;
		}, validSet, null);//TODO where do I get a Type<T> from?
		type.setRegistryName(MODID, s);
		event.getRegistry().register(type);
		try
		{
			Field typeField = tile.getField("TYPE");
			typeField.set(null, type);
		} catch(NoSuchFieldException|IllegalAccessException e)
		{
			e.printStackTrace();
		}
		registeredIETiles.add(tile);
	}

	public static void addConfiguredWorldgen(BlockState state, String name, int[] config)
	{
		if(config!=null&&config.length >= 5&&config[0] > 0)
			IEWorldGen.addOreGen(name, state, config[0], config[1], config[2], config[3], config[4]);
	}

	public static void addBanner(String name, String id, Object item, int... offset)
	{
		name = MODID+"_"+name;
		id = "ie_"+id;
		ItemStack craftingStack = ItemStack.EMPTY;
		if(item instanceof ItemStack&&(offset==null||offset.length < 1))
			craftingStack = (ItemStack)item;
		BannerPattern e = EnumHelper.addEnum(BannerPattern.class, name.toUpperCase(), new Class[]{String.class, String.class, ItemStack.class}, name, id, craftingStack);
		if(craftingStack.isEmpty())
			RecipeBannerAdvanced.addAdvancedPatternRecipe(e, ApiUtils.createIngredientStack(item), offset);
	}
}
