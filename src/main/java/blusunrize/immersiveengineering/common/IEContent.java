package blusunrize.immersiveengineering.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.api.tool.CrusherRecipe;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase.BlockIESimple;
import blusunrize.immersiveengineering.common.blocks.BlockStorage;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
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
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFurnaceHeater;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLantern;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningRod;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySkycrateDispenser;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityStructuralArm;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityThermoelectricGen;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityWallmountMetal;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBucketWheel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCokeOven;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCrusher;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockFermenter;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockLightningRod;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSqueezer;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;
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
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.crafting.IEFuelHandler;
import blusunrize.immersiveengineering.common.crafting.RecipeOreCrushing;
import blusunrize.immersiveengineering.common.crafting.RecipeRevolver;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.entities.EntitySkycrate;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import blusunrize.immersiveengineering.common.items.ItemBullet;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.items.ItemDrillhead;
import blusunrize.immersiveengineering.common.items.ItemIEBase;
import blusunrize.immersiveengineering.common.items.ItemIESeed;
import blusunrize.immersiveengineering.common.items.ItemIETool;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import blusunrize.immersiveengineering.common.items.ItemToolUpgrade;
import blusunrize.immersiveengineering.common.items.ItemWireCoil;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class IEContent
{
	public static BlockIEBase blockOres;
	public static BlockIEBase blockStorage;
	public static BlockIEBase blockMetalDevice;
	public static BlockIEBase blockMetalDevice2;
	public static BlockIEBase blockMetalDecoration;
	public static BlockIEBase blockMetalMultiblocks;
	public static BlockIEBase blockWoodenDevice;
	public static BlockIEBase blockWoodenDecoration;
	public static Block blockWoodenStair;
	public static BlockIEBase blockStoneDevice;
	public static Block blockCrop;
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
		blockMetalDevice = new BlockMetalDevices();
		blockMetalDevice2 = new BlockMetalDevices2();
		blockMetalDecoration = new BlockMetalDecoration();
		blockMetalMultiblocks = new BlockMetalMultiblocks();
		blockWoodenDevice = new BlockWoodenDevices();
		blockWoodenDecoration = new BlockWoodenDecoration();
		blockWoodenStair = new BlockIEWoodenStairs();
		blockStoneDevice = new BlockStoneDevices();
		blockCrop = new BlockIECrop("hemp", "0B","1B","2B","3B","4B","0T");

		itemMetal = new ItemIEBase("metal", 64,
				"ingotCopper","ingotAluminum","ingotLead","ingotSilver","ingotNickel","ingotConstantan","ingotElectrum","ingotSteel",  
				"dustIron","dustGold","dustCopper","dustAluminum","dustLead","dustSilver","dustNickel","dustConstantan","dustElectrum",
				"dustCoke","dustQuartz","dustHOPGraphite","ingotHOPGraphite");

		itemMaterial = new ItemIEBase("material", 64,
				"treatedStick","waterwheelSegment","windmillBlade","hempFiber","fabric","windmillBladeAdvanced",
				"coalCoke",
				"gunpartBarrel","gunpartDrum","gunpartGrip","gunpartHammer",
				"componentIron","componentSteel");

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
		registerOre("Cupronickel",	null,new ItemStack(itemMetal,1,5),new ItemStack(itemMetal,1,15),new ItemStack(blockStorage,1,5));

		OreDictionary.registerOre("seedIndustrialHemp", new ItemStack(itemSeeds));
		OreDictionary.registerOre("treatedStick", new ItemStack(itemMaterial,1,0));
		OreDictionary.registerOre("fuelCoke", new ItemStack(itemMaterial,1,6));
		OreDictionary.registerOre("blockFuelCoke", new ItemStack(blockStoneDevice,1,3));
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
		registerTile(TileEntityWoodenPost.class);
		registerTile(TileEntityWatermill.class);
		registerTile(TileEntityWindmill.class);
		registerTile(TileEntityWindmillAdvanced.class);
		registerTile(TileEntityWoodenCrate.class);
		registerTile(TileEntityModWorkbench.class);
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

		registerTile(TileEntityStructuralArm.class);
		registerTile(TileEntityConnectorStructural.class);
		registerTile(TileEntityWallmountMetal.class);
		registerTile(TileEntityLantern.class);
		
		registerTile(TileEntityBreakerSwitch.class);
		registerTile(TileEntitySkycrateDispenser.class);


		registerTile(TileEntityCokeOven.class);
		registerTile(TileEntityBlastFurnace.class);

		/**ENTITIES*/
		EntityRegistry.registerModEntity(EntityRevolvershot.class, "revolverShot", 0, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntitySkylineHook.class, "skylineHook", 1, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntitySkycrate.class, "skylineCrate", 2, ImmersiveEngineering.instance, 64, 1, true);


		/**SMELTING*/
		//Ores
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(blockOres,1,0), new ItemStack(itemMetal,1,0), 0.3F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(blockOres,1,1), new ItemStack(itemMetal,1,1), 0.3F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(blockOres,1,2), new ItemStack(itemMetal,1,2), 0.7F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(blockOres,1,3), new ItemStack(itemMetal,1,3), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(blockOres,1,4), new ItemStack(itemMetal,1,4), 1.0F);
		//Dusts
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,8), new ItemStack(Items.iron_ingot), 0.7F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,9), new ItemStack(Items.gold_ingot), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,10), new ItemStack(itemMetal,1,0), 0.3F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,11), new ItemStack(itemMetal,1,1), 0.3F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,12), new ItemStack(itemMetal,1,2), 0.7F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,13), new ItemStack(itemMetal,1,3), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,14), new ItemStack(itemMetal,1,4), 0.5F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,15), new ItemStack(itemMetal,1,5), 0.5F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(itemMetal,1,16), new ItemStack(itemMetal,1,6), 0.5F);


		/**CRAFTING*/
		ItemStack treatedWood = new ItemStack(blockWoodenDecoration,1,0);
		ItemStack copperCoil = new ItemStack(blockStorage,1,8);
		ItemStack electrumCoil = new ItemStack(blockStorage,1,9);
		ItemStack hvCoil = new ItemStack(blockStorage,1,10);
		ItemStack componentIron = new ItemStack(itemMaterial,1,11);
		ItemStack componentSteel = new ItemStack(itemMaterial,1,12);

		addOredictRecipe(new ItemStack(itemTool,1,0), " IF"," SI","S  ", 'I',"ingotIron", 'S',"stickWood", 'F',new ItemStack(Items.string));
		addOredictRecipe(new ItemStack(itemTool,1,1), "SI"," S", 'I',"ingotIron", 'S',"treatedStick").setMirrored(true);
		addOredictRecipe(new ItemStack(itemTool,1,2), " P ","SCS", 'C',"ingotCopper", 'P',Items.compass, 'S',"treatedStick");
		addShapelessOredictRecipe(new ItemStack(itemTool,1,3), Items.book,Blocks.lever);
		addOredictRecipe(new ItemStack(itemRevolver,1,0), " I ","HDB","GIG", 'I',"ingotIron",'B',new ItemStack(itemMaterial,1,7),'D',new ItemStack(itemMaterial,1,8),'G',new ItemStack(itemMaterial,1,9),'H',new ItemStack(itemMaterial,1,10)).setMirrored(false);
		addOredictRecipe(new ItemStack(itemRevolver,1,1), "  I","IIS","  I", 'I',"ingotIron",'S',"ingotSteel");
		GameRegistry.addRecipe(new RecipeRevolver());

		addOredictRecipe(new ItemStack(itemBullet,3,0), "I I","I I"," I ", 'I',"ingotCopper");
		addOredictRecipe(new ItemStack(itemBullet,3,1), "PDP","PDP"," I ", 'I',"ingotCopper",'P',Items.paper,'D',"dyeRed");
		addBulletRecipes(2, "ingotIron", 0);
		addBulletRecipes(3, "ingotSteel", 0);
		if(!OreDictionary.getOres("ingotTungsten").isEmpty())
			addBulletRecipes(3, "ingotTungsten", 0);
		if(!OreDictionary.getOres("ingotCyanite").isEmpty())
			addBulletRecipes(3, "ingotCyanite", 0);
		addBulletRecipes(4, "dustIron", 1);
		addBulletRecipes(5, Blocks.tnt, 0);
		addBulletRecipes(6, "dustAluminum", 1);
		
		addOredictRecipe(new ItemStack(itemSkyhook,1,0), "II ","IC "," GG", 'C',componentIron,'I',"ingotSteel", 'G',new ItemStack(itemMaterial,1,9));
		
		addOredictRecipe(new ItemStack(itemDrill,1,0), "  G"," EG","C  ", 'C',componentSteel,'E',new ItemStack(blockMetalDecoration,1,BlockMetalDecoration.META_heavyEngineering), 'G',new ItemStack(itemMaterial,1,9));
		addOredictRecipe(new ItemStack(itemDrillhead,1,0), "SS ","BBS","SS ", 'B',"blockSteel", 'S',"ingotSteel");
		addOredictRecipe(new ItemStack(itemDrillhead,1,1), "SS ","BBS","SS ", 'B',"blockIron", 'S',"ingotIron");
		addOredictRecipe(new ItemStack(itemToolUpgrades,1,0), "BI ","IBI"," IC", 'B',Items.bucket, 'I',"dyeBlue", 'C',componentIron);
		for (ItemStack container : Utils.getContainersFilledWith(new FluidStack(fluidPlantoil,1000)))
			addOredictRecipe(new ItemStack(itemToolUpgrades,1,1), "BI ","IBI"," IC", 'B',container, 'I',"ingotIron", 'C',componentIron);
		addOredictRecipe(new ItemStack(itemToolUpgrades,1,2), "SSS"," C ", 'S',"ingotSteel", 'C',componentSteel);
		addOredictRecipe(new ItemStack(itemToolUpgrades,1,3), "CS ","SBO"," OB", 'C',componentIron, 'S',"ingotSteel", 'B',Items.bucket, 'O',"dyeRed");
		addOredictRecipe(new ItemStack(itemToolUpgrades,1,4), "SI","IW", 'S',Items.iron_sword, 'I',"ingotSteel", 'W',treatedWood);
		addOredictRecipe(new ItemStack(itemToolUpgrades,1,5), " CS","C C","IC ", 'I',componentIron, 'S',"ingotSteel", 'C',"ingotCopper");
		addOredictRecipe(new ItemStack(itemToolUpgrades,1,6), " G ","GEG","GEG", 'E',electrumCoil, 'G',"blockGlass");

		addShapelessOredictRecipe(new ItemStack(itemMetal,2,15), "dustCopper","dustNickel");
		addShapelessOredictRecipe(new ItemStack(itemMetal,2,16), "dustSilver","dustGold");

		addOredictRecipe(new ItemStack(itemMaterial,4,0), "W","W", 'W',treatedWood);
		addOredictRecipe(new ItemStack(itemMaterial,1,1), " S ","SBS","BSB", 'B',treatedWood, 'S',"treatedStick");
		addOredictRecipe(new ItemStack(itemMaterial,1,2), "BB ","SSB","SS ", 'B',treatedWood, 'S',"treatedStick");
		addShapelessOredictRecipe(new ItemStack(Items.string), new ItemStack(itemMaterial,1,3));
		addOredictRecipe(new ItemStack(itemMaterial,1,4), "HHH","HSH","HHH", 'H',new ItemStack(itemMaterial,1,3), 'S',"stickWood");
		addShapelessOredictRecipe(new ItemStack(itemMaterial,1,5), new ItemStack(itemMaterial,1,2),new ItemStack(itemMaterial,1,4),new ItemStack(itemMaterial,1,4),new ItemStack(itemMaterial,1,4),new ItemStack(itemMaterial,1,4));
		addShapelessOredictRecipe(new ItemStack(itemMaterial,9,6), new ItemStack(blockStoneDevice,1,3));
		addOredictRecipe(new ItemStack(itemMaterial,1,7), "III", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(itemMaterial,1,8), " I ","ICI"," I ", 'I',"ingotSteel",'C',componentIron);
		addOredictRecipe(new ItemStack(itemMaterial,1,9), "SS","IS","SS", 'I',"ingotCopper",'S',"treatedStick");
		addOredictRecipe(new ItemStack(itemMaterial,1,10), "I  ","II "," II", 'I',"ingotSteel");
		addOredictRecipe(componentIron, "I I"," C ","I I", 'I',"ingotIron",'C',"ingotCopper");
		addOredictRecipe(componentSteel, "I I"," C ","I I", 'I',"ingotSteel",'C',"ingotCopper");

		addOredictRecipe(new ItemStack(itemWireCoil,4,0), " I ","ISI"," I ", 'I',"ingotCopper", 'S',"stickWood");
		addOredictRecipe(new ItemStack(itemWireCoil,4,1), " I ","ISI"," I ", 'I',"ingotElectrum", 'S',"stickWood");
		addOredictRecipe(new ItemStack(itemWireCoil,4,2), " I ","ASA"," I ", 'I',"ingotSteel", 'A',"ingotAluminum", 'S',"stickWood");
		addOredictRecipe(new ItemStack(itemWireCoil,4,3), " I ","ISI"," I ", 'I',new ItemStack(itemMaterial,1,3), 'S',"stickWood");
		addOredictRecipe(new ItemStack(itemWireCoil,4,4), " I ","ISI"," I ", 'I',"ingotSteel", 'S',"stickWood");

		for (ItemStack container : Utils.getContainersFilledWith(new FluidStack(fluidCreosote,1000)))
			addOredictRecipe(new ItemStack(blockWoodenDecoration,8,0), "WWW","WCW","WWW", 'W',"plankWood",'C',container);
		addOredictRecipe(new ItemStack(blockWoodenDecoration,2,1), "SSS","SSS", 'S',"treatedStick");
		addOredictRecipe(new ItemStack(blockWoodenDecoration,6,2), "WWW", 'W',treatedWood);
		addOredictRecipe(new ItemStack(blockWoodenDecoration,6,5), "WWW"," S ","S S", 'W',treatedWood,'S',new ItemStack(blockWoodenDecoration,1,1));
		addOredictRecipe(new ItemStack(blockWoodenStair,4,0), "  W"," WW","WWW", 'W',treatedWood).setMirrored(true);
		addOredictRecipe(new ItemStack(blockWoodenDecoration,4,6), "WW","WF","W ", 'W',treatedWood,'F',new ItemStack(blockWoodenDecoration,1,1));

		addOredictRecipe(new ItemStack(blockWoodenDevice,1,0), "F","F","S", 'F',new ItemStack(blockWoodenDecoration,1,1),'S',"bricksStone");
		addOredictRecipe(new ItemStack(blockWoodenDevice,1,1), " P ","PWP"," P ", 'P',new ItemStack(itemMaterial,1,1),'W',treatedWood);
		addOredictRecipe(new ItemStack(blockWoodenDevice,1,2), " P ","PIP"," P ", 'P',new ItemStack(itemMaterial,1,2),'I',"ingotIron");
		addOredictRecipe(new ItemStack(blockWoodenDevice,1,3), "PPP","PIP","PPP", 'P',new ItemStack(itemMaterial,1,5),'I',"ingotSteel");
		addOredictRecipe(new ItemStack(blockWoodenDevice,1,4), "WWW","W W","WWW", 'W',treatedWood);
		addOredictRecipe(new ItemStack(blockWoodenDevice,1,5), "WWW","B F", 'W',new ItemStack(blockWoodenDecoration,1,2),'B',"craftingTableWood",'F',new ItemStack(blockWoodenDecoration,1,1));

		addOredictRecipe(new ItemStack(blockStoneDevice,6,0), "CCC","HHH","CCC", 'C',Items.clay_ball,'H',new ItemStack(itemMaterial,1,3));
		addOredictRecipe(new ItemStack(blockStoneDevice,2,1), "CBC","BSB","CBC", 'S',"sandstone",'C',Items.clay_ball,'B',"ingotBrick");
		addOredictRecipe(new ItemStack(blockStoneDevice,2,2), "NBN","BDB","NBN", 'D',Items.blaze_powder,'N',"ingotBrickNether",'B',"ingotBrick");
		addOredictRecipe(new ItemStack(blockStoneDevice,1,3), "CCC","CCC","CCC", 'C',new ItemStack(itemMaterial,1,6));
		addOredictRecipe(new ItemStack(blockStoneDevice,2,4), " I ","GDG"," I ", 'G',"blockGlass",'I',"dustIron",'D',"dyeGreen");
		addOredictRecipe(new ItemStack(blockStoneDevice,2,4), " G ","IDI"," G ", 'G',"blockGlass",'I',"dustIron",'D',"dyeGreen");

		addOredictRecipe(new ItemStack(blockStorage,1,0), "III","III","III", 'I',"ingotCopper");
		addOredictRecipe(new ItemStack(blockStorage,1,1), "III","III","III", 'I',"ingotAluminum");
		addOredictRecipe(new ItemStack(blockStorage,1,2), "III","III","III", 'I',"ingotLead");
		addOredictRecipe(new ItemStack(blockStorage,1,3), "III","III","III", 'I',"ingotSilver");
		addOredictRecipe(new ItemStack(blockStorage,1,4), "III","III","III", 'I',"ingotNickel");
		addOredictRecipe(new ItemStack(blockStorage,1,5), "III","III","III", 'I',"ingotConstantan");
		addOredictRecipe(new ItemStack(blockStorage,1,6), "III","III","III", 'I',"ingotElectrum");
		addOredictRecipe(new ItemStack(blockStorage,1,7), "III","III","III", 'I',"ingotSteel");
		addShapelessOredictRecipe(new ItemStack(itemMetal,9,0), "blockCopper");
		addShapelessOredictRecipe(new ItemStack(itemMetal,9,1), "blockAluminum");
		addShapelessOredictRecipe(new ItemStack(itemMetal,9,2), "blockLead");
		addShapelessOredictRecipe(new ItemStack(itemMetal,9,3), "blockSilver");
		addShapelessOredictRecipe(new ItemStack(itemMetal,9,4), "blockNickel");
		addShapelessOredictRecipe(new ItemStack(itemMetal,9,5), "blockConstantan");
		addShapelessOredictRecipe(new ItemStack(itemMetal,9,6), "blockElectrum");
		addShapelessOredictRecipe(new ItemStack(itemMetal,9,7), "blockSteel");


		addOredictRecipe(new ItemStack(blockStorage,1,8), "WWW","WIW","WWW", 'W',new ItemStack(itemWireCoil,1,0),'I',"ingotIron");
		addOredictRecipe(new ItemStack(blockStorage,1,9), "WWW","WIW","WWW", 'W',new ItemStack(itemWireCoil,1,1),'I',"ingotIron");
		addOredictRecipe(new ItemStack(blockStorage,1,10), "WWW","WIW","WWW", 'W',new ItemStack(itemWireCoil,1,2),'I',"ingotIron");

		addOredictRecipe(new ItemStack(blockMetalDevice,8, BlockMetalDevices.META_connectorLV), "BIB"," I ","BIB", 'I',"ingotCopper",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_capacitorLV), "III","CLC","WRW", 'L',"ingotLead",'I',"ingotIron",'C',"ingotCopper",'R',"dustRedstone",'W',treatedWood);
		addOredictRecipe(new ItemStack(blockMetalDevice,8, BlockMetalDevices.META_connectorMV), "BIB"," I ","BIB", 'I',"ingotIron",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_capacitorMV), "III","ELE","WRW", 'L',"ingotLead",'I',"ingotIron",'E',"ingotElectrum",'R',"blockRedstone",'W',treatedWood);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_transformer), "L M","IBI","III", 'L',new ItemStack(blockMetalDevice,1,BlockMetalDevices.META_connectorLV),'M',new ItemStack(blockMetalDevice,1,BlockMetalDevices.META_connectorMV),'I',"ingotIron",'B',electrumCoil).setMirrored(true);
		addOredictRecipe(new ItemStack(blockMetalDevice,8, BlockMetalDevices.META_relayHV), "BIB"," I ","BIB", 'I',"ingotIron",'B',new ItemStack(blockStoneDevice,1,4));
		addOredictRecipe(new ItemStack(blockMetalDevice,4, BlockMetalDevices.META_connectorHV), "BIB","BIB","BIB", 'I',"ingotAluminum",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_capacitorHV), "III","ALA","WRW", 'L',"blockLead",'I',"ingotSteel",'A',"ingotAluminum",'R',"blockRedstone",'W',treatedWood);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_transformerHV), "M H","IBI","III", 'H',new ItemStack(blockMetalDevice,1,BlockMetalDevices.META_connectorHV),'M',new ItemStack(blockMetalDevice,1,BlockMetalDevices.META_connectorMV),'I',"ingotIron",'B',hvCoil).setMirrored(true);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_dynamo), "RCR","III", 'C',copperCoil,'I',"ingotIron",'R',"dustRedstone");
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_thermoelectricGen), "III","CBC","CCC", 'I',"ingotSteel",'C',"ingotConstantan",'B',copperCoil);
		addOredictRecipe(new ItemStack(blockMetalDevice,8, BlockMetalDevices.META_conveyorBelt), "LLL","IRI", 'I',"ingotIron",'R',"dustRedstone",'L',Items.leather);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_furnaceHeater), "ICI","CBC","IRI", 'I',"ingotIron",'R',"dustRedstone",'C',"ingotCopper",'B',copperCoil);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_sorter), "IRI","WBW","IRI", 'I',"ingotIron",'R',"dustRedstone",'W',treatedWood,'B',componentIron);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_sampleDrill), "SFS","SFS","BFB", 'F',new ItemStack(blockMetalDecoration,1,BlockMetalDecoration.META_fence),'S',new ItemStack(blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding),'B',new ItemStack(blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering));
		
		addOredictRecipe(new ItemStack(blockMetalDevice2,1, BlockMetalDevices2.META_breakerSwitch), " L ","CIC", 'L',Blocks.lever,'C',Blocks.hardened_clay,'I',"ingotCopper");

		addOredictRecipe(new ItemStack(blockMetalDecoration,16,BlockMetalDecoration.META_fence), "III","III", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(blockMetalDecoration, 6,BlockMetalDecoration.META_scaffolding), "III"," S ","S S", 'I',"ingotSteel",'S',new ItemStack(blockMetalDecoration,1,0));
		addOredictRecipe(new ItemStack(blockMetalDecoration, 4,BlockMetalDecoration.META_lantern), " I ","PGP"," I ", 'G',"glowstone",'I',"ingotIron",'P',"paneGlass");
		addOredictRecipe(new ItemStack(blockMetalDecoration, 4,BlockMetalDecoration.META_structuralArm), "B  ","BB ","BBB", 'B',new ItemStack(blockMetalDecoration,1,1));
		addOredictRecipe(new ItemStack(blockMetalDecoration, 2,BlockMetalDecoration.META_radiator), "ICI","CBC","ICI", 'I',"ingotSteel",'C',"ingotCopper",'B',Items.water_bucket);
		addOredictRecipe(new ItemStack(blockMetalDecoration, 2,BlockMetalDecoration.META_heavyEngineering), "IGI","PEP","IGI", 'I',"ingotSteel",'E',"ingotElectrum",'G',componentSteel,'P',Blocks.piston);
		addOredictRecipe(new ItemStack(blockMetalDecoration, 2,BlockMetalDecoration.META_generator), "III","EDE","III", 'I',"ingotSteel",'E',"ingotElectrum",'D',new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_dynamo));
		addOredictRecipe(new ItemStack(blockMetalDecoration, 2,BlockMetalDecoration.META_lightEngineering), "IGI","CCC","IGI", 'I',"ingotIron",'C',"ingotCopper",'G',componentIron);
		addOredictRecipe(new ItemStack(blockMetalDecoration, 8,BlockMetalDecoration.META_connectorStructural), "FIF","III", 'I',"ingotSteel",'F',new ItemStack(blockMetalDecoration,1,0));
		addOredictRecipe(new ItemStack(blockMetalDecoration, 4,BlockMetalDecoration.META_wallMount), "WW","WF","W ", 'W',new ItemStack(blockMetalDecoration,1,1),'F',new ItemStack(blockMetalDecoration,1,0));

		addOredictRecipe(new ItemStack(blockMetalMultiblocks, 2,BlockMetalMultiblocks.META_squeezer), "IPI","GDG","IPI", 'I',"ingotIron",'D',"dyeGreen",'G',componentIron,'P',Blocks.piston);
		addOredictRecipe(new ItemStack(blockMetalMultiblocks, 2,BlockMetalMultiblocks.META_fermenter), "IPI","GDG","IPI", 'I',"ingotIron",'D',"dyeBlue",'G',componentIron,'P',Blocks.piston);
		addOredictRecipe(new ItemStack(blockMetalMultiblocks, 1,BlockMetalMultiblocks.META_lightningRod), "IFI","CBC","IHI", 'I',"ingotSteel",'F',new ItemStack(blockMetalDecoration,1,BlockMetalDecoration.META_fence),'B',new ItemStack(blockMetalDevice,1,BlockMetalDevices.META_capacitorHV),'C',electrumCoil,'H',hvCoil);

		CokeOvenRecipe.addRecipe(new ItemStack(itemMaterial,1,6), new ItemStack(Items.coal), 1800, 500);
		CokeOvenRecipe.addRecipe(new ItemStack(blockStoneDevice,1,3), "blockCoal", 1800*9, 5000);
		CokeOvenRecipe.addRecipe(new ItemStack(Items.coal,1,1), "logWood", 900, 250);
		BlastFurnaceRecipe.addRecipe(new ItemStack(itemMetal,1,7), "ingotIron", 1200);
		BlastFurnaceRecipe.addRecipe(new ItemStack(blockStorage,1,7), "blockIron", 1200*9);

		BlastFurnaceRecipe.addBlastFuel("fuelCoke", 1200);
		BlastFurnaceRecipe.addBlastFuel("blockFuelCoke", 1200*10);
		BlastFurnaceRecipe.addBlastFuel("charcoal", 300);
		BlastFurnaceRecipe.addBlastFuel("blockCharcoal", 300*10);
		GameRegistry.registerFuelHandler(new IEFuelHandler());

		//		CrusherRecipe r = addCrusherRecipe( 8, "Iron").addSecondaryOutput(new ItemStack(itemMetal,1,14), .1f);
		addOreProcessingRecipe(new ItemStack(itemMetal,2,8), "Iron", 4000, true, new ItemStack(itemMetal,1,14),.1f);
		ItemStack cinnabar = !OreDictionary.getOres("crystalCinnabar").isEmpty()?OreDictionary.getOres("crystalCinnabar").get(0):null;
		addOreProcessingRecipe(new ItemStack(itemMetal,2,9), "Gold", 4000, true, cinnabar,.05f);

		addOreProcessingRecipe(new ItemStack(itemMetal,2,10), "Copper", 4000, true, new ItemStack(itemMetal,1,9),.1f);
		addOreProcessingRecipe(new ItemStack(itemMetal,2,11), "Aluminum", 4000, true, null,0);
		addOreProcessingRecipe(new ItemStack(itemMetal,2,12), "Lead", 4000, true, new ItemStack(itemMetal,1,13),.1f);
		addOreProcessingRecipe(new ItemStack(itemMetal,2,13), "Silver", 4000, true, new ItemStack(itemMetal,1,12),.1f);
		ItemStack platinum = !OreDictionary.getOres("dustPlatinum").isEmpty()?OreDictionary.getOres("dustPlatinum").get(0):null;
		addOreProcessingRecipe(new ItemStack(itemMetal,2,14), "Nickel", 4000, true, platinum,.1f);

		//		r = addCrusherRecipe(10, "Copper").addSecondaryOutput(new ItemStack(itemMetal,1,9), .1f);
		//		r = addCrusherRecipe(11, "Aluminum");
		//		r = addCrusherRecipe(12, "Lead").addSecondaryOutput(new ItemStack(itemMetal,1,13), .1f);
		//		r = addCrusherRecipe(13, "Silver").addSecondaryOutput(new ItemStack(itemMetal,1,12), .1f);
		//		r = addCrusherRecipe(14, "Nickel");
		//		if(!OreDictionary.getOres("dustPlatinum").isEmpty())
		//			r.addSecondaryOutput(OreDictionary.getOres("dustPlatinum").get(0), .1f);
		addCrusherRecipe(new ItemStack(itemMetal,1,15), "ingotConstantan", 2400, null,0);
		addCrusherRecipe(new ItemStack(itemMetal,1,16), "ingotElectrum", 2400, null,0);

		addOreProcessingRecipe(new ItemStack(Items.dye,9,4), "Lapis", 4000, false, null,0);
		addOreProcessingRecipe(new ItemStack(Items.diamond,2), "Diamond", 4000, false, null,0);
		addOreProcessingRecipe(new ItemStack(Items.redstone,6), "Redstone", 4000, false, cinnabar,.25f);
		addOreProcessingRecipe(new ItemStack(Items.emerald,2), "Emerald", 4000, false, null,0);
		ItemStack sulfur = !OreDictionary.getOres("dustSulfur").isEmpty()?OreDictionary.getOres("dustSulfur").get(0):null;
		addOreProcessingRecipe(new ItemStack(Items.quartz,3), "Quartz", 4000, false, sulfur,.15f);
		addOreProcessingRecipe(new ItemStack(Items.coal,4), "Coal", 4000, false, null,0);
		
		CrusherRecipe.addRecipe(new ItemStack(Blocks.sand), "cobblestone", 3200);
		CrusherRecipe.addRecipe(new ItemStack(Blocks.sand), "blockGlass", 3200);
		CrusherRecipe.addRecipe(new ItemStack(Items.quartz,4), "blockQuartz", 3200);
		addOreDictCrusherRecipe("Tin", "Iron",.1f);
		addOreDictCrusherRecipe("Bronze", null,0);
		addOreDictCrusherRecipe("Steel", null,0);
		addOreDictCrusherRecipe("Enderium", null,0);
		addOreDictCrusherRecipe("Lumium", null,0);
		addOreDictCrusherRecipe("Signalum", null,0);
		addOreDictCrusherRecipe("Invar", null,0);
		addOreDictCrusherRecipe("Mithril", null,0);
		addOreDictCrusherRecipe("Platinum", null,0);
		addOreDictCrusherRecipe("Ardite", null,0);
		addOreDictCrusherRecipe("Cobalt", null,0);
		addOreDictCrusherRecipe("Zinc", null,0);
		addOreDictCrusherRecipe("Uranium", null,0);
		addOreDictCrusherRecipe("Yellorium", null,0);
		addItemToOreDictCrusherRecipe("dustCoal",1, new ItemStack(Items.coal), 2400);
		addItemToOreDictCrusherRecipe("dustWood",2, "logWood", 1600);
		addCrusherRecipe(new ItemStack(Items.blaze_powder,4), "rodBlaze", 1600, sulfur,.5f);
		CrusherRecipe.addRecipe(new ItemStack(itemMetal,1,17), "fuelCoke", 3200);
		CrusherRecipe.addRecipe(new ItemStack(itemMetal,1,18), "gemQuartz", 3200);

		//Nether Ores



		DieselHandler.registerFuel(fluidBiodiesel, 125);
		DieselHandler.registerFuel(FluidRegistry.getFluid("fuel"), 375);
		DieselHandler.registerFuel(FluidRegistry.getFluid("diesel"), 175);

		DieselHandler.addSqueezerRecipe(Items.wheat_seeds, 80, new FluidStack(fluidPlantoil, 80), null);
		DieselHandler.addSqueezerRecipe(Items.pumpkin_seeds, 80, new FluidStack(fluidPlantoil, 80), null);
		DieselHandler.addSqueezerRecipe(Items.melon_seeds, 80, new FluidStack(fluidPlantoil, 80), null);
		DieselHandler.addSqueezerRecipe(itemSeeds, 80, new FluidStack(fluidPlantoil, 120), null);

		DieselHandler.addFermenterRecipe(Items.reeds, 80, new FluidStack(fluidEthanol,80), null);
		DieselHandler.addFermenterRecipe(Items.melon, 80, new FluidStack(fluidEthanol,80), null);
		DieselHandler.addFermenterRecipe(Items.apple, 80, new FluidStack(fluidEthanol,80), null);

		DieselHandler.addRefineryRecipe(new FluidStack(fluidPlantoil,8), new FluidStack(fluidEthanol,8), new FluidStack(fluidBiodiesel,16));

		ThermoelectricHandler.registerSourceInKelvin("blockIce", 273);
		ThermoelectricHandler.registerSourceInKelvin("blockPackedIce", 200);
		ThermoelectricHandler.registerSourceInKelvin("blockPlutonium", 4000);
		ThermoelectricHandler.registerSourceInKelvin("blockBlutonium", 4000);
		ThermoelectricHandler.registerSourceInKelvin("blockUranium", 2000);
		ThermoelectricHandler.registerSourceInKelvin("blockYellorium", 2000);

		ExcavatorHandler.mineralVeinCapacity = Config.getInt("excavator_depletion");
		ExcavatorHandler.addMineral("Iron", 30, .1f, new String[]{"oreIron","oreNickel","oreTin","denseoreIron"}, new float[]{.5f,.25f,.20f,.05f});
		ExcavatorHandler.addMineral("Magnetite", 30, .1f, new String[]{"oreIron","oreGold"}, new float[]{.85f,.15f});
		ExcavatorHandler.addMineral("Pyrite", 20, .1f, new String[]{"oreIron","oreSulfur"}, new float[]{.5f,.5f});
		ExcavatorHandler.addMineral("Bauxite", 20, .2f, new String[]{"oreAluminum","oreTitanium","denseoreAluminum"}, new float[]{.90f,.05f,.05f});
		ExcavatorHandler.addMineral("Copper", 40, .2f, new String[]{"oreCopper","oreGold","oreNickel","denseoreCopper"}, new float[]{.65f,.25f,.05f,.05f});
		ExcavatorHandler.addMineral("Gold", 20, .3f, new String[]{"oreGold","oreCopper","oreNickel","denseoreGold"}, new float[]{.65f,.25f,.05f,.05f});
		ExcavatorHandler.addMineral("Nickel", 20, .3f, new String[]{"oreNickel","orePlatinum","oreIron","denseoreNickel"}, new float[]{.85f,.05f,.05f,.05f});
		ExcavatorHandler.addMineral("Platinum", 5, .35f, new String[]{"orePlatinum","oreNickel","oreIridium","denseorePlatinum"}, new float[]{.45f,.35f,.1f,.05f});
		ExcavatorHandler.addMineral("Uranium", 10, .35f, new String[]{"oreUranium","oreLead","orePlutonium","denseoreUranium"}, new float[]{.55f,.3f,.1f,.05f}).addReplacement("oreUranium", "oreYellorium");
		ExcavatorHandler.addMineral("Quartzite", 5, .3f, new String[]{"oreQuartz","oreCertusQuartz"}, new float[]{.6f,.4f});
		ExcavatorHandler.addMineral("Galena", 15, .2f, new String[]{"oreLead","oreSilver","oreSulfur","denseoreLead","denseoreSilver"}, new float[]{.40f,.40f,.1f,.05f,.05f});
		ExcavatorHandler.addMineral("Lead", 10, .15f, new String[]{"oreLead","oreSilver","denseoreLead"}, new float[]{.55f,.4f,.05f});
		ExcavatorHandler.addMineral("Silver", 10, .2f, new String[]{"oreSilver","oreLead","denseoreSilver"}, new float[]{.55f,.4f,.05f});
		ExcavatorHandler.addMineral("Lapis", 10, .2f, new String[]{"oreLapis","oreIron","oreSulfur","denseoreLapis"}, new float[]{.65f,.275f,.025f,.05f});


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
		addHammerCrushingRecipe("Iron",8);
		addHammerCrushingRecipe("Gold",9);
		addHammerCrushingRecipe("Copper",10);
		addHammerCrushingRecipe("Aluminum",11);
		addHammerCrushingRecipe("Lead",12);
		addHammerCrushingRecipe("Silver",13);
		addHammerCrushingRecipe("Nickel",14);
		Config.setBoolean("crushingOreRecipe", !validCrushingOres.isEmpty());
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
	public static void registerOre(String type, ItemStack ore, ItemStack ingot, ItemStack dust, ItemStack block)
	{
		if(ore!=null)
			OreDictionary.registerOre("ore"+type, ore);
		if(ingot!=null)
			OreDictionary.registerOre("ingot"+type, ingot);
		if(dust!=null)
			OreDictionary.registerOre("dust"+type, dust);
		if(block!=null)
			OreDictionary.registerOre("block"+type, block);
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

	public static ShapedOreRecipe addOredictRecipe(ItemStack output, Object... recipe)
	{
		ShapedOreRecipe sor = new ShapedOreRecipe(output, recipe);
		GameRegistry.addRecipe(sor);
		return sor;
	}
	public static void addShapelessOredictRecipe(ItemStack output, Object... recipe)
	{
		GameRegistry.addRecipe(new ShapelessOreRecipe(output, recipe));
	}
	public static List<String> validCrushingOres = new ArrayList();
	public static void addHammerCrushingRecipe(String oreName, int dustMeta)
	{
		if(OreDictionary.getOres("dust"+oreName).size()<2)
		{
			GameRegistry.addRecipe(new RecipeOreCrushing(oreName,dustMeta));
			validCrushingOres.add(oreName);
		}
	}
	public static void addBulletRecipes(int meta, Object load, int casingType)
	{
		addOredictRecipe(new ItemStack(itemBullet,3,meta), "III","CCC","GGG", 'I',load,'C',new ItemStack(itemBullet,1,casingType),'G',Items.gunpowder);
		addOredictRecipe(new ItemStack(itemBullet,2,meta), "II","CC","GG", 'I',load,'C',new ItemStack(itemBullet,1,casingType),'G',Items.gunpowder);
		addOredictRecipe(new ItemStack(itemBullet,1,meta), "I","C","G", 'I',load,'C',new ItemStack(itemBullet,1,casingType),'G',Items.gunpowder);
	}

	public static void addCrusherRecipe(ItemStack output, Object input, int energy, ItemStack secondary, float chance)
	{
		CrusherRecipe r = CrusherRecipe.addRecipe(output, input, energy);
		if(secondary!=null)
			r.addSecondaryOutput(secondary, chance);
	}
	public static void addOreProcessingRecipe(ItemStack output, String ore, int energy, boolean ingot, ItemStack secondary, float secChance)
	{
		if(ingot && !OreDictionary.getOres("ingot"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(output, output.stackSize/2), "ingot"+ore, (int)(energy*.6f), null,0);
		if(!OreDictionary.getOres("ore"+ore).isEmpty())
			addCrusherRecipe(output, "ore"+ore, energy, secondary,secChance);
		if(!OreDictionary.getOres("oreNether"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(output, output.stackSize*2), "oreNether"+ore, energy, new ItemStack(Blocks.netherrack),.15f);
	}
	public static void addOreDictCrusherRecipe(String ore, String secondaryDust, float chance)
	{
		if(OreDictionary.getOres("dust"+ore).isEmpty())
			return;
		ItemStack dust = OreDictionary.getOres("dust"+ore).get(0);
		if(dust==null)
			return;
		if(!OreDictionary.getOres("ore"+ore).isEmpty())
		{
			CrusherRecipe r = CrusherRecipe.addRecipe(Utils.copyStackWithAmount(dust, 2), "ore"+ore, 4000);
			if(secondaryDust!=null && chance>0 && !OreDictionary.getOres("dust"+secondaryDust).isEmpty())
				r.addSecondaryOutput(OreDictionary.getOres("dust"+secondaryDust).get(0), chance);
		}
		if(!OreDictionary.getOres("ingot"+ore).isEmpty())
			CrusherRecipe.addRecipe(Utils.copyStackWithAmount(dust, 1), "ingot"+ore, 2400);
		if(!OreDictionary.getOres("oreNether"+ore).isEmpty())
			CrusherRecipe.addRecipe(Utils.copyStackWithAmount(dust, 4), "oreNether"+ore, 4000).addSecondaryOutput(new ItemStack(Blocks.netherrack),.15f);
	}
	public static CrusherRecipe addItemToOreDictCrusherRecipe(String oreName, int outSize, Object input, int energy)
	{
		if(OreDictionary.getOres(oreName).isEmpty())
			return null;
		ItemStack out = OreDictionary.getOres(oreName).get(0);
		if(out==null)
			return null;
		return CrusherRecipe.addRecipe(Utils.copyStackWithAmount(out, outSize), input, energy);
	}
}