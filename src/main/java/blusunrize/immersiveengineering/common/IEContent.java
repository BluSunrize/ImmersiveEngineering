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
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.*;
import blusunrize.immersiveengineering.common.blocks.BlockFakeLight.TileEntityFakeLight;
import blusunrize.immersiveengineering.common.blocks.cloth.TileEntityBalloon;
import blusunrize.immersiveengineering.common.blocks.cloth.TileEntityShaderBanner;
import blusunrize.immersiveengineering.common.blocks.cloth.TileEntityStripCurtain;
import blusunrize.immersiveengineering.common.blocks.generic.BlockConnector;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPost;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.*;
import blusunrize.immersiveengineering.common.blocks.plant.BlockHemp;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
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

import javax.annotation.Nonnull;
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
			Block sheetmetal = new BlockIEBase("sheetmetal_"+name, sheetmetalProperties, ItemBlockIEBase.class)
					.setOpaque(true);
			addSlabFor(sheetmetal);
			if(!m.isVanillaMetal())
			{
				ore = new BlockIEBase("ore_"+m.name().toLowerCase(), Block.Properties.create(Material.ROCK)
						.hardnessAndResistance(3, 5), ItemBlockIEBase.class)
						.setOpaque(true);
				storage = new BlockIEBase("storage_"+m.name().toLowerCase(), storageProperties, ItemBlockIEBase.class)
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

		IEBlocks.StoneDecoration.cokebrick = new BlockIEBase("cokebrick", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.blastbrick = new BlockIEBase("blastbrick", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.blastbrickReinforced = new BlockIEBase("blastbrick_reinforced", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.coke = new BlockIEBase("coke", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.hempcrete = new BlockIEBase("hempcrete", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.concrete = new BlockIEBase("concrete", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.concreteTile = new BlockIEBase("concrete_tile", stoneDecoProps, ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.concreteLeaded = new BlockIEBase("concrete_leaded", Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 180), ItemBlockIEBase.class);
		IEBlocks.StoneDecoration.alloybrick = new BlockIEBase("alloybrick", stoneDecoProps, ItemBlockIEBase.class);

		IEBlocks.StoneDecoration.insulatingGlass = new BlockIEBase("insulating_glass", stoneDecoProps, ItemBlockIEBase.class)
		{
			@Override
			public int getOpacity(BlockState p_200011_1_, IBlockReader p_200011_2_, BlockPos p_200011_3_)
			{
				return 0;
			}
		}
				.setBlockLayer(BlockRenderLayer.TRANSLUCENT)
				.setNotNormalBlock();
		IEBlocks.StoneDecoration.concreteSprayed = new BlockIEBase("concrete_sprayed", Block.Properties.create(Material.ROCK).hardnessAndResistance(.2F, 1)., ItemBlockIEBase.class)
		{
			@Override
			public int getItemsToDropCount(@Nonnull BlockState state, int fortune, World world, BlockPos pos, @Nonnull Random r)
			{
				return 0;
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

		blockStoneDecorationSlabs = (BlockIEBase)new BlockIESlab("stone_decoration_slab", Material.ROCK, PropertyEnum.create("type", BlockTypes_StoneDecoration.class)).setMetaHidden(3, 8, sprConcreteMeta).setMetaExplosionResistance(BlockTypes_StoneDecoration.CONCRETE_LEADED.getMeta(), 180).setHardness(2.0F).setResistance(10.0F);
		blockStoneStair_hempcrete = new BlockIEStairs("stone_decoration_stairs_hempcrete", blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.HEMPCRETE.getMeta()));
		blockStoneStair_concrete0 = new BlockIEStairs("stone_decoration_stairs_concrete", blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE.getMeta()));
		blockStoneStair_concrete1 = new BlockIEStairs("stone_decoration_stairs_concrete_tile", blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()));
		blockStoneStair_concrete2 = new BlockIEStairs("stone_decoration_stairs_concrete_leaded", blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE_LEADED.getMeta())).setExplosionResistance(180f);

		blockStoneDevice = new BlockStoneDevice();

		blockTreatedWood = (BlockIEBase)new BlockIEBase("treated_wood", Material.WOOD, PropertyEnum.create("type", TreatedWoodStyles.class), ItemBlockIEBase.class).setOpaque(true).setHasFlavour().setHardness(2.0F).setResistance(5.0F);
		blockTreatedWoodSlabs = (BlockIESlab)new BlockIESlab("treated_wood_slab", Material.WOOD, PropertyEnum.create("type", TreatedWoodStyles.class)).setHasFlavour().setHardness(2.0F).setResistance(5.0F);
		blockWoodenStair = new BlockIEStairs("treated_wood_stairs0", blockTreatedWood.getStateFromMeta(0)).setHasFlavour(true);
		blockWoodenStair1 = new BlockIEStairs("treated_wood_stairs1", blockTreatedWood.getStateFromMeta(1)).setHasFlavour(true);
		blockWoodenStair2 = new BlockIEStairs("treated_wood_stairs2", blockTreatedWood.getStateFromMeta(2)).setHasFlavour(true);

		blockWoodenDecoration = new BlockWoodenDecoration();
		blockWoodenDevice0 = new BlockWoodenDevice0();
		blockWoodenDevice1 = new BlockWoodenDevice1().setMetaHidden(BlockTypes_WoodenDevice1.WINDMILL_ADVANCED.getMeta());
		blockCrop = new BlockHemp("hemp", PropertyEnum.create("type", EnumHempGrowth.class));
		blockClothDevice = new BlockClothDevice();
		blockFakeLight = new BlockFakeLight();


		blockMetalDecoration0 = new BlockMetalDecoration0();
		blockMetalDecoration1 = new BlockMetalDecoration1();
		blockMetalDecoration2 = new BlockMetalDecoration2();
		blockMetalDecorationSlabs1 = (BlockIESlab)new BlockIEScaffoldSlab("metal_decoration1_slab", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDecoration1.class)).setMetaHidden(0, 4).setHardness(3.0F).setResistance(15.0F);
		blockSteelScaffoldingStair = new BlockIEStairs("steel_scaffolding_stairs0", blockMetalDecoration1.getStateFromMeta(1)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockSteelScaffoldingStair1 = new BlockIEStairs("steel_scaffolding_stairs1", blockMetalDecoration1.getStateFromMeta(2)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockSteelScaffoldingStair2 = new BlockIEStairs("steel_scaffolding_stairs2", blockMetalDecoration1.getStateFromMeta(3)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockAluminumScaffoldingStair = new BlockIEStairs("aluminum_scaffolding_stairs0", blockMetalDecoration1.getStateFromMeta(5)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockAluminumScaffoldingStair1 = new BlockIEStairs("aluminum_scaffolding_stairs1", blockMetalDecoration1.getStateFromMeta(6)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockAluminumScaffoldingStair2 = new BlockIEStairs("aluminum_scaffolding_stairs2", blockMetalDecoration1.getStateFromMeta(7)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockMetalLadder = new BlockMetalLadder();

		blockConnectors = new BlockConnector();
		blockMetalDevice0 = new BlockMetalDevice0();
		blockMetalDevice1 = new BlockMetalDevice1();
		blockConveyor = new BlockConveyor();
		blockMetalMultiblock = new BlockMetalMultiblocks();

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
		if(Config.IEConfig.hempSeedWeight > 0)
			MinecraftForge.addGrassSeed(new ItemStack(itemSeeds), Config.IEConfig.hempSeedWeight);
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
		TileEntityEnergyConnector.registerConnectorTEs(event);
		ConveyorHandler.registerConveyorTEs(event);

		registerTile(TileEntityIESlab.class, event);

		registerTile(TileEntityBalloon.class, event);
		registerTile(TileEntityStripCurtain.class, event);
		registerTile(TileEntityShaderBanner.class, event);

		registerTile(TileEntityCokeOven.class, event);
		registerTile(TileEntityBlastFurnace.class, event);
		registerTile(TileEntityBlastFurnaceAdvanced.class, event);
		registerTile(TileEntityCoresample.class, event);
		registerTile(TileEntityAlloySmelter.class, event);

		registerTile(TileEntityWoodenCrate.class, event);
		registerTile(TileEntityWoodenBarrel.class, event);
		registerTile(TileEntityModWorkbench.class, event);
		registerTile(TileEntitySorter.class, event);
		registerTile(TileEntityTurntable.class, event);
		registerTile(TileEntityFluidSorter.class, event);
		registerTile(TileEntityWatermill.class, event);
		registerTile(TileEntityWindmill.class, event);
		registerTile(TileEntityPost.class, event);
		registerTile(TileEntityWallmount.class, event);

		registerTile(TileEntityLadder.class, event);
		registerTile(TileEntityLantern.class, event);
		registerTile(TileEntityRazorWire.class, event);
		registerTile(TileEntityToolbox.class, event);
		registerTile(TileEntityStructuralArm.class, event);

		registerTile(TileEntityConnectorStructural.class, event);
		registerTile(TileEntityTransformer.class, event);
		registerTile(TileEntityTransformerHV.class, event);
		registerTile(TileEntityBreakerSwitch.class, event);
		registerTile(TileEntityRedstoneBreaker.class, event);
		registerTile(TileEntityEnergyMeter.class, event);
		registerTile(TileEntityConnectorRedstone.class, event);
		registerTile(TileEntityConnectorProbe.class, event);
		registerTile(TileEntityFeedthrough.class, event);

		registerTile(TileEntityCapacitorLV.class, event);
		registerTile(TileEntityCapacitorMV.class, event);
		registerTile(TileEntityCapacitorHV.class, event);
		registerTile(TileEntityCapacitorCreative.class, event);
		registerTile(TileEntityMetalBarrel.class, event);
		registerTile(TileEntityFluidPump.class, event);
		registerTile(TileEntityFluidPlacer.class, event);

		registerTile(TileEntityBlastFurnacePreheater.class, event);
		registerTile(TileEntityFurnaceHeater.class, event);
		registerTile(TileEntityDynamo.class, event);
		registerTile(TileEntityThermoelectricGen.class, event);
		registerTile(TileEntityElectricLantern.class, event);
		registerTile(TileEntityChargingStation.class, event);
		registerTile(TileEntityFluidPipe.class, event);
		registerTile(TileEntitySampleDrill.class, event);
		registerTile(TileEntityTeslaCoil.class, event);
		registerTile(TileEntityFloodlight.class, event);
		registerTile(TileEntityTurret.class, event);
		registerTile(TileEntityTurretChem.class, event);
		registerTile(TileEntityTurretGun.class, event);
		registerTile(TileEntityBelljar.class, event);

		registerTile(TileEntityConveyorBelt.class, event);
		registerTile(TileEntityConveyorVertical.class, event);

		registerTile(TileEntityMetalPress.class, event);
		registerTile(TileEntityCrusher.class, event);
		registerTile(TileEntitySheetmetalTank.class, event);
		registerTile(TileEntitySilo.class, event);
		registerTile(TileEntityAssembler.class, event);
		registerTile(TileEntityAutoWorkbench.class, event);
		registerTile(TileEntityBottlingMachine.class, event);
		registerTile(TileEntitySqueezer.class, event);
		registerTile(TileEntityFermenter.class, event);
		registerTile(TileEntityRefinery.class, event);
		registerTile(TileEntityDieselGenerator.class, event);
		registerTile(TileEntityBucketWheel.class, event);
		registerTile(TileEntityExcavator.class, event);
		registerTile(TileEntityArcFurnace.class, event);
		registerTile(TileEntityLightningrod.class, event);
		registerTile(TileEntityMixer.class, event);
		//		registerTile(TileEntitySkycrateDispenser.class);
		registerTile(TileEntityFakeLight.class, event);
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
		ExcavatorHandler.mineralVeinCapacity = IEConfig.Machines.excavator_depletion;
		ExcavatorHandler.mineralChance = IEConfig.Machines.excavator_chance;
		ExcavatorHandler.defaultDimensionBlacklist = IEConfig.Machines.excavator_dimBlacklist;
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
			entity.getEntityData().removeTag(Lib.MAGNET_PREVENT_NBT);
		});
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "conveyor"), ConveyorBasic.class, (tileEntity) -> new ConveyorBasic());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "uncontrolled"), ConveyorUncontrolled.class, (tileEntity) -> new ConveyorUncontrolled());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "dropper"), ConveyorDrop.class, (tileEntity) -> new ConveyorDrop());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "vertical"), ConveyorVertical.class, (tileEntity) -> new ConveyorVertical());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "splitter"), ConveyorSplit.class, (tileEntity) -> new ConveyorSplit(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): Direction.NORTH));
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "extract"), ConveyorExtract.class, (tileEntity) -> new ConveyorExtract(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): Direction.NORTH));
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "covered"), ConveyorCovered.class, (tileEntity) -> new ConveyorCovered());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "droppercovered"), ConveyorDropCovered.class, (tileEntity) -> new ConveyorDropCovered());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "verticalcovered"), ConveyorVerticalCovered.class, (tileEntity) -> new ConveyorVerticalCovered());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(MODID, "extractcovered"), ConveyorExtractCovered.class, (tileEntity) -> new ConveyorExtractCovered(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): Direction.NORTH));
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
		if(IEConfig.Machines.arcfurnace_recycle)
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
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "revolverShot"), EntityRevolvershot.class, "revolverShot", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "skylineHook"), EntitySkylineHook.class, "skylineHook", i++, ImmersiveEngineering.instance, 64, 1, true);
		//EntityRegistry.registerModEntity(EntitySkycrate.class, "skylineCrate", 2, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "revolverShotHoming"), EntityRevolvershotHoming.class, "revolverShotHoming", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "revolverShotWolfpack"), EntityWolfpackShot.class, "revolverShotWolfpack", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "chemthrowerShot"), EntityChemthrowerShot.class, "chemthrowerShot", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "railgunShot"), EntityRailgunShot.class, "railgunShot", i++, ImmersiveEngineering.instance, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "revolverShotFlare"), EntityRevolvershotFlare.class, "revolverShotFlare", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "explosive"), EntityIEExplosive.class, "explosive", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "fluorescentTube"), EntityFluorescentTube.class, "fluorescentTube", i++, ImmersiveEngineering.instance, 64, 1, true);
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
					List<EntityChemthrowerShot> otherProjectiles = world.getEntitiesWithinAABB(EntityChemthrowerShot.class, aabb);
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
				List<EntityChemthrowerShot> otherProjectiles = world.getEntitiesWithinAABB(EntityChemthrowerShot.class, aabb);
				for(EntityChemthrowerShot shot : otherProjectiles)
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

		ExternalHeaterHandler.defaultFurnaceEnergyCost = IEConfig.Machines.heater_consumption;
		ExternalHeaterHandler.defaultFurnaceSpeedupCost = IEConfig.Machines.heater_speedupConsumption;
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

		TileEntityFluidPipe.initCovers();
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

	public static void registerToOreDict(String type, BlockIEBase item, int... metas)
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

	public static <T extends TileEntity> void registerTile(Class<T> tile, Register<TileEntityType<?>> event)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		TileEntityType<T> type = new TileEntityType<>(() -> {
			try
			{
				return (T)tile.newInstance();
			} catch(InstantiationException|IllegalAccessException e)
			{
				e.printStackTrace();
			}
			return null;
		}, null);
		type.setRegistryName(MODID, s);
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
