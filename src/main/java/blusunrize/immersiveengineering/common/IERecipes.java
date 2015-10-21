package blusunrize.immersiveengineering.common;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.crafting.RecipeJerrycan;
import blusunrize.immersiveengineering.common.crafting.RecipePotionBullets;
import blusunrize.immersiveengineering.common.crafting.RecipeRevolver;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.NetherOresHelper;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

public class IERecipes
{

	public static void initCraftingRecipes()
	{
		ItemStack copperCoil = new ItemStack(IEContent.blockStorage,1,8);
		ItemStack electrumCoil = new ItemStack(IEContent.blockStorage,1,9);
		ItemStack hvCoil = new ItemStack(IEContent.blockStorage,1,10);
		ItemStack componentIron = new ItemStack(IEContent.itemMaterial,1,11);
		ItemStack componentSteel = new ItemStack(IEContent.itemMaterial,1,12);

		addOredictRecipe(new ItemStack(IEContent.itemTool,1,0), " IF"," SI","S  ", 'I',"ingotIron", 'S',"stickWood", 'F',new ItemStack(Items.string));
		addOredictRecipe(new ItemStack(IEContent.itemTool,1,1), "SI"," S", 'I',"ingotIron", 'S',"treatedStick").setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.itemTool,1,2), " P ","SCS", 'C',"ingotCopper", 'P',Items.compass, 'S',"treatedStick");
		addShapelessOredictRecipe(new ItemStack(IEContent.itemTool,1,3), Items.book,Blocks.lever);
		addOredictRecipe(new ItemStack(IEContent.itemRevolver,1,0), " I ","HDB","GIG", 'I',"ingotIron",'B',new ItemStack(IEContent.itemMaterial,1,7),'D',new ItemStack(IEContent.itemMaterial,1,8),'G',new ItemStack(IEContent.itemMaterial,1,9),'H',new ItemStack(IEContent.itemMaterial,1,10)).setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.itemRevolver,1,1), "  I","IIS","  I", 'I',"ingotIron",'S',"ingotSteel");
		GameRegistry.addRecipe(new RecipeRevolver());

		addOredictRecipe(new ItemStack(IEContent.itemBullet,3,0), "I I","I I"," I ", 'I',"ingotCopper");
		addOredictRecipe(new ItemStack(IEContent.itemBullet,3,1), "PDP","PDP"," I ", 'I',"ingotCopper",'P',Items.paper,'D',"dyeRed");


		boolean hardmodeBullets = Config.getBoolean("hardmodeBulletRecipes");
		if(hardmodeBullets)
		{
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,2), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotLead");
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotSteel","ingotConstantan");
			if(!OreDictionary.getOres("ingotTungsten").isEmpty())
				BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotTungsten");
			if(!OreDictionary.getOres("ingotCyanite").isEmpty())
				BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotCyanite");
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,9), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotLead","nuggetSilver","nuggetSilver","nuggetSilver");
		}
		else
		{
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,2), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetLead","nuggetLead");
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetSteel","nuggetSteel","nuggetConstantan","nuggetConstantan");
			if(!OreDictionary.getOres("nuggetTungsten").isEmpty())
				BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetTungsten","nuggetTungsten");
			else if(!OreDictionary.getOres("ingotTungsten").isEmpty())
				BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,3,3), new ItemStack(IEContent.itemBullet,3,0),new ItemStack(Items.gunpowder,3),"ingotTungsten");
			if(!OreDictionary.getOres("nuggetCyanite").isEmpty())
				BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetCyanite","nuggetCyanite");
			else if(!OreDictionary.getOres("ingotCyanite").isEmpty())
				BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,3,3), new ItemStack(IEContent.itemBullet,3,0),new ItemStack(Items.gunpowder,3),"ingotCyanite");
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,9), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetLead","nuggetLead","nuggetSilver");
		}
		BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,4), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder,"dustIron");
		BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,5), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,Blocks.tnt);

		BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,6), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder,"dustAluminum","dustAluminum");
		if(Loader.isModLoaded("Botania"))
		{
			if(hardmodeBullets)
				BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,7), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotTerrasteel");
			else
				BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,7), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetTerrasteel","nuggetTerrasteel");
			BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,8), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder, new ItemStack(IEContent.itemBullet,4,7));
			Config.setBoolean("botaniaBullets", true);
		}
		BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,10), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"dustQuartz",Items.glass_bottle);

		BlueprintCraftingRecipe.addVillagerTrade("bullet", new ItemStack(Items.emerald,1,2));
		BlueprintCraftingRecipe.addVillagerTrade("specialBullet", new ItemStack(Items.emerald,1,7));
		GameRegistry.addRecipe(new RecipePotionBullets());

		int blueprint = BlueprintCraftingRecipe.blueprintCategories.indexOf("bullet");
		addOredictRecipe(new ItemStack(IEContent.itemBlueprint,1,blueprint), "JKL","DDD","PPP", 'J',Items.gunpowder,'K',"ingotCopper",'L',Items.gunpowder, 'D',"dyeBlue",'P',Items.paper);

		BlueprintCraftingRecipe.addRecipe("electrode", new ItemStack(IEContent.itemGraphiteElectrode), "ingotHOPGraphite","ingotHOPGraphite","ingotHOPGraphite","ingotHOPGraphite");
		BlueprintCraftingRecipe.addVillagerTrade("electrode", new ItemStack(Items.emerald,1,18));
		blueprint = BlueprintCraftingRecipe.blueprintCategories.indexOf("electrode");
		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(IEContent.itemBlueprint,1,blueprint), 1,1, 10));
		ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(IEContent.itemBlueprint,1,blueprint), 1,1, 2));
		if(Config.getBoolean("arcfurnace_electrodeCrafting"))
			addOredictRecipe(new ItemStack(IEContent.itemBlueprint,1,blueprint), "GGG","GDG","GPG", 'G',"ingotHOPGraphite", 'D',"dyeBlue",'P',Items.paper);

		addOredictRecipe(new ItemStack(IEContent.itemSkyhook,1,0), "II ","IC "," GG", 'C',componentIron,'I',"ingotSteel", 'G',new ItemStack(IEContent.itemMaterial,1,9));

		addOredictRecipe(new ItemStack(IEContent.itemDrill,1,0), "  G"," EG","C  ", 'C',componentSteel,'E',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_heavyEngineering), 'G',new ItemStack(IEContent.itemMaterial,1,9));
		addOredictRecipe(new ItemStack(IEContent.itemDrillhead,1,0), "SS ","BBS","SS ", 'B',"blockSteel", 'S',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.itemDrillhead,1,1), "SS ","BBS","SS ", 'B',"blockIron", 'S',"ingotIron");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,0), "BI ","IBI"," IC", 'B',Items.bucket, 'I',"dyeBlue", 'C',componentIron);
		for (ItemStack container : Utils.getContainersFilledWith(new FluidStack(IEContent.fluidPlantoil,1000)))
			addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,1), "BI ","IBI"," IC", 'B',container, 'I',"ingotIron", 'C',componentIron);
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,2), "SSS"," C ", 'S',"ingotSteel", 'C',componentSteel);
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,3), "CS ","SBO"," OB", 'C',componentIron, 'S',"ingotSteel", 'B',Items.bucket, 'O',"dyeRed");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,4), "SI","IW", 'S',Items.iron_sword, 'I',"ingotSteel", 'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,5), " CS","C C","IC ", 'I',componentIron, 'S',"ingotSteel", 'C',"ingotCopper");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,6), " G ","GEG","GEG", 'E',electrumCoil, 'G',"blockGlass");

		addShapelessOredictRecipe(new ItemStack(IEContent.itemMetal,2,15), "dustCopper","dustNickel");
		addShapelessOredictRecipe(new ItemStack(IEContent.itemMetal,2,16), "dustSilver","dustGold");

		addOredictRecipe(new ItemStack(IEContent.itemMaterial,4,0), "W","W", 'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,1), " S ","SBS","BSB", 'B',"plankTreatedWood", 'S',"treatedStick");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,2), "BB ","SSB","SS ", 'B',"plankTreatedWood", 'S',"treatedStick");
		addShapelessOredictRecipe(new ItemStack(Items.string), new ItemStack(IEContent.itemMaterial,1,3));
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,4), "HHH","HSH","HHH", 'H',new ItemStack(IEContent.itemMaterial,1,3), 'S',"stickWood");
		addShapelessOredictRecipe(new ItemStack(IEContent.itemMaterial,1,5), new ItemStack(IEContent.itemMaterial,1,2),new ItemStack(IEContent.itemMaterial,1,4),new ItemStack(IEContent.itemMaterial,1,4),new ItemStack(IEContent.itemMaterial,1,4),new ItemStack(IEContent.itemMaterial,1,4));
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,7), "III", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,8), " I ","ICI"," I ", 'I',"ingotSteel",'C',componentIron);
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,9), "SS","IS","SS", 'I',"ingotCopper",'S',"treatedStick");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,10), "I  ","II "," II", 'I',"ingotSteel");
		addOredictRecipe(componentIron, "I I"," C ","I I", 'I',"ingotIron",'C',"ingotCopper");
		addOredictRecipe(componentSteel, "I I"," C ","I I", 'I',"ingotSteel",'C',"ingotCopper");

		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,0), " I ","ISI"," I ", 'I',"ingotCopper", 'S',"stickWood");
		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,1), " I ","ISI"," I ", 'I',"ingotElectrum", 'S',"stickWood");
		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,2), " I ","ASA"," I ", 'I',"ingotSteel", 'A',"ingotAluminum", 'S',"stickWood");
		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,3), " I ","ISI"," I ", 'I',new ItemStack(IEContent.itemMaterial,1,3), 'S',"stickWood");
		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,4), " I ","ISI"," I ", 'I',"ingotSteel", 'S',"stickWood");

		GameRegistry.addRecipe(new RecipeJerrycan());


		for(ItemStack container : Utils.getContainersFilledWith(new FluidStack(IEContent.fluidCreosote,1000)))
			addOredictRecipe(new ItemStack(IEContent.blockTreatedWood,8,0), "WWW","WCW","WWW", 'W',"plankWood",'C',container);
		addOredictRecipe(new ItemStack(IEContent.blockTreatedWood,1,0), "W","W", 'W',new ItemStack(IEContent.blockWoodenDecoration,1,2));
		for(int i=0; i<IEContent.blockTreatedWood.subNames.length; i++)
			addShapelessOredictRecipe(new ItemStack(IEContent.blockTreatedWood,1, i-1<IEContent.blockTreatedWood.subNames.length?i+1:0), new ItemStack(IEContent.blockTreatedWood,1,i));
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDecoration,2,1), "SSS","SSS", 'S',"treatedStick");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDecoration,6,2), "WWW", 'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDecoration,6,5), "WWW"," S ","S S", 'W',"plankTreatedWood",'S',new ItemStack(IEContent.blockWoodenDecoration,1,1));
		addOredictRecipe(new ItemStack(IEContent.blockWoodenStair,4,0), "  W"," WW","WWW", 'W',"plankTreatedWood").setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDecoration,4,6), "WW","WF","W ", 'W',"plankTreatedWood",'F',new ItemStack(IEContent.blockWoodenDecoration,1,1));

		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice,1,0), "F","F","S", 'F',new ItemStack(IEContent.blockWoodenDecoration,1,1),'S',"bricksStone");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice,1,1), " P ","PWP"," P ", 'P',new ItemStack(IEContent.itemMaterial,1,1),'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice,1,2), " P ","PIP"," P ", 'P',new ItemStack(IEContent.itemMaterial,1,2),'I',"ingotIron");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice,1,3), "PPP","PIP","PPP", 'P',new ItemStack(IEContent.itemMaterial,1,5),'I',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice,1,4), "WWW","W W","WWW", 'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice,1,5), "WWW","B F", 'W',new ItemStack(IEContent.blockWoodenDecoration,1,2),'B',"craftingTableWood",'F',new ItemStack(IEContent.blockWoodenDecoration,1,1));
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice,1,6), "SSS","W W","WWW", 'W',"plankTreatedWood",'S',new ItemStack(IEContent.blockWoodenDecoration,1,2));

		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,6,0), "CCC","HHH","CCC", 'C',Items.clay_ball,'H',new ItemStack(IEContent.itemMaterial,1,3));
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,2,1), "CBC","BSB","CBC", 'S',"sandstone",'C',Items.clay_ball,'B',"ingotBrick");
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,2,2), "NBN","BDB","NBN", 'D',Items.blaze_powder,'N',"ingotBrickNether",'B',"ingotBrick");
		addTwoWayStorageRecipe(new ItemStack(IEContent.blockStoneDecoration,1,3), new ItemStack(IEContent.itemMaterial,1,6));
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,8,4), "SCS","GSG","SCS", 'C',Items.clay_ball,'S',"itemSlag",'G',Blocks.gravel);
		addShapelessOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,1,4), new ItemStack(IEContent.blockStoneDecoration,1,5));
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,4,5), "CC","CC", 'C',new ItemStack(IEContent.blockStoneDecoration,1,4));
		addOredictRecipe(new ItemStack(IEContent.blockStoneDevice,2,4), " G ","IDI"," G ", 'G',"blockGlass",'I',"dustIron",'D',"dyeGreen");

		addTwoWayStorageRecipe(new ItemStack(Items.iron_ingot), new ItemStack(IEContent.itemMetal,1,21));
		for(int i=0; i<=7; i++)
		{
			addTwoWayStorageRecipe(new ItemStack(IEContent.itemMetal,1,i), new ItemStack(IEContent.itemMetal,1,22+i));
			addTwoWayStorageRecipe(new ItemStack(IEContent.blockStorage,1,i), new ItemStack(IEContent.itemMetal,1,i));
			addOredictRecipe(new ItemStack(IEContent.blockStorageSlabs,6,i), "III", 'I',new ItemStack(IEContent.blockStorage,1,i));
			addOredictRecipe(new ItemStack(IEContent.blockStorage,1,i), "I","I", 'I',new ItemStack(IEContent.blockStorageSlabs,1,i));
		}

		addOredictRecipe(new ItemStack(IEContent.blockStorage,1,8), "WWW","WIW","WWW", 'W',new ItemStack(IEContent.itemWireCoil,1,0),'I',"ingotIron");
		addOredictRecipe(new ItemStack(IEContent.blockStorage,1,9), "WWW","WIW","WWW", 'W',new ItemStack(IEContent.itemWireCoil,1,1),'I',"ingotIron");
		addOredictRecipe(new ItemStack(IEContent.blockStorage,1,10), "WWW","WIW","WWW", 'W',new ItemStack(IEContent.itemWireCoil,1,2),'I',"ingotIron");

		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,8, BlockMetalDevices.META_connectorLV), "BIB"," I ","BIB", 'I',"ingotCopper",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_capacitorLV), "III","CLC","WRW", 'L',"ingotLead",'I',"ingotIron",'C',"ingotCopper",'R',"dustRedstone",'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,8, BlockMetalDevices.META_connectorMV), "BIB"," I ","BIB", 'I',"ingotIron",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_capacitorMV), "III","ELE","WRW", 'L',"ingotLead",'I',"ingotIron",'E',"ingotElectrum",'R',"blockRedstone",'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_transformer), "L M","IBI","III", 'L',new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorLV),'M',new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorMV),'I',"ingotIron",'B',electrumCoil).setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,8, BlockMetalDevices.META_relayHV), "BIB"," I ","BIB", 'I',"ingotIron",'B',new ItemStack(IEContent.blockStoneDevice,1,4));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,4, BlockMetalDevices.META_connectorHV), "BIB","BIB","BIB", 'I',"ingotAluminum",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_capacitorHV), "III","ALA","WRW", 'L',"blockLead",'I',"ingotSteel",'A',"ingotAluminum",'R',"blockRedstone",'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_transformerHV), "M H","IBI","III", 'H',new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorHV),'M',new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorMV),'I',"ingotIron",'B',hvCoil).setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_dynamo), "RCR","III", 'C',copperCoil,'I',"ingotIron",'R',"dustRedstone");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_thermoelectricGen), "III","CBC","CCC", 'I',"ingotSteel",'C',"ingotConstantan",'B',copperCoil);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,8, BlockMetalDevices.META_conveyorBelt), "LLL","IRI", 'I',"ingotIron",'R',"dustRedstone",'L',Items.leather);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_furnaceHeater), "ICI","CBC","IRI", 'I',"ingotIron",'R',"dustRedstone",'C',"ingotCopper",'B',copperCoil);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_sorter), "IRI","WBW","IRI", 'I',"ingotIron",'R',"dustRedstone",'W',"plankTreatedWood",'B',componentIron);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_sampleDrill), "SFS","SFS","BFB", 'F',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_fence),'S',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding),'B',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering));

		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice2,1, BlockMetalDevices2.META_breakerSwitch), " L ","CIC", 'L',Blocks.lever,'C',Blocks.hardened_clay,'I',"ingotCopper");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice2,2, BlockMetalDevices2.META_electricLantern), "PIP","PGP","IRI", 'P',"paneGlass",'I',"ingotIron",'G',"glowstone",'R',"dustRedstone");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice2,8, BlockMetalDevices2.META_fluidPipe), "I I","SSS","I I", 'S',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_sheetMetal),'I',"ingotIron");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice2,1, BlockMetalDevices2.META_fluidPump), " I ","ICI","PPP", 'I',"ingotIron",'C',componentIron,'P',new ItemStack(IEContent.blockMetalDevice2,1,BlockMetalDevices2.META_fluidPipe));

		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration,16,BlockMetalDecoration.META_fence), "III","III", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 6,BlockMetalDecoration.META_scaffolding), "III"," S ","S S", 'I',"ingotSteel",'S',new ItemStack(IEContent.blockMetalDecoration,1,0));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 4,BlockMetalDecoration.META_lantern), " I ","PGP"," I ", 'G',"glowstone",'I',"ingotIron",'P',"paneGlass");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 4,BlockMetalDecoration.META_structuralArm), "B  ","BB ","BBB", 'B',new ItemStack(IEContent.blockMetalDecoration,1,1));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 2,BlockMetalDecoration.META_radiator), "ICI","CBC","ICI", 'I',"ingotSteel",'C',"ingotCopper",'B',Items.water_bucket);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 2,BlockMetalDecoration.META_heavyEngineering), "IGI","PEP","IGI", 'I',"ingotSteel",'E',"ingotElectrum",'G',componentSteel,'P',Blocks.piston);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 2,BlockMetalDecoration.META_generator), "III","EDE","III", 'I',"ingotSteel",'E',"ingotElectrum",'D',new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_dynamo));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 2,BlockMetalDecoration.META_lightEngineering), "IGI","CCC","IGI", 'I',"ingotIron",'C',"ingotCopper",'G',componentIron);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 8,BlockMetalDecoration.META_connectorStructural), "FIF","III", 'I',"ingotSteel",'F',new ItemStack(IEContent.blockMetalDecoration,1,0));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 4,BlockMetalDecoration.META_wallMount), "WW","WF","W ", 'W',new ItemStack(IEContent.blockMetalDecoration,1,1),'F',new ItemStack(IEContent.blockMetalDecoration,1,0));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration, 4,BlockMetalDecoration.META_sheetMetal), " I ","IHI"," I ", 'I',"ingotIron",'H',new ItemStack(IEContent.itemTool,1,0));

		addOredictRecipe(new ItemStack(IEContent.blockMetalMultiblocks, 2,BlockMetalMultiblocks.META_squeezer), "IPI","GDG","IPI", 'I',"ingotIron",'D',"dyeGreen",'G',componentIron,'P',Blocks.piston);
		addOredictRecipe(new ItemStack(IEContent.blockMetalMultiblocks, 2,BlockMetalMultiblocks.META_fermenter), "IPI","GDG","IPI", 'I',"ingotIron",'D',"dyeBlue",'G',componentIron,'P',Blocks.piston);
		addOredictRecipe(new ItemStack(IEContent.blockMetalMultiblocks, 1,BlockMetalMultiblocks.META_lightningRod), "IFI","CBC","IHI", 'I',"ingotSteel",'F',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_fence),'B',new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_capacitorHV),'C',electrumCoil,'H',hvCoil);
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
	public static void addTwoWayStorageRecipe(ItemStack storage, ItemStack component)
	{
		addOredictRecipe(storage, "III","III","III", 'I',component);
		addShapelessOredictRecipe(Utils.copyStackWithAmount(component,9), storage);
	}


	public static void initFurnaceRecipes()
	{
		//Ores
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.blockOres,1,0), new ItemStack(IEContent.itemMetal,1,0), 0.3F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.blockOres,1,1), new ItemStack(IEContent.itemMetal,1,1), 0.3F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.blockOres,1,2), new ItemStack(IEContent.itemMetal,1,2), 0.7F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.blockOres,1,3), new ItemStack(IEContent.itemMetal,1,3), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.blockOres,1,4), new ItemStack(IEContent.itemMetal,1,4), 1.0F);
		//Dusts
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,8), new ItemStack(Items.iron_ingot), 0.7F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,9), new ItemStack(Items.gold_ingot), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,10), new ItemStack(IEContent.itemMetal,1,0), 0.3F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,11), new ItemStack(IEContent.itemMetal,1,1), 0.3F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,12), new ItemStack(IEContent.itemMetal,1,2), 0.7F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,13), new ItemStack(IEContent.itemMetal,1,3), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,14), new ItemStack(IEContent.itemMetal,1,4), 0.5F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,15), new ItemStack(IEContent.itemMetal,1,5), 0.5F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,16), new ItemStack(IEContent.itemMetal,1,6), 0.5F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(IEContent.itemMetal,1,19), new ItemStack(IEContent.itemMetal,1,20), 0F);
	}

	public static HashMap<String, ItemStack> oreOutputModifier = new HashMap<String, ItemStack>();
	public static HashMap<String, Object[]> oreOutputSecondaries = new HashMap<String, Object[]>();
	public static void initCrusherRecipes()
	{
		oreOutputModifier.put("Iron", new ItemStack(IEContent.itemMetal,2,8));
		oreOutputSecondaries.put("Iron", new Object[]{new ItemStack(IEContent.itemMetal,1,14),.1f});

		oreOutputModifier.put("Gold", new ItemStack(IEContent.itemMetal,2,9));
		oreOutputSecondaries.put("Gold", new Object[]{"crystalCinnabar",.05f});

		oreOutputModifier.put("Copper", new ItemStack(IEContent.itemMetal,2,10));
		oreOutputSecondaries.put("Copper", new Object[]{new ItemStack(IEContent.itemMetal,1,9),.1f});

		oreOutputModifier.put("Aluminum", new ItemStack(IEContent.itemMetal,2,11));
		oreOutputModifier.put("Aluminium", new ItemStack(IEContent.itemMetal,2,11));

		oreOutputModifier.put("Lead", new ItemStack(IEContent.itemMetal,2,12));
		oreOutputSecondaries.put("Lead", new Object[]{new ItemStack(IEContent.itemMetal,1,13),.1f});

		oreOutputModifier.put("Silver", new ItemStack(IEContent.itemMetal,2,13));
		oreOutputSecondaries.put("Silver", new Object[]{new ItemStack(IEContent.itemMetal,1,12),.1f});

		oreOutputModifier.put("Nickel", new ItemStack(IEContent.itemMetal,2,14));
		oreOutputSecondaries.put("Nickel", new Object[]{"dustPlatinum",.1f});

		addCrusherRecipe(new ItemStack(IEContent.itemMetal,1,15), "ingotConstantan", 3600, null,0);
		addCrusherRecipe(new ItemStack(IEContent.itemMetal,1,16), "ingotElectrum", 3600, null,0);
		addCrusherRecipe(new ItemStack(IEContent.itemMetal,1,19), "ingotHOPGraphite", 3600, null,0);
		CrusherRecipe.addRecipe(new ItemStack(IEContent.itemMetal,1,17), "fuelCoke", 4800);
		CrusherRecipe.addRecipe(new ItemStack(IEContent.itemMetal,1,18), "gemQuartz", 4800);

		oreOutputModifier.put("Lapis", new ItemStack(Items.dye,9,4));
		oreOutputModifier.put("Diamond", new ItemStack(Items.diamond,2));
		oreOutputModifier.put("Redstone", new ItemStack(Items.redstone,6));
		oreOutputSecondaries.put("Redstone", new Object[]{"crystalCinnabar",.25f});
		oreOutputModifier.put("Emerald", new ItemStack(Items.emerald,2));
		oreOutputModifier.put("Quartz", new ItemStack(Items.quartz,3));
		oreOutputSecondaries.put("Quartz", new Object[]{"dustSulfur",.15f});
		oreOutputModifier.put("Coal", new ItemStack(Items.coal,4));

		oreOutputSecondaries.put("Platinum", new Object[]{"dustNickel",.1f});
		oreOutputSecondaries.put("Tungsten", new Object[]{"dustManganese",.1f});
		oreOutputSecondaries.put("Uranium", new Object[]{"dustLead",.1f});
		oreOutputSecondaries.put("Yellorium", new Object[]{"dustLead",.1f});
		oreOutputSecondaries.put("Plutonium", new Object[]{"dustUranium",.1f});
		Item item = GameRegistry.findItem("IC2", "itemOreIridium");
		oreOutputSecondaries.put("Osmium", new Object[]{item,.01f});
		oreOutputSecondaries.put("Iridium", new Object[]{"dustPlatium",.1f});
		oreOutputSecondaries.put("FzDarkIron", new Object[]{"dustIron",.1f});
		item = GameRegistry.findItem("Railcraft", "firestone.raw");
		if(item!=null)
			oreOutputModifier.put("Firestone", new ItemStack(item));
		oreOutputSecondaries.put("Nikolite", new Object[]{Items.diamond,.025f});

		CrusherRecipe.addRecipe(new ItemStack(Blocks.sand), "cobblestone", 3200);
		CrusherRecipe.addRecipe(new ItemStack(Blocks.sand), "blockGlass", 3200);
		CrusherRecipe.addRecipe(new ItemStack(Items.quartz,4), "blockQuartz", 3200);
		CrusherRecipe.addRecipe(new ItemStack(Items.glowstone_dust,4), "glowstone", 3200);
		addCrusherRecipe(new ItemStack(Items.blaze_powder,4), "rodBlaze", 3200, "dustSulfur",.5f);
		addCrusherRecipe(new ItemStack(Items.dye,6,15), Items.bone, 3200);
		addItemToOreDictCrusherRecipe("dustCoal",1, new ItemStack(Items.coal), 2400);
		addItemToOreDictCrusherRecipe("dustWood",2, "logWood", 2400);
		addItemToOreDictCrusherRecipe("dustObsidian",4, Blocks.obsidian, 6000);
		for(int i=0; i<16; i++)
		{
			CrusherRecipe r = CrusherRecipe.addRecipe(new ItemStack(Items.string,4), new ItemStack(Blocks.wool,1,i), 3200);
			if(i!=0)
				r.addToSecondaryOutput(new ItemStack(Items.dye,1,15-i), .05f);
		}
	}
	public static void postInitCrusherAndArcRecipes()
	{
		for(String name : OreDictionary.getOreNames())
			if(name.startsWith("ore"))
			{
				String ore = name.substring("ore".length());
				ItemStack out = oreOutputModifier.get(ore);
				if(out==null)
				{
					ArrayList<ItemStack> gems = OreDictionary.getOres("gem"+ore);
					if(!gems.isEmpty())
						out = Utils.copyStackWithAmount(IEApi.getPreferredOreStack("gem"+ore), 2);
					else
					{
						ArrayList<ItemStack> dusts = OreDictionary.getOres("dust"+ore);
						if(!dusts.isEmpty())
							out = Utils.copyStackWithAmount(IEApi.getPreferredOreStack("dust"+ore), 2);
					}
				}
				if(out!=null)
				{
					Object[] secondaries = oreOutputSecondaries.get(ore);
					Object s = secondaries!=null&&secondaries.length>1?secondaries[0]: null;
					Float f = secondaries!=null&&secondaries.length>1&&secondaries[1] instanceof Float?(Float)secondaries[1]: 0;
					addOreProcessingRecipe(out, ore, 6000, true, s, f);
				}
				out = arcOutputModifier.get(ore);
				if(out==null)
				{
					ArrayList<ItemStack> ingots = OreDictionary.getOres("ingot"+ore);
					if(!ingots.isEmpty())
						out = Utils.copyStackWithAmount(IEApi.getPreferredOreStack("ingot"+ore),2);
				}
				if(out!=null)
					addArcOreSmelting(out, ore);
			}
			else if(name.startsWith("gem"))
			{
				String ore = name.substring("gem".length());
				ArrayList<ItemStack> dusts = OreDictionary.getOres("dust"+ore);
				if(!dusts.isEmpty())
					addCrusherRecipe(IEApi.getPreferredOreStack("dust"+ore), "gem"+ore, 6000, null,0);
			}
			else if(name.startsWith("dust"))
			{
				String ore = name.substring("dust".length());
				ItemStack out = arcOutputModifier.get(ore);
				if(out==null)
				{
					ArrayList<ItemStack> ingots = OreDictionary.getOres("ingot"+ore);
					if(!ingots.isEmpty())
						out = IEApi.getPreferredOreStack("ingot"+ore);
				}
				else
					out = Utils.copyStackWithAmount(out, out.stackSize/2);
				if(out!=null)
					addArcRecipe(out, "dust"+ore, 100,512, null);

				if(OreDictionary.doesOreNameExist("ingot"+ore))
					addCrusherRecipe(IEApi.getPreferredOreStack("dust"+ore), "ingot"+ore, 3600, null,0);
			}
	}

	public static CrusherRecipe addCrusherRecipe(ItemStack output, Object input, int energy, Object... secondary)
	{
		CrusherRecipe r = CrusherRecipe.addRecipe(output, input, energy);
		if(secondary!=null && secondary.length>0)
			r.addToSecondaryOutput(secondary);
		return r;
	}
	public static void addOreProcessingRecipe(ItemStack output, String ore, int energy, boolean ingot, Object secondary, float secChance)
	{
		if(ingot && !OreDictionary.getOres("ingot"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(output, output.stackSize/2), "ingot"+ore, (int)(energy*.6f));
		if(!OreDictionary.getOres("ore"+ore).isEmpty())
			addCrusherRecipe(output, "ore"+ore, energy, secondary,secChance);
		if(!OreDictionary.getOres("oreNether"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(output, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, energy, secondary,secChance,Blocks.netherrack,.15f);

		//YAY GregTech!
		if(!OreDictionary.getOres("oreNetherrack"+ore).isEmpty())
			addCrusherRecipe(output, "oreNetherrack"+ore, energy, secondary,secChance, new ItemStack(Blocks.netherrack),.15f);
		if(!OreDictionary.getOres("oreEndstone"+ore).isEmpty())
			addCrusherRecipe(output, "oreEndstone"+ore, energy, secondary,secChance, "dustEndstone",.5f);
		if(!OreDictionary.getOres("oreBlackgranite"+ore).isEmpty())
			addCrusherRecipe(output, "oreBlackgranite"+ore, energy, secondary,secChance, "dustGraniteBlack",.5f);
		if(!OreDictionary.getOres("oreRedgranite"+ore).isEmpty())
			addCrusherRecipe(output, "oreRedgranite"+ore, energy, secondary,secChance, "dustGraniteBlack",.5f);
	}
	public static void addOreDictCrusherRecipe(String ore, Object secondary, float chance)
	{
		if(OreDictionary.getOres("dust"+ore).isEmpty())
			return;
		ItemStack dust = IEApi.getPreferredOreStack("dust"+ore);
		if(dust==null)
			return;
		if(!OreDictionary.getOres("ore"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "ore"+ore, 6000, secondary,chance);
		if(!OreDictionary.getOres("ingot"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 1), "ingot"+ore, 3600);
		if(!OreDictionary.getOres("oreNether"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(dust, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, 6000, secondary,chance,Blocks.netherrack,.15f);

		//YAY GregTech!
		if(!OreDictionary.getOres("oreNetherrack"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreNetherrack"+ore, 6000, secondary,chance, new ItemStack(Blocks.netherrack),.15f);
		if(!OreDictionary.getOres("oreEndstone"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreEndstone"+ore, 6000, secondary,chance, "dustEndstone",.5f);
		if(!OreDictionary.getOres("oreBlackgranite"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreBlackgranite"+ore, 6000, secondary,chance, "dustGraniteBlack",.5f);
		if(!OreDictionary.getOres("oreRedgranite"+ore).isEmpty())
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreRedgranite"+ore, 6000, secondary,chance, "dustGraniteRed",.5f);
	}
	public static CrusherRecipe addItemToOreDictCrusherRecipe(String oreName, int outSize, Object input, int energy)
	{
		if(OreDictionary.getOres(oreName).isEmpty())
			return null;
		ItemStack out = IEApi.getPreferredOreStack(oreName);
		if(out==null)
			return null;
		return CrusherRecipe.addRecipe(Utils.copyStackWithAmount(out, outSize), input, energy);
	}


	public static HashMap<String, ItemStack> arcOutputModifier = new HashMap<String, ItemStack>();
	public static void initArcSmeltingRecipes()
	{
		//Steel
		ArcFurnaceRecipe.addRecipe(new ItemStack(IEContent.itemMetal,1,7), "ingotIron", new ItemStack(IEContent.itemMaterial,1,13), 400,512, "dustCoke");
		ArcFurnaceRecipe.addRecipe(new ItemStack(IEContent.itemMetal,1,7), "dustIron", new ItemStack(IEContent.itemMaterial,1,13), 400,512, "dustCoke");
		//Vanilla+IE Ores
		arcOutputModifier.put("Iron", new ItemStack(Items.iron_ingot,2));
		arcOutputModifier.put("Gold", new ItemStack(Items.gold_ingot,2));
		arcOutputModifier.put("Copper", new ItemStack(IEContent.itemMetal,2,0));
		arcOutputModifier.put("Aluminum", new ItemStack(IEContent.itemMetal,2,1));
		arcOutputModifier.put("Aluminium", new ItemStack(IEContent.itemMetal,2,1));
		arcOutputModifier.put("Lead", new ItemStack(IEContent.itemMetal,2,2));
		arcOutputModifier.put("Silver", new ItemStack(IEContent.itemMetal,2,3));
		arcOutputModifier.put("Nickel", new ItemStack(IEContent.itemMetal,2,4));
	}
	public static void addArcRecipe(ItemStack output, Object input, int time, int energyPerTick, ItemStack slag, Object... additives)
	{
		ArcFurnaceRecipe.addRecipe(output, input, slag, time, energyPerTick, additives);
	}
	public static void addArcOreSmelting(ItemStack output, String ore)
	{
		if(!OreDictionary.getOres("ore"+ore).isEmpty())
			addArcRecipe(output, "ore"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,13));
		if(!OreDictionary.getOres("oreNether"+ore).isEmpty())
			addArcRecipe(Utils.copyStackWithAmount(output, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,13));

		//YAY GregTech!
		if(!OreDictionary.getOres("oreNetherrack"+ore).isEmpty())
			addArcRecipe(output, "oreNetherrack"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,13));
		if(!OreDictionary.getOres("oreEndstone"+ore).isEmpty())
			addArcRecipe(output, "oreEndstone"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,13));
		if(!OreDictionary.getOres("oreBlackgranite"+ore).isEmpty())
			addArcRecipe(output, "oreBlackgranite"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,13));
		if(!OreDictionary.getOres("oreRedgranite"+ore).isEmpty())
			addArcRecipe(output, "oreRedgranite"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,13));
	}
}
