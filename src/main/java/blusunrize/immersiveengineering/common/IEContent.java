package blusunrize.immersiveengineering.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.blocks.BlockFakeLight;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase.BlockIESimple;
import blusunrize.immersiveengineering.common.blocks.BlockIESlabs;
import blusunrize.immersiveengineering.common.blocks.BlockStorage;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.TileEntityIESlab;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDynamo;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityElectricLantern;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodLight;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFurnaceHeater;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLantern;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningRod;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySheetmetalTank;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySilo;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySkycrateDispenser;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityStructuralArm;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityThermoelectricGen;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityWallmountMetal;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBucketWheel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCokeOven;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCrusher;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockFermenter;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockLightningRod;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSheetmetalTank;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSilo;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSqueezer;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;
import blusunrize.immersiveengineering.common.blocks.stone.BlockStoneDecoration;
import blusunrize.immersiveengineering.common.blocks.stone.BlockStoneDevices;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockIEWoodenStairs;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockWoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockWoodenDevices;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallmount;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.crafting.IEFuelHandler;
import blusunrize.immersiveengineering.common.crafting.RecipeOreCrushing;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershotHoming;
import blusunrize.immersiveengineering.common.entities.EntitySkycrate;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import blusunrize.immersiveengineering.common.entities.EntityWolfpackShot;
import blusunrize.immersiveengineering.common.items.ItemBullet;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.items.ItemDrillhead;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.items.ItemGraphiteElectrode;
import blusunrize.immersiveengineering.common.items.ItemIEBase;
import blusunrize.immersiveengineering.common.items.ItemIESeed;
import blusunrize.immersiveengineering.common.items.ItemIETool;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import blusunrize.immersiveengineering.common.items.ItemToolUpgrade;
import blusunrize.immersiveengineering.common.items.ItemWireCoil;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.VillageEngineersHouse;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;

public class IEContent
{
	public static BlockIEBase blockOres;
	public static BlockIEBase blockStorage;
	public static BlockIEBase blockStorageSlabs;
	public static BlockIEBase blockMetalDevice;
	public static BlockIEBase blockMetalDevice2;
	public static BlockIEBase blockMetalDecoration;
	public static BlockIEBase blockMetalMultiblocks;
	public static BlockIEBase blockWoodenDevice;
	public static BlockIEBase blockWoodenDecoration;
	public static Block blockWoodenStair;
	public static BlockIEBase blockStoneDevice;
	public static BlockIEBase blockStoneDecoration;
	public static Block blockCrop;
	public static Block blockFakeLight;
	public static ItemIEBase itemMetal;
	public static ItemIEBase itemMaterial;
	public static ItemIEBase itemSeeds;
	public static ItemIEBase itemWireCoil;
	public static ItemIEBase itemTool;
	public static ItemIEBase itemRevolver;
	public static ItemIEBase itemBullet;
	public static ItemIEBase itemFluidContainers;
	public static ItemIEBase itemDrill;
	public static ItemIEBase itemDrillhead;
	public static ItemIEBase itemToolUpgrades;
	public static ItemIEBase itemSkyhook;
	public static ItemIEBase itemBlueprint;
	public static ItemIEBase itemGraphiteElectrode;
	public static Fluid fluidCreosote;
	public static boolean IECreosote=false;
	public static Fluid fluidPlantoil;
	public static boolean IEPlantoil=false;
	public static Fluid fluidEthanol;
	public static boolean IEEthanol=false;
	public static Fluid fluidBiodiesel;
	public static boolean IEBiodiesel=false;

