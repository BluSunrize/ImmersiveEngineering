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
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase.BlockIESimple;
import blusunrize.immersiveengineering.common.blocks.BlockStorage;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDynamo;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityThermoelectricGen;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;
import blusunrize.immersiveengineering.common.blocks.stone.BlockStoneDevices;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockIEWoodenStairs;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockWoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockWoodenDevices;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.crafting.IEFuelHandler;
import blusunrize.immersiveengineering.common.crafting.RecipeOreCrushing;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.items.ItemBullet;
import blusunrize.immersiveengineering.common.items.ItemIEBase;
import blusunrize.immersiveengineering.common.items.ItemIESeed;
import blusunrize.immersiveengineering.common.items.ItemIETool;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.items.ItemWireCoil;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class IEContent
{
	public static BlockIEBase blockOres;
	public static BlockIEBase blockStorage;
	public static BlockIEBase blockMetalDevice;
	public static BlockIEBase blockMetalDecoration;
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
	public static Fluid fluidCreosote;
	public static boolean IECreosote=false;

	public static void preInit()
	{
		blockOres = (BlockIEBase) new BlockIESimple("ore",Material.rock,ItemBlockIEBase.class, "Copper","Aluminum","Lead","Silver","Nickel").setHardness(3f).setResistance(5f);
		blockStorage = (BlockIEBase) new BlockStorage("Copper","Aluminum","Lead","Silver","Nickel","Constantan","Electrum","Steel", "CoilCopper","CoilElectrum","CoilHV").setHardness(4f).setResistance(5f);
		blockMetalDevice = new BlockMetalDevices();
		blockMetalDecoration = new BlockMetalDecoration();
		blockWoodenDevice = new BlockWoodenDevices();
		blockWoodenDecoration = new BlockWoodenDecoration();
		blockWoodenStair = new BlockIEWoodenStairs();
		blockStoneDevice = new BlockStoneDevices();
		blockCrop = new BlockIECrop("hemp", "0B","1B","2B","3B","4B","0T");

		itemMetal = new ItemIEBase("metal", 64, "ingotCopper","ingotAluminum","ingotLead","ingotSilver","ingotNickel","ingotConstantan","ingotElectrum","ingotSteel",   "dustIron","dustGold","dustCopper","dustAluminum","dustLead","dustSilver","dustNickel","dustConstantan","dustElectrum");
		itemMaterial = new ItemIEBase("material", 64, "treatedStick","waterwheelSegment","windmillBlade","hempFiber","fabric","windmillBladeAdvanced","coalCoke","bottleCreosote","bucketCreosote", "gunpartBarrel","gunpartDrum","gunpartGrip","gunpartHammer");
		itemSeeds = new ItemIESeed(blockCrop,"hemp");
		MinecraftForge.addGrassSeed(new ItemStack(itemSeeds), 5);
		itemWireCoil = new ItemWireCoil();
		itemTool = new ItemIETool();
		itemRevolver = new ItemRevolver();
		itemBullet = new ItemBullet();

		fluidCreosote = FluidRegistry.getFluid("creosote");
		if(fluidCreosote==null)
		{
			fluidCreosote = new Fluid("creosote").setDensity(800).setViscosity(1000);
			FluidRegistry.registerFluid(fluidCreosote);
			IECreosote=true;
		}

		//Ore Dict
		registerToOreDict("ore", blockOres);
		registerToOreDict("block", blockStorage, 0,1,2,3,4,5,6,7);
		registerToOreDict("", itemMetal);
		registerOre("Cupronickel",	null,new ItemStack(itemMetal,1,5),new ItemStack(itemMetal,1,15),new ItemStack(blockStorage,1,5));

		//		registerOre("Iron", 	null,null,new ItemStack(itemMetal,1,7),null);
		//		registerOre("Gold", 	null,null,new ItemStack(itemMetal,1,8),null);
		//		registerOre("Copper",	new ItemStack(blockOres,1,0),new ItemStack(itemMetal,1,0),new ItemStack(itemMetal,1,9),new ItemStack(blockStorage,1,0));
		//		registerOre("Aluminum",	new ItemStack(blockOres,1,1),new ItemStack(itemMetal,1,1),new ItemStack(itemMetal,1,10),new ItemStack(blockStorage,1,1));
		//		registerOre("Lead",		new ItemStack(blockOres,1,2),new ItemStack(itemMetal,1,2),new ItemStack(itemMetal,1,11),new ItemStack(blockStorage,1,2));
		//		registerOre("Silver",	new ItemStack(blockOres,1,3),new ItemStack(itemMetal,1,3),new ItemStack(itemMetal,1,12),new ItemStack(blockStorage,1,3));
		//		registerOre("Nickel",	new ItemStack(blockOres,1,4),new ItemStack(itemMetal,1,4),new ItemStack(itemMetal,1,13),new ItemStack(blockStorage,1,3));
		//		registerOre("Constantan",null,new ItemStack(itemMetal,1,5),new ItemStack(itemMetal,1,13),new ItemStack(blockStorage,1,3));
		//		registerOre("Electrum",	null,new ItemStack(itemMetal,1,5),new ItemStack(itemMetal,1,14),new ItemStack(blockStorage,1,5));
		//		registerOre("Steel",	null,new ItemStack(itemMetal,1,6),null,new ItemStack(blockStorage,1,6));


		OreDictionary.registerOre("treatedStick", new ItemStack(itemMaterial,1,0));
		OreDictionary.registerOre("fuelCoke", new ItemStack(itemMaterial,1,6));
		OreDictionary.registerOre("blockFuelCoke", new ItemStack(blockStoneDevice,1,3));
		//Vanilla OreDict
		OreDictionary.registerOre("bricksStone", new ItemStack(Blocks.stonebrick));
		//Fluid Containers
		FluidContainerRegistry.registerFluidContainer(fluidCreosote, new ItemStack(itemMaterial,1,7), new ItemStack(Items.glass_bottle));
		FluidContainerRegistry.registerFluidContainer(fluidCreosote, new ItemStack(itemMaterial,1,8), new ItemStack(Items.bucket));
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
		IEWorldGen.addOreGen(blockOres, 0,8, 40,72, 8,100);
		IEWorldGen.addOreGen(blockOres, 1,8, 40,85, 8,100);
		IEWorldGen.addOreGen(blockOres, 2,8,  8,36, 4,100);
		IEWorldGen.addOreGen(blockOres, 3,8,  8,40, 4,80);
		IEWorldGen.addOreGen(blockOres, 4,4,  8,24, 2,100);
	}

	public static void init()
	{
		/**TILEENTITIES*/
		registerTile(TileEntityWoodenPost.class);
		registerTile(TileEntityWatermill.class);
		registerTile(TileEntityWindmill.class);
		registerTile(TileEntityWindmillAdvanced.class);

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

		registerTile(TileEntityCokeOven.class);
		registerTile(TileEntityBlastFurnace.class);

		/**ENTITIES*/
		EntityRegistry.registerModEntity(EntityRevolvershot.class, "revolverShot", 0, ImmersiveEngineering.instance, 64, 1, true);


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


		/**CRAFTING*/
		ItemStack treatedWood = new ItemStack(blockWoodenDecoration,1,0);
		ItemStack copperCoil = new ItemStack(blockStorage,1,8);
		ItemStack electrumCoil = new ItemStack(blockStorage,1,9);
		ItemStack hvCoil = new ItemStack(blockStorage,1,10);

		addOredictRecipe(new ItemStack(itemTool,1,0), " IF"," SI","S  ", 'I',"ingotIron", 'S',"stickWood", 'F',new ItemStack(Items.string));
		addOredictRecipe(new ItemStack(itemTool,1,1), "SI"," S", 'I',"ingotIron", 'S',"treatedStick").setMirrored(true);
		addOredictRecipe(new ItemStack(itemTool,1,2), " P ","SCS", 'C',"ingotCopper", 'P',Items.compass, 'S',"treatedStick");
		addShapelessOredictRecipe(new ItemStack(itemTool,1,3), Items.book,Blocks.lever);
		addOredictRecipe(new ItemStack(itemRevolver,1,0), " I ","HDB","GIG", 'I',"ingotIron",'B',new ItemStack(itemMaterial,1,9),'D',new ItemStack(itemMaterial,1,10),'G',new ItemStack(itemMaterial,1,11),'H',new ItemStack(itemMaterial,1,12));
		addOredictRecipe(new ItemStack(itemRevolver,1,2), "  I","IIS","  I", 'I',"ingotIron",'S',"ingotSteel");

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

		addShapelessOredictRecipe(new ItemStack(itemMetal,2,15), "dustCopper","dustNickel");
		addShapelessOredictRecipe(new ItemStack(itemMetal,2,16), "dustSilver","dustGold");

		addOredictRecipe(new ItemStack(itemMaterial,4,0), "W","W", 'W',treatedWood);
		addOredictRecipe(new ItemStack(itemMaterial,1,1), " S ","SBS","BSB", 'B',treatedWood, 'S',"treatedStick");
		addOredictRecipe(new ItemStack(itemMaterial,1,2), "BB ","SSB","SS ", 'B',treatedWood, 'S',"treatedStick");
		addOredictRecipe(new ItemStack(itemMaterial,1,4), "HHH","HSH","HHH", 'H',new ItemStack(itemMaterial,1,3), 'S',"stickWood");
		addShapelessOredictRecipe(new ItemStack(itemMaterial,1,5), new ItemStack(itemMaterial,1,2),new ItemStack(itemMaterial,1,4),new ItemStack(itemMaterial,1,4),new ItemStack(itemMaterial,1,4),new ItemStack(itemMaterial,1,4));
		addShapelessOredictRecipe(new ItemStack(itemMaterial,1,6), new ItemStack(blockStoneDevice,1,3));
		addOredictRecipe(new ItemStack(itemMaterial,1,9), "III", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(itemMaterial,1,10), " I ","III"," I ", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(itemMaterial,1,11), "SS","IS","SS", 'I',"ingotCopper",'S',"treatedStick");
		addOredictRecipe(new ItemStack(itemMaterial,1,12), "I  ","II "," II", 'I',"ingotSteel");

		addOredictRecipe(new ItemStack(itemWireCoil,4,0), " I ","ISI"," I ", 'I',"ingotCopper", 'S',"stickWood");
		addOredictRecipe(new ItemStack(itemWireCoil,4,1), " I ","ISI"," I ", 'I',"ingotElectrum", 'S',"stickWood");
		addOredictRecipe(new ItemStack(itemWireCoil,4,2), " I ","ASA"," I ", 'I',"ingotSteel", 'A',"ingotAluminum", 'S',"stickWood");

		for (ItemStack container : Utils.getContainersFilledWith(new FluidStack(fluidCreosote,1000)))
			addOredictRecipe(new ItemStack(blockWoodenDecoration,8,0), "WWW","WCW","WWW", 'W',"plankWood",'C',container);
		addOredictRecipe(new ItemStack(blockWoodenDecoration,2,1), "SSS","SSS", 'S',"treatedStick");
		addOredictRecipe(new ItemStack(blockWoodenDecoration,6,2), "WWW", 'W',treatedWood);
		addOredictRecipe(new ItemStack(blockWoodenStair,4,0), "  W"," WW","WWW", 'W',treatedWood).setMirrored(true);

		addOredictRecipe(new ItemStack(blockWoodenDevice,1,0), "F","F","S", 'F',new ItemStack(blockWoodenDecoration,1,1),'S',"bricksStone");
		addOredictRecipe(new ItemStack(blockWoodenDevice,1,1), " P ","PWP"," P ", 'P',new ItemStack(itemMaterial,1,1),'W',treatedWood);
		addOredictRecipe(new ItemStack(blockWoodenDevice,1,2), " P ","PIP"," P ", 'P',new ItemStack(itemMaterial,1,2),'I',"ingotIron");
		addOredictRecipe(new ItemStack(blockWoodenDevice,6,3), "WWW"," S ","S S", 'W',treatedWood,'S',"treatedStick");
		addOredictRecipe(new ItemStack(blockWoodenDevice,1,4), "PPP","PIP","PPP", 'P',new ItemStack(itemMaterial,1,5),'I',"ingotSteel");

		addOredictRecipe(new ItemStack(blockStoneDevice,6,0), "CCC","HHH","CCC", 'C',Blocks.clay,'H',new ItemStack(itemMaterial,1,3));
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
		addOredictRecipe(new ItemStack(blockStorage,1,8), "WWW","WIW","WWW", 'W',new ItemStack(itemWireCoil,1,0),'I',"ingotIron");
		addOredictRecipe(new ItemStack(blockStorage,1,9), "WWW","WIW","WWW", 'W',new ItemStack(itemWireCoil,1,1),'I',"ingotIron");
		addOredictRecipe(new ItemStack(blockStorage,1,10), "WWW","WIW","WWW", 'W',new ItemStack(itemWireCoil,1,2),'I',"ingotIron");

		addOredictRecipe(new ItemStack(blockMetalDevice,8, BlockMetalDevices.META_connectorLV), "BIB"," I ","BIB", 'I',"ingotCopper",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_capacitorLV), "III","CLC","WRW", 'L',"ingotLead",'I',"ingotIron",'C',"ingotCopper",'R',"dustRedstone",'W',treatedWood);
		addOredictRecipe(new ItemStack(blockMetalDevice,8, BlockMetalDevices.META_connectorMV), "BIB"," I ","BIB", 'I',"ingotIron",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_capacitorMV), "III","ELE","WRW", 'L',"ingotLead",'I',"ingotIron",'E',"ingotElectrum",'R',"blockRedstone",'W',treatedWood);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_transformer), "C C","IBI","III", 'C',new ItemStack(blockMetalDevice,1,0),'I',"ingotIron",'B',electrumCoil);
		addOredictRecipe(new ItemStack(blockMetalDevice,8, BlockMetalDevices.META_relayHV), "BIB"," I ","BIB", 'I',"ingotIron",'B',new ItemStack(blockStoneDevice,1,4));
		addOredictRecipe(new ItemStack(blockMetalDevice,4, BlockMetalDevices.META_connectorHV), "BIB","BIB","BIB", 'I',"ingotAluminum",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_capacitorHV), "III","ALA","WRW", 'L',"blockLead",'I',"ingotSteel",'A',"ingotAluminum",'R',"blockRedstone",'W',treatedWood);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_transformerHV), "C C","IBI","III", 'C',new ItemStack(blockMetalDevice,1,8),'I',"ingotIron",'B',hvCoil);
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_dynamo), "RCR","III", 'C',copperCoil,'I',"ingotIron",'R',"dustRedstone");
		addOredictRecipe(new ItemStack(blockMetalDevice,1, BlockMetalDevices.META_thermoelectricGen), "III","CBC","CCC", 'I',"ingotSteel",'C',"ingotConstantan",'B',copperCoil);
		
		addOredictRecipe(new ItemStack(blockMetalDecoration,16,0), "III","III", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(blockMetalDecoration,4,1), "III"," S ","S S", 'I',"ingotSteel",'S',new ItemStack(blockMetalDecoration,1,0));
		addOredictRecipe(new ItemStack(blockMetalDecoration,4,2), " I ","PGP"," I ", 'G',"glowstone",'I',"ingotIron",'P',"paneGlass");
		
		//		addOredictRecipe(new ItemStack(blockMetalDevice,8,0), "BIB"," I ","BIB", 'I',"ingotCopper",'B',Blocks.hardened_clay);
		//		addOredictRecipe(new ItemStack(blockMetalDevice,1,1), "III","RLR","WLW", 'L',"ingotLead",'I',"ingotIron",'R',"dustRedstone",'W',treatedWood);
		//		addOredictRecipe(new ItemStack(blockMetalDevice,4,2), " I ","PGP"," I ", 'G',"glowstone",'I',"ingotIron",'P',"paneGlass");
		//		addOredictRecipe(new ItemStack(blockMetalDevice,1,3), "C C","IBI","III", 'C',new ItemStack(blockMetalDevice,1,0),'I',"ingotIron",'B',electrumCoil);
		//		addOredictRecipe(new ItemStack(blockMetalDevice,8,4), "BIB"," I ","BIB", 'I',"ingotIron",'B',new ItemStack(blockStoneDevice,1,4));
		//		addOredictRecipe(new ItemStack(blockMetalDevice,1,5), "C C","IBI","III", 'C',new ItemStack(blockMetalDevice,1,8),'I',"ingotIron",'B',hvCoil);
		//		addOredictRecipe(new ItemStack(blockMetalDevice,1,6), "RCR","III", 'C',copperCoil,'I',"ingotIron",'R',"dustRedstone");
		//		addOredictRecipe(new ItemStack(blockMetalDevice,1,7), "III","ALA","WRW", 'L',"blockLead",'I',"ingotSteel",'A',"ingotAluminum",'R',"dustRedstone",'W',treatedWood);
		//		addOredictRecipe(new ItemStack(blockMetalDevice,4,8), "BIB","BIB","BIB", 'I',"ingotAluminum",'B',Blocks.hardened_clay);
		//		addOredictRecipe(new ItemStack(blockMetalDevice,1,9), "III","CBC","CCC", 'I',"ingotSteel",'C',"ingotConstantan",'B',copperCoil);


		CokeOvenRecipe.addRecipe(new ItemStack(Items.coal), new ItemStack(itemMaterial,1,6), 1800, 500);
		CokeOvenRecipe.addRecipe("blockCoal", new ItemStack(blockStoneDevice,1,4), 1800*9, 5000);
		CokeOvenRecipe.addRecipe("logWood", new ItemStack(Items.coal,1,1), 900, 250);
		BlastFurnaceRecipe.addRecipe("ingotIron", new ItemStack(itemMetal,1,7), 1200);
		BlastFurnaceRecipe.addRecipe("blockIron", new ItemStack(blockStorage,1,7), 1200*9);

		GameRegistry.registerFuelHandler(new IEFuelHandler());

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
		addHammerCrushingRecipe("Iron");
		addHammerCrushingRecipe("Gold");
		addHammerCrushingRecipe("Copper");
		addHammerCrushingRecipe("Aluminum");
		addHammerCrushingRecipe("Lead");
		addHammerCrushingRecipe("Silver");
		addHammerCrushingRecipe("Nickel");
		Config.setBoolean("crushingOreRecipe", validCrushingOres.isEmpty());
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
	public static void addHammerCrushingRecipe(String oreName)
	{
		if(OreDictionary.getOres("dust"+oreName).size()<2)
		{
			GameRegistry.addRecipe(new RecipeOreCrushing(oreName));
			validCrushingOres.add(oreName);
		}
	}
	public static void addBulletRecipes(int meta, Object load, int casingType)
	{
		addOredictRecipe(new ItemStack(itemBullet,3,meta), "III","CCC","GGG", 'I',load,'C',new ItemStack(itemBullet,1,casingType),'G',Items.gunpowder);
		addOredictRecipe(new ItemStack(itemBullet,2,meta), "II","CC","GG", 'I',load,'C',new ItemStack(itemBullet,1,casingType),'G',Items.gunpowder);
		addOredictRecipe(new ItemStack(itemBullet,1,meta), "I","C","G", 'I',load,'C',new ItemStack(itemBullet,1,casingType),'G',Items.gunpowder);
	}

}