	public static void preInit()
	{
		blockOres = (BlockIEBase) new BlockIESimple("ore",Material.rock,ItemBlockIEBase.class, "Copper","Aluminum","Lead","Silver","Nickel").setHardness(3f).setResistance(5f);
		blockStorage = (BlockIEBase) new BlockStorage("Copper","Aluminum","Lead","Silver","Nickel","Constantan","Electrum","Steel", "CoilCopper","CoilElectrum","CoilHV").setHardness(4f).setResistance(5f);
		blockStorageSlabs = (BlockIEBase) new BlockIESlabs("storageSlab","storage_",Material.iron,"Copper","Aluminum","Lead","Silver","Nickel","Constantan","Electrum","Steel").setHardness(4f).setResistance(5f);
		blockMetalDevice = new BlockMetalDevices();
		blockMetalDevice2 = new BlockMetalDevices2();
		blockMetalDecoration = new BlockMetalDecoration();
		blockMetalMultiblocks = new BlockMetalMultiblocks();
		blockWoodenDevice = new BlockWoodenDevices();
		blockWoodenDecoration = new BlockWoodenDecoration();
		blockWoodenStair = new BlockIEWoodenStairs();
		blockStoneDevice = new BlockStoneDevices();
		blockStoneDecoration = new BlockStoneDecoration();
		blockCrop = new BlockIECrop("hemp", "0B","1B","2B","3B","4B","0T");
		blockFakeLight = new BlockFakeLight();

		itemMetal = new ItemIEBase("metal", 64,
				"ingotCopper","ingotAluminum","ingotLead","ingotSilver","ingotNickel","ingotConstantan","ingotElectrum","ingotSteel",  
				"dustIron","dustGold","dustCopper","dustAluminum","dustLead","dustSilver","dustNickel","dustConstantan","dustElectrum",
				"dustCoke","dustQuartz","dustHOPGraphite","ingotHOPGraphite",
				"nuggetIron","nuggetCopper","nuggetAluminum","nuggetLead","nuggetSilver","nuggetNickel","nuggetConstantan","nuggetElectrum","nuggetSteel");

		itemMaterial = new ItemIEBase("material", 64,
				"treatedStick","waterwheelSegment","windmillBlade","hempFiber","fabric","windmillBladeAdvanced",
				"coalCoke",
				"gunpartBarrel","gunpartDrum","gunpartGrip","gunpartHammer",
				"componentIron","componentSteel","slag");

		itemSeeds = new ItemIESeed(blockCrop,"hemp");
		MinecraftForge.addGrassSeed(new ItemStack(itemSeeds), 5);
		itemWireCoil = new ItemWireCoil();
		WireType.ieWireCoil = itemWireCoil;
		itemTool = new ItemIETool();
		itemRevolver = new ItemRevolver();
		itemBullet = new ItemBullet();
		itemFluidContainers = new ItemIEBase("fluidContainers", 64, "bottleCreosote","bucketCreosote",  "bottlePlantoil","bucketPlantoil",  "bottleEthanol","bucketEthanol", "bottleBiodiesel","bucketBiodiesel")
		{
			@Override
			public boolean hasContainerItem(ItemStack stack)
			{
				return true;
			}
			@Override
			public ItemStack getContainerItem(ItemStack stack)
			{
				return stack.getItemDamage()%2==0?new ItemStack(Items.glass_bottle): new ItemStack(Items.bucket);
			}
			@Override
			public int getItemStackLimit(ItemStack stack)
			{
				return stack.getItemDamage()%2==0?16:1;
			}
		};
		itemDrill = new ItemDrill();
		itemDrillhead = new ItemDrillhead();
		itemToolUpgrades = new ItemToolUpgrade();
		itemSkyhook = new ItemSkyhook();
		itemBlueprint = new ItemEngineersBlueprint();
		itemGraphiteElectrode = new ItemGraphiteElectrode();

		fluidCreosote = FluidRegistry.getFluid("creosote");
		if(fluidCreosote==null)
		{
			fluidCreosote = new Fluid("creosote").setDensity(800).setViscosity(3000);
			FluidRegistry.registerFluid(fluidCreosote);
			IECreosote=true;
		}
		fluidPlantoil = FluidRegistry.getFluid("plantoil");
		if(fluidPlantoil==null)
		{
			fluidPlantoil = new Fluid("plantoil").setDensity(925).setViscosity(2000);
			FluidRegistry.registerFluid(fluidPlantoil);
			IEPlantoil=true;
		}
		fluidEthanol = FluidRegistry.getFluid("ethanol");
		if(fluidEthanol==null)
		{
			fluidEthanol = new Fluid("ethanol").setDensity(789).setViscosity(1000);
			FluidRegistry.registerFluid(fluidEthanol);
			IEEthanol=true;
		}
		fluidBiodiesel = FluidRegistry.getFluid("biodiesel");
		if(fluidBiodiesel==null)
		{
			fluidBiodiesel = new Fluid("biodiesel").setDensity(789).setViscosity(1000);
			FluidRegistry.registerFluid(fluidBiodiesel);
			IEBiodiesel=true;
		}

		//Ore Dict
		registerToOreDict("ore", blockOres);
		registerToOreDict("block", blockStorage, 0,1,2,3,4,5,6,7);
		registerToOreDict("", itemMetal);
		registerOre("Cupronickel",	null,new ItemStack(itemMetal,1,5),new ItemStack(itemMetal,1,15),new ItemStack(blockStorage,1,5),new ItemStack(itemMetal,1,27));

		OreDictionary.registerOre("seedIndustrialHemp", new ItemStack(itemSeeds));
		OreDictionary.registerOre("treatedStick", new ItemStack(itemMaterial,1,0));
		OreDictionary.registerOre("fuelCoke", new ItemStack(itemMaterial,1,6));
		OreDictionary.registerOre("blockFuelCoke", new ItemStack(blockStoneDevice,1,3));
		OreDictionary.registerOre("blockFuelCoke", new ItemStack(blockStoneDecoration,1,3));
		OreDictionary.registerOre("itemSlag", new ItemStack(itemMaterial,1,13));
		//Vanilla OreDict
		OreDictionary.registerOre("bricksStone", new ItemStack(Blocks.stonebrick));
		OreDictionary.registerOre("blockIce", new ItemStack(Blocks.ice));
		OreDictionary.registerOre("blockPackedIce", new ItemStack(Blocks.packed_ice));
		OreDictionary.registerOre("craftingTableWood", new ItemStack(Blocks.crafting_table));
		OreDictionary.registerOre("rodBlaze", new ItemStack(Items.blaze_rod));
		OreDictionary.registerOre("charcoal", new ItemStack(Items.coal,1,1));
		//Fluid Containers
		FluidContainerRegistry.registerFluidContainer(fluidCreosote, new ItemStack(itemFluidContainers,1,0), new ItemStack(Items.glass_bottle));
		FluidContainerRegistry.registerFluidContainer(fluidCreosote, new ItemStack(itemFluidContainers,1,1), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(fluidPlantoil, new ItemStack(itemFluidContainers,1,2), new ItemStack(Items.glass_bottle));
		FluidContainerRegistry.registerFluidContainer(fluidPlantoil, new ItemStack(itemFluidContainers,1,3), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(fluidEthanol, new ItemStack(itemFluidContainers,1,4), new ItemStack(Items.glass_bottle));
		FluidContainerRegistry.registerFluidContainer(fluidEthanol, new ItemStack(itemFluidContainers,1,5), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(fluidBiodiesel, new ItemStack(itemFluidContainers,1,6), new ItemStack(Items.glass_bottle));
		FluidContainerRegistry.registerFluidContainer(fluidBiodiesel, new ItemStack(itemFluidContainers,1,7), new ItemStack(Items.bucket));
		//Mining
		blockOres.setHarvestLevel("pickaxe", 1, 0);//Copper
		blockOres.setHarvestLevel("pickaxe", 1, 1);//Bauxite
		blockOres.setHarvestLevel("pickaxe", 2, 2);//Lead
		blockOres.setHarvestLevel("pickaxe", 2, 3);//Silver
		blockOres.setHarvestLevel("pickaxe", 2, 4);//Nickel
		blockStorage.setHarvestLevel("pickaxe", 1, 0);//Copper
		blockStorage.setHarvestLevel("pickaxe", 1, 1);//Aluminium
		blockStorage.setHarvestLevel("pickaxe", 2, 2);//Lead
		blockStorage.setHarvestLevel("pickaxe", 2, 3);//Silver
		blockStorage.setHarvestLevel("pickaxe", 2, 4);//Nickel
		blockStorage.setHarvestLevel("pickaxe", 2, 5);//Constantan
		blockStorage.setHarvestLevel("pickaxe", 2, 6);//Electrum
		blockStorage.setHarvestLevel("pickaxe", 2, 7);//Steel
		blockStorage.setHarvestLevel("pickaxe", 2, 8);//CoilCopper
		blockStorage.setHarvestLevel("pickaxe", 2, 9);//CoilElectrum
		blockStorage.setHarvestLevel("pickaxe", 2,10);//CoilHV

		addConfiguredWorldgen(blockOres,0, "ore_copper");
		addConfiguredWorldgen(blockOres,1, "ore_bauxite");
		addConfiguredWorldgen(blockOres,2, "ore_lead");
		addConfiguredWorldgen(blockOres,3, "ore_silver");
		addConfiguredWorldgen(blockOres,4, "ore_nickel");
	}

	public static void init()
	{
		/**TILEENTITIES*/
		registerTile(TileEntityIESlab.class);

		registerTile(TileEntityWoodenPost.class);
		registerTile(TileEntityWatermill.class);
		registerTile(TileEntityWindmill.class);
		registerTile(TileEntityWindmillAdvanced.class);
		registerTile(TileEntityWoodenCrate.class);
		registerTile(TileEntityModWorkbench.class);
		registerTile(TileEntityWoodenBarrel.class);
		registerTile(TileEntityWallmount.class);

		registerTile(TileEntityConnectorLV.class);
		registerTile(TileEntityCapacitorLV.class);
		registerTile(TileEntityConnectorMV.class);
		registerTile(TileEntityCapacitorMV.class);
		registerTile(TileEntityTransformer.class);
		registerTile(TileEntityRelayHV.class);
		registerTile(TileEntityConnectorHV.class);
		registerTile(TileEntityCapacitorHV.class);
		registerTile(TileEntityTransformerHV.class);
		registerTile(TileEntityDynamo.class);
		registerTile(TileEntityThermoelectricGen.class);
		registerTile(TileEntityConveyorBelt.class);
		registerTile(TileEntityFurnaceHeater.class);
		registerTile(TileEntityConveyorSorter.class);
		registerTile(TileEntitySampleDrill.class);

		registerTile(TileEntityLightningRod.class);
		registerTile(TileEntityDieselGenerator.class);
		registerTile(TileEntitySqueezer.class);
		registerTile(TileEntityFermenter.class);
		registerTile(TileEntityRefinery.class);
		registerTile(TileEntityCrusher.class);
		registerTile(TileEntityBucketWheel.class);
		registerTile(TileEntityExcavator.class);
		registerTile(TileEntityArcFurnace.class);
		registerTile(TileEntitySheetmetalTank.class);
		registerTile(TileEntitySilo.class);

		registerTile(TileEntityStructuralArm.class);
		registerTile(TileEntityConnectorStructural.class);
		registerTile(TileEntityWallmountMetal.class);
		registerTile(TileEntityLantern.class);

		registerTile(TileEntityBreakerSwitch.class);
		registerTile(TileEntitySkycrateDispenser.class);
		registerTile(TileEntityEnergyMeter.class);
		registerTile(TileEntityElectricLantern.class);
		registerTile(TileEntityFloodLight.class);


		registerTile(TileEntityCokeOven.class);
		registerTile(TileEntityBlastFurnace.class);

		/**ENTITIES*/
		EntityRegistry.registerModEntity(EntityRevolvershot.class, "revolverShot", 0, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntitySkylineHook.class, "skylineHook", 1, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntitySkycrate.class, "skylineCrate", 2, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityRevolvershotHoming.class, "revolverShotHoming", 3, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityWolfpackShot.class, "revolverShotWolfpack", 4, ImmersiveEngineering.instance, 64, 1, true);		
		int villagerId = Config.getInt("villager_engineer");
		VillagerRegistry.instance().registerVillagerId(villagerId);
		VillagerRegistry.instance().registerVillageCreationHandler(new VillageEngineersHouse.VillageManager());
		try{
			MapGenStructureIO.func_143031_a(VillageEngineersHouse.class, "IEVillageEngineersHouse");
		}catch (Exception e){
			IELogger.error("Engineer's House not added to Villages");
		}

		/**SMELTING*/
		IERecipes.initFurnaceRecipes();
		
		/**CRAFTING*/
		IERecipes.initCraftingRecipes();

		CokeOvenRecipe.addRecipe(new ItemStack(itemMaterial,1,6), new ItemStack(Items.coal), 1800, 500);
		CokeOvenRecipe.addRecipe(new ItemStack(blockStoneDecoration,1,3), "blockCoal", 1800*9, 5000);
		CokeOvenRecipe.addRecipe(new ItemStack(Items.coal,1,1), "logWood", 900, 250);
		BlastFurnaceRecipe.addRecipe(new ItemStack(itemMetal,1,7), "ingotIron", 1200);
		BlastFurnaceRecipe.addRecipe(new ItemStack(blockStorage,1,7), "blockIron", 1200*9);

		BlastFurnaceRecipe.addBlastFuel("fuelCoke", 1200);
		BlastFurnaceRecipe.addBlastFuel("blockFuelCoke", 1200*10);
		BlastFurnaceRecipe.addBlastFuel("charcoal", 300);
		BlastFurnaceRecipe.addBlastFuel("blockCharcoal", 300*10);
		GameRegistry.registerFuelHandler(new IEFuelHandler());

		IERecipes.initCrusherRecipes();
		
		IERecipes.initArcSmeltingRecipes();

		DieselHandler.registerFuel(fluidBiodiesel, 125);
		DieselHandler.registerFuel(FluidRegistry.getFluid("fuel"), 375);
		DieselHandler.registerFuel(FluidRegistry.getFluid("diesel"), 175);

		DieselHandler.addSqueezerRecipe(new ItemStack(itemMetal,8,17), 240, null, new ItemStack(itemMetal,1,19));
		DieselHandler.addSqueezerRecipe(Items.wheat_seeds, 80, new FluidStack(fluidPlantoil, 80), null);
		DieselHandler.addSqueezerRecipe(Items.pumpkin_seeds, 80, new FluidStack(fluidPlantoil, 80), null);
		DieselHandler.addSqueezerRecipe(Items.melon_seeds, 80, new FluidStack(fluidPlantoil, 80), null);
		DieselHandler.addSqueezerRecipe(itemSeeds, 80, new FluidStack(fluidPlantoil, 120), null);

		DieselHandler.addFermenterRecipe(Items.reeds, 80, new FluidStack(fluidEthanol,80), null);
		DieselHandler.addFermenterRecipe(Items.melon, 80, new FluidStack(fluidEthanol,80), null);
		DieselHandler.addFermenterRecipe(Items.apple, 80, new FluidStack(fluidEthanol,80), null);
		DieselHandler.addFermenterRecipe("cropPotato", 80, new FluidStack(fluidEthanol,80), null);

		DieselHandler.addRefineryRecipe(new FluidStack(fluidPlantoil,8), new FluidStack(fluidEthanol,8), new FluidStack(fluidBiodiesel,16));

		ThermoelectricHandler.registerSourceInKelvin("blockIce", 273);
		ThermoelectricHandler.registerSourceInKelvin("blockPackedIce", 200);
		ThermoelectricHandler.registerSourceInKelvin("blockPlutonium", 4000);
		ThermoelectricHandler.registerSourceInKelvin("blockBlutonium", 4000);
		ThermoelectricHandler.registerSourceInKelvin("blockUranium", 2000);
		ThermoelectricHandler.registerSourceInKelvin("blockYellorium", 2000);

		ExcavatorHandler.mineralVeinCapacity = Config.getInt("excavator_depletion");
		ExcavatorHandler.addMineral("Iron", 25, .1f, new String[]{"oreIron","oreNickel","oreTin","denseoreIron"}, new float[]{.5f,.25f,.20f,.05f});
		ExcavatorHandler.addMineral("Magnetite", 25, .1f, new String[]{"oreIron","oreGold"}, new float[]{.85f,.15f});
		ExcavatorHandler.addMineral("Pyrite", 20, .1f, new String[]{"oreIron","oreSulfur"}, new float[]{.5f,.5f});
		ExcavatorHandler.addMineral("Bauxite", 20, .2f, new String[]{"oreAluminum","oreTitanium","denseoreAluminum"}, new float[]{.90f,.05f,.05f});
		ExcavatorHandler.addMineral("Copper", 30, .2f, new String[]{"oreCopper","oreGold","oreNickel","denseoreCopper"}, new float[]{.65f,.25f,.05f,.05f});
		ExcavatorHandler.addMineral("Gold", 20, .3f, new String[]{"oreGold","oreCopper","oreNickel","denseoreGold"}, new float[]{.65f,.25f,.05f,.05f});
		ExcavatorHandler.addMineral("Nickel", 20, .3f, new String[]{"oreNickel","orePlatinum","oreIron","denseoreNickel"}, new float[]{.85f,.05f,.05f,.05f});
		ExcavatorHandler.addMineral("Platinum", 5, .35f, new String[]{"orePlatinum","oreNickel","oreIridium","denseorePlatinum"}, new float[]{.45f,.35f,.1f,.05f});
		ExcavatorHandler.addMineral("Uranium", 10, .35f, new String[]{"oreUranium","oreLead","orePlutonium","denseoreUranium"}, new float[]{.55f,.3f,.1f,.05f}).addReplacement("oreUranium", "oreYellorium");
		ExcavatorHandler.addMineral("Quartzite", 5, .3f, new String[]{"oreQuartz","oreCertusQuartz"}, new float[]{.6f,.4f});
		ExcavatorHandler.addMineral("Galena", 15, .2f, new String[]{"oreLead","oreSilver","oreSulfur","denseoreLead","denseoreSilver"}, new float[]{.40f,.40f,.1f,.05f,.05f});
		ExcavatorHandler.addMineral("Lead", 10, .15f, new String[]{"oreLead","oreSilver","denseoreLead"}, new float[]{.55f,.4f,.05f});
		ExcavatorHandler.addMineral("Silver", 10, .2f, new String[]{"oreSilver","oreLead","denseoreSilver"}, new float[]{.55f,.4f,.05f});
		ExcavatorHandler.addMineral("Lapis", 10, .2f, new String[]{"oreLapis","oreIron","oreSulfur","denseoreLapis"}, new float[]{.65f,.275f,.025f,.05f});
		ExcavatorHandler.addMineral("Coal", 25, .1f, new String[]{"oreCoal","denseoreCoal","oreDiamond","oreEmerald"}, new float[]{.92f,.1f,.015f,.015f});


		MultiblockHandler.registerMultiblock(MultiblockCokeOven.instance);
		MultiblockHandler.registerMultiblock(MultiblockBlastFurnace.instance);
		MultiblockHandler.registerMultiblock(MultiblockDieselGenerator.instance);
		MultiblockHandler.registerMultiblock(MultiblockSqueezer.instance);
		MultiblockHandler.registerMultiblock(MultiblockFermenter.instance);
		MultiblockHandler.registerMultiblock(MultiblockRefinery.instance);
		MultiblockHandler.registerMultiblock(MultiblockCrusher.instance);
		MultiblockHandler.registerMultiblock(MultiblockLightningRod.instance);
		MultiblockHandler.registerMultiblock(MultiblockExcavator.instance);
		MultiblockHandler.registerMultiblock(MultiblockBucketWheel.instance);
		MultiblockHandler.registerMultiblock(MultiblockArcFurnace.instance);
		MultiblockHandler.registerMultiblock(MultiblockSheetmetalTank.instance);
		MultiblockHandler.registerMultiblock(MultiblockSilo.instance);

		IEAchievements.init();
		
		//Railcraft Compat
		if(Loader.isModLoaded("Railcraft"))
		{
			Block rcCube = GameRegistry.findBlock("Railcraft", "cube");
			if(rcCube!=null)
				OreDictionary.registerOre("blockFuelCoke", new ItemStack(rcCube,1,0));
		}
	}

	public static void loadComplete()
	{
		//Crushing
		if(!Config.getBoolean("disableHammerCrushing") || Config.getBoolean("forceHammerCrushing"))
		{
			addHammerCrushingRecipe("Iron",8);
			addHammerCrushingRecipe("Gold",9);
			addHammerCrushingRecipe("Copper",10);
			addHammerCrushingRecipe("Aluminum",11);
			addHammerCrushingRecipe("Lead",12);
			addHammerCrushingRecipe("Silver",13);
			addHammerCrushingRecipe("Nickel",14);
			Config.setBoolean("crushingOreRecipe", !validCrushingOres.isEmpty());
		}

		//Villager Trades
		//These are done so late to account for Blueprints added by addons
		int villagerId = Config.getInt("villager_engineer");
		VillagerRegistry.instance().registerVillageTradeHandler(villagerId, new IEVillagerTradeHandler());
	}


	public static void registerToOreDict(String type, ItemIEBase item, int... metas)
	{
		if(metas==null||metas.length<1)
			for(int meta=0; meta<item.subNames.length; meta++)
				OreDictionary.registerOre(type+item.subNames[meta], new ItemStack(item,1,meta));
		else
			for(int meta: metas)
				OreDictionary.registerOre(type+item.subNames[meta], new ItemStack(item,1,meta));
	}
	public static void registerToOreDict(String type, BlockIEBase item, int... metas)
	{
		if(metas==null||metas.length<1)
			for(int meta=0; meta<item.subNames.length; meta++)
				OreDictionary.registerOre(type+item.subNames[meta], new ItemStack(item,1,meta));
		else
			for(int meta: metas)
				OreDictionary.registerOre(type+item.subNames[meta], new ItemStack(item,1,meta));
	}
	public static void registerOre(String type, ItemStack ore, ItemStack ingot, ItemStack dust, ItemStack block, ItemStack nugget)
	{
		if(ore!=null)
			OreDictionary.registerOre("ore"+type, ore);
		if(ingot!=null)
			OreDictionary.registerOre("ingot"+type, ingot);
		if(dust!=null)
			OreDictionary.registerOre("dust"+type, dust);
		if(block!=null)
			OreDictionary.registerOre("block"+type, block);
		if(nugget!=null)
			OreDictionary.registerOre("nugget"+type, nugget);
	}

	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersiveEngineering.MODID+":"+ s);
	}

	public static void addConfiguredWorldgen(Block block, int meta, String config)
	{
		int[] values = Config.getIntArray(config);
		if(values!=null && values.length>=5 && values[0]>0)
			IEWorldGen.addOreGen(block, meta, values[0],values[1],values[2], values[3],values[4]);
	}

	public static List<String> validCrushingOres = new ArrayList();
	public static void addHammerCrushingRecipe(String oreName, int dustMeta)
	{
		if(OreDictionary.getOres("dust"+oreName).size()<2 || Config.getBoolean("forceHammerCrushing"))
		{
			GameRegistry.addRecipe(new RecipeOreCrushing(oreName,dustMeta));
			validCrushingOres.add(oreName);
		}
	}
}