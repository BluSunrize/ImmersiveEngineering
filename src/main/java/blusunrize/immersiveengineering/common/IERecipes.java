package blusunrize.immersiveengineering.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.ArrayListMultimap;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.blocks.cloth.BlockTypes_ClothDevice;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Conveyor;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDevice0;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDevice1;
import blusunrize.immersiveengineering.common.crafting.RecipeEarmuffs;
import blusunrize.immersiveengineering.common.crafting.RecipeFlareBullets;
import blusunrize.immersiveengineering.common.crafting.RecipeJerrycan;
import blusunrize.immersiveengineering.common.crafting.RecipePotionBullets;
import blusunrize.immersiveengineering.common.crafting.RecipeRevolver;
import blusunrize.immersiveengineering.common.crafting.RecipeShaderBags;
import blusunrize.immersiveengineering.common.crafting.RecipeShapedArrayList;
import blusunrize.immersiveengineering.common.crafting.RecipeShapedOreNBTCopy;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class IERecipes
{

	public static void initCraftingRecipes()
	{
		RecipeSorter.register(ImmersiveEngineering.MODID+":shapedArrayList", RecipeShapedArrayList.class, RecipeSorter.Category.SHAPED, "after:forge:shapedore");

		ItemStack copperCoil = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.COIL_LV.getMeta());
		ItemStack electrumCoil = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.COIL_MV.getMeta());
		ItemStack hvCoil = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.COIL_HV.getMeta());
		ItemStack componentIron = new ItemStack(IEContent.itemMaterial,1,8);
		ItemStack componentSteel = new ItemStack(IEContent.itemMaterial,1,9);
		ItemStack woodenGrip = new ItemStack(IEContent.itemMaterial,1,13);


		addOredictRecipe(new ItemStack(IEContent.itemMaterial,4,0), "W","W", 'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,4,1), "I","I", 'I',"ingotIron");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,4,2), "I","I", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,4,3), "I","I", 'I',"ingotAluminum");
		addShapelessOredictRecipe(new ItemStack(Items.string), new ItemStack(IEContent.itemMaterial,1,4),new ItemStack(IEContent.itemMaterial,1,4),new ItemStack(IEContent.itemMaterial,1,4));
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,5), "HHH","HSH","HHH", 'H',new ItemStack(IEContent.itemMaterial,1,4), 'S',"stickWood");
		addOredictRecipe(componentIron, "I I"," C ","I I", 'I',"ingotIron",'C',"ingotCopper");
		addOredictRecipe(componentSteel, "I I"," C ","I I", 'I',"ingotSteel",'C',"ingotCopper");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,10), " S ","SBS","BSB", 'B',"plankTreatedWood", 'S',"stickTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,11), "BB ","SSB","SS ", 'B',"plankTreatedWood", 'S',"stickTreatedWood");
		addShapelessOredictRecipe(new ItemStack(IEContent.itemMaterial,1,12), new ItemStack(IEContent.itemMaterial,1,11),"fabricHemp","fabricHemp","fabricHemp","fabricHemp");

		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,13), "SS","IS","SS", 'I',"ingotCopper",'S',"stickTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,14), "III", 'I',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,15), " I ","ICI"," I ", 'I',"ingotSteel",'C',componentIron);
		addOredictRecipe(new ItemStack(IEContent.itemMaterial,1,16), "I  ","II "," II", 'I',"ingotSteel");
		//		addOredictRecipe(new ItemStack(IEContent.itemMaterial,4,14), "I","I", 'I',"ingotIron");
		//		addOredrrictRecipe(new ItemStack(IEContent.itemMaterial,4,15), "I","I", 'I',"ingotSteel");


		addOredictRecipe(new ItemStack(IEContent.itemTool,1,0), " IF"," SI","S  ", 'I',"ingotIron", 'S',"stickWood", 'F',new ItemStack(Items.string));
		addOredictRecipe(new ItemStack(IEContent.itemTool,1,1), "SI"," S", 'I',"ingotIron", 'S',"stickTreatedWood").setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.itemTool,1,2), " P ","SCS", 'C',"ingotCopper", 'P',Items.compass, 'S',"stickTreatedWood");
		addShapelessOredictRecipe(new ItemStack(IEContent.itemTool,1,3), Items.book,Blocks.lever);
		addOredictRecipe(new ItemStack(IEContent.itemRevolver,1,0), " I ","BDH","GIG", 'I',"ingotIron",'B',new ItemStack(IEContent.itemMaterial,1,14),'D',new ItemStack(IEContent.itemMaterial,1,15),'G',woodenGrip,'H',new ItemStack(IEContent.itemMaterial,1,16)).setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.itemRevolver,1,1), "  I","IIS","  I", 'I',"ingotIron",'S',"ingotSteel");
		GameRegistry.addRecipe(new RecipeRevolver());
		RecipeSorter.register(ImmersiveEngineering.MODID+":revolverLoop", RecipeRevolver.class, RecipeSorter.Category.SHAPELESS, "after:forge:shapelessore");

		addOredictRecipe(new ItemStack(IEContent.itemBullet,5,0), "I I","I I"," I ", 'I',"ingotCopper");
		addOredictRecipe(new ItemStack(IEContent.itemBullet,5,1), "PDP","PDP"," I ", 'I',"ingotCopper",'P',Items.paper,'D',"dyeRed");
		int blueprint = BlueprintCraftingRecipe.blueprintCategories.indexOf("bullet");
		addOredictRecipe(new ItemStack(IEContent.itemBlueprint,1,blueprint), "JKL","DDD","PPP", 'J',Items.gunpowder,'K',"ingotCopper",'L',Items.gunpowder, 'D',"dyeBlue",'P',Items.paper);
		addOredictRecipe(new ItemStack(IEContent.itemMold,1,3), " P ","PCP"," P ", 'P',"plateSteel",'C',new ItemStack(IEContent.itemBullet,1,0));
		MetalPressRecipe.addRecipe(new ItemStack(IEContent.itemBullet,2,0),"ingotCopper",new ItemStack(IEContent.itemMold,1,3), 2400);

		addOredictRecipe(new ItemStack(IEContent.itemSkyhook,1,0), "II ","IC "," GG", 'C',componentIron,'I',"ingotSteel", 'G',woodenGrip);

		addOredictRecipe(new ItemStack(IEContent.itemDrill,1,0), "  G"," EG","C  ", 'C',componentSteel,'E',new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()), 'G',woodenGrip);
		addOredictRecipe(new ItemStack(IEContent.itemDrillhead,1,0), "SS ","BBS","SS ", 'B',"blockSteel", 'S',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.itemDrillhead,1,1), "SS ","BBS","SS ", 'B',"blockIron", 'S',"ingotIron");

		addOredictRecipe(new ItemStack(IEContent.itemChemthrower,1,0), " OG"," EG","PB ", 'P',new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()), 'O',new ItemStack(IEContent.itemToolUpgrades,1,0), 'B',Items.bucket, 'E',new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()), 'G',woodenGrip);

		addOredictRecipe(new ItemStack(IEContent.itemRailgun,1,0), " HG","CBH","BC ", 'C',electrumCoil, 'H',new ItemStack(IEContent.blockMetalDevice0,1,BlockTypes_MetalDevice0.CAPACITOR_HV.getMeta()), 'B',new ItemStack(IEContent.itemMaterial,1,14), 'G',woodenGrip);

		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,0), "BI ","IBI"," IC", 'B',Items.bucket, 'I',"dyeBlue", 'C',componentIron);
		for (ItemStack container : Utils.getContainersFilledWith(new FluidStack(IEContent.fluidPlantoil,1000)))
			addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,1), "BI ","IBI"," IC", 'B',container, 'I',"ingotIron", 'C',componentIron);
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,2), "SSS"," C ", 'S',"ingotSteel", 'C',componentSteel);
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,3), "CS ","SBO"," OB", 'C',componentIron, 'S',"ingotSteel", 'B',Items.bucket, 'O',"dyeRed");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,4), "SI","IW", 'S',Items.iron_sword, 'I',"ingotSteel", 'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,5), " CS","C C","IC ", 'I',componentIron, 'S',"ingotSteel", 'C',"ingotCopper");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,6), " G ","GEG","GEG", 'E',electrumCoil, 'G',"blockGlass");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,7), " SS","PPH"," SS", 'P',new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()), 'H',Blocks.hopper, 'S',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,8), "GC ","C C"," CG", 'G',"paneGlassColorless", 'C',"ingotCopper");
		addOredictRecipe(new ItemStack(IEContent.itemToolUpgrades,1,9), "WWW","HHH", 'W',new ItemStack(IEContent.itemWireCoil,1,2), 'H',new ItemStack(IEContent.blockMetalDevice0,1,2));

		addShapelessOredictRecipe(new ItemStack(IEContent.itemMetal,2,15), "dustCopper","dustNickel");
		addShapelessOredictRecipe(new ItemStack(IEContent.itemMetal,2,16), "dustSilver","dustGold");

		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,0), " I ","ISI"," I ", 'I',"wireCopper", 'S',"stickWood");
		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,1), " I ","ISI"," I ", 'I',"wireElectrum", 'S',"stickWood");
		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,2), " I ","ASA"," I ", 'I',"wireSteel", 'A',"wireAluminum", 'S',"stickWood");
		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,3), " I ","ISI"," I ", 'I',new ItemStack(IEContent.itemMaterial,1,4), 'S',"stickWood");
		addOredictRecipe(new ItemStack(IEContent.itemWireCoil,4,4), " I ","ISI"," I ", 'I',"wireSteel", 'S',"stickWood");

		addOredictRecipe(new ItemStack(IEContent.itemJerrycan), " II","IBB","IBB", 'I',"plateIron",'B',Items.bucket);
		GameRegistry.addRecipe(new RecipeJerrycan());
		RecipeSorter.register(ImmersiveEngineering.MODID+":jerrycan", RecipeJerrycan.class, RecipeSorter.Category.SHAPELESS, "after:forge:shapelessore");
		addOredictRecipe(new ItemStack(IEContent.itemToolbox), "PPP","RCR", 'P',"plateAluminum",'C',new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.CRATE.getMeta()),'R',"dyeRed");

		GameRegistry.addRecipe(new RecipeShaderBags());
		RecipeSorter.register(ImmersiveEngineering.MODID+":shaderbags", RecipeShaderBags.class, RecipeSorter.Category.SHAPELESS, "after:forge:shapelessore");

		addOredictRecipe(new ItemStack(IEContent.itemManeuverGear), " C ","GTG","WLW", 'C',new ItemStack(Items.leather_chestplate),'G',componentSteel,'T',new ItemStack(IEContent.itemToolUpgrades,1,0),'W',new ItemStack(IEContent.itemWireCoil,1,4),'L',new ItemStack(Items.leather_leggings));

		addOredictRecipe(new ItemStack(IEContent.itemEarmuffs), " S ","S S","W W", 'S',"stickIron",'W',new ItemStack(Blocks.wool,1,OreDictionary.WILDCARD_VALUE));
		GameRegistry.addRecipe(new RecipeEarmuffs());
		RecipeSorter.register(ImmersiveEngineering.MODID+":earmuffs", RecipeEarmuffs.class, RecipeSorter.Category.SHAPELESS, "after:forge:shapelessore");
		
		addOredictRecipe(new ItemStack(IEContent.itemsFaradaySuit[0]), "AAA", "A A", 'A', "plateAluminum");
		addOredictRecipe(new ItemStack(IEContent.itemsFaradaySuit[1]), "A A", "AAA", "AAA", 'A', "plateAluminum");
		addOredictRecipe(new ItemStack(IEContent.itemsFaradaySuit[2]), "AAA", "A A", "A A", 'A', "plateAluminum");
		addOredictRecipe(new ItemStack(IEContent.itemsFaradaySuit[3]), "A A", "A A", 'A', "plateAluminum");
		
		addOredictRecipe(new ItemStack(IEContent.itemFluorescentTube), "GEG", "GgG", "GgG", 'G', "blockGlass", 'E', new ItemStack(IEContent.itemGraphiteElectrode), 'g', "dustGlowstone");
		
		//
		//TREATED WOOD
		//
		for(ItemStack container : Utils.getContainersFilledWith(new FluidStack(IEContent.fluidCreosote,1000)))
			addOredictRecipe(new ItemStack(IEContent.blockTreatedWood,8,0), "WWW","WCW","WWW", 'W',"plankWood",'C',container);
		for(int i=0; i<IEContent.blockTreatedWood.enumValues.length; i++)
		{
			addShapelessOredictRecipe(new ItemStack(IEContent.blockTreatedWood,1, i==IEContent.blockTreatedWood.enumValues.length-1?0:i+1), new ItemStack(IEContent.blockTreatedWood,1,i));
			addTwoWaySlabRecipe(new ItemStack(IEContent.blockTreatedWoodSlabs,1,i), new ItemStack(IEContent.blockTreatedWood,1,i));
			addShapelessOredictRecipe(new ItemStack(IEContent.blockTreatedWoodSlabs,1, i==IEContent.blockTreatedWood.enumValues.length-1?0:i+1), new ItemStack(IEContent.blockTreatedWoodSlabs,1,i));
		}
		addStairRecipe(IEContent.blockWoodenStair, new ItemStack(IEContent.blockTreatedWood,1,0));
		addStairRecipe(IEContent.blockWoodenStair1, new ItemStack(IEContent.blockTreatedWood,1,1));
		addStairRecipe(IEContent.blockWoodenStair2, new ItemStack(IEContent.blockTreatedWood,1,2));
		GameRegistry.addShapelessRecipe(new ItemStack(IEContent.blockWoodenStair,1,0), new ItemStack(IEContent.blockWoodenStair2,1,0));
		GameRegistry.addShapelessRecipe(new ItemStack(IEContent.blockWoodenStair1,1,0), new ItemStack(IEContent.blockWoodenStair,1,0));
		GameRegistry.addShapelessRecipe(new ItemStack(IEContent.blockWoodenStair2,1,0), new ItemStack(IEContent.blockWoodenStair1,1,0));

		//
		//WOODEN DECORACTION
		//
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDecoration,3,BlockTypes_WoodenDecoration.FENCE.getMeta()), "WSW","WSW", 'W',"plankTreatedWood", 'S',"stickTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDecoration,6,BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta()), "WWW"," S ","S S", 'W',"plankTreatedWood",'S',"stickTreatedWood");

		//
		//WOODEN DEVICES
		//
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.CRATE.getMeta()), "WWW","W W","WWW", 'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.BARREL.getMeta()), "SSS","W W","WWW", 'W',"plankTreatedWood",'S',"slabTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.WORKBENCH.getMeta()), "WWW","B F", 'W',"plankTreatedWood",'B',"craftingTableWood",'F',"fenceTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.SORTER.getMeta()), "WRW","IBI","WRW", 'I',"ingotIron",'R',"dustRedstone",'W',"plankTreatedWood",'B',componentIron);
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.GUNPOWDER_BARREL.getMeta()), " F ","GBG","GGG", 'F',new ItemStack(IEContent.itemMaterial,1,4),'G',Items.gunpowder,'B',new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.BARREL.getMeta()));
		GameRegistry.addRecipe(new RecipeShapedOreNBTCopy(new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.REINFORCED_CRATE.getMeta()), 4, "WPW","RCR","WPW", 'W',"plankTreatedWood", 'P',"plateIron", 'R',"stickIron", 'C',new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.CRATE.getMeta())));
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.WATERMILL.getMeta()), " P ","PIP"," P ", 'P',new ItemStack(IEContent.itemMaterial,1,10),'I',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.WINDMILL.getMeta()), " P ","PIP"," P ", 'P',new ItemStack(IEContent.itemMaterial,1,11),'I',"ingotIron");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.WINDMILL_ADVANCED.getMeta()), "PPP","PIP","PPP", 'P',new ItemStack(IEContent.itemMaterial,1,12),'I',"ingotSteel");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.POST.getMeta()), "F","F","S", 'F',"fenceTreatedWood",'S',"bricksStone");
		addOredictRecipe(new ItemStack(IEContent.blockWoodenDevice1,4,BlockTypes_WoodenDevice1.WALLMOUNT.getMeta()), "WW","WS", 'W',"plankTreatedWood",'S',"stickTreatedWood");

		//
		//CLOTH DEVICES
		//
		addOredictRecipe(new ItemStack(IEContent.blockClothDevice,3,BlockTypes_ClothDevice.CUSHION.getMeta()), "FFF","F F","FFF", 'F',"fabricHemp");
		addOredictRecipe(new ItemStack(IEContent.blockClothDevice,2,BlockTypes_ClothDevice.BALLOON.getMeta()), " F ","FTF"," S ", 'F',"fabricHemp", 'T',"torch", 'S',"slabTreatedWood");

		//
		//STONE DECORACTION
		//
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,2,BlockTypes_StoneDecoration.COKEBRICK.getMeta()), "CBC","BSB","CBC", 'S',"sandstone",'C',Items.clay_ball,'B',"ingotBrick");
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,2,BlockTypes_StoneDecoration.BLASTBRICK.getMeta()), "NBN","BDB","NBN", 'D',Items.blaze_powder,'N',"ingotBrickNether",'B',"ingotBrick");
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.BLASTBRICK_REINFORCED.getMeta()), "P","B", 'P',"plateSteel",'B',new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.BLASTBRICK.getMeta()));
		addTwoWayStorageRecipe(new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.COKE.getMeta()), new ItemStack(IEContent.itemMaterial,1,6));
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,6,BlockTypes_StoneDecoration.HEMPCRETE.getMeta()), "CCC","HHH","CCC", 'C',Items.clay_ball,'H',new ItemStack(IEContent.itemMaterial,1,4));

		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,8,BlockTypes_StoneDecoration.CONCRETE.getMeta()), "SCS","GBG","SCS", 'C',Items.clay_ball,'S',"sand",'G',Blocks.gravel,'B',Items.water_bucket);
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,12,BlockTypes_StoneDecoration.CONCRETE.getMeta()), "SCS","GBG","SCS", 'C',Items.clay_ball,'S',"itemSlag",'G',Blocks.gravel,'B',Items.water_bucket);
		addShapelessOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE.getMeta()), new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,4,BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()), "CC","CC", 'C',new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockStoneDecoration,2,BlockTypes_StoneDecoration.INSULATING_GLASS.getMeta()), " G ","IDI"," G ", 'G',"blockGlass",'I',"dustIron",'D',"dyeGreen");
		for(int i=0; i<=BlockTypes_StoneDecoration.values().length; i++)
			if(!IEContent.blockStoneDecorationSlabs.isMetaHidden(i))
				addTwoWaySlabRecipe(new ItemStack(IEContent.blockStoneDecorationSlabs,1,i), new ItemStack(IEContent.blockStoneDecoration,1,i));
		addStairRecipe(IEContent.blockStoneStair_hempcrete, new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.HEMPCRETE.getMeta()));
		addStairRecipe(IEContent.blockStoneStair_concrete0, new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE.getMeta()));
		addStairRecipe(IEContent.blockStoneStair_concrete1, new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()));
		addStairRecipe(IEContent.blockStoneStair_concrete2, new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE_LEADED.getMeta()));
		GameRegistry.addShapelessRecipe(new ItemStack(IEContent.blockStoneStair_concrete0,1,0), new ItemStack(IEContent.blockStoneStair_concrete1,1,0));
		GameRegistry.addShapelessRecipe(new ItemStack(IEContent.blockStoneStair_concrete1,1,0), new ItemStack(IEContent.blockStoneStair_concrete0,1,0));

		//
		//METAL STORAGE
		//
		addTwoWayStorageRecipe(new ItemStack(Items.iron_ingot), new ItemStack(IEContent.itemMetal,1,29));
		for(int i=0; i<=8; i++)
		{
			addTwoWayStorageRecipe(new ItemStack(IEContent.itemMetal,1,i), new ItemStack(IEContent.itemMetal,1,20+i));
			addTwoWayStorageRecipe(new ItemStack(IEContent.blockStorage,1,i), new ItemStack(IEContent.itemMetal,1,i));
			addTwoWaySlabRecipe(new ItemStack(IEContent.blockStorageSlabs,1,i), new ItemStack(IEContent.blockStorage,1,i));
		}
		//
		//SHEETMETAL
		//
		for(int i=0; i<=Lib.METALS_ALL.length; i++)
			if(!IEContent.itemMetal.isMetaHidden(30+i))
			{
				addShapelessOredictRecipe(new ItemStack(IEContent.itemMetal,1,30+i), "ingot"+Lib.METALS_ALL[i],new ItemStack(IEContent.itemTool,1,0));
				addOredictRecipe(new ItemStack(IEContent.blockSheetmetal,4,i), " P ","P P"," P ", 'P',"plate"+Lib.METALS_ALL[i]);
				addShapelessOredictRecipe(new ItemStack(IEContent.itemMetal,1,30+i), new ItemStack(IEContent.blockSheetmetal,1,i));
				addTwoWaySlabRecipe(new ItemStack(IEContent.blockSheetmetalSlabs,1,i), new ItemStack(IEContent.blockSheetmetal,1,i));
			}

		//
		//METAL DECORACTION
		//
		addOredictRecipe(copperCoil, "WWW","WIW","WWW", 'I',"ingotIron",'W',new ItemStack(IEContent.itemWireCoil,1,0));
		addOredictRecipe(electrumCoil, "WWW","WIW","WWW", 'I',"ingotIron",'W',new ItemStack(IEContent.itemWireCoil,1,1));
		addOredictRecipe(hvCoil, "WWW","WIW","WWW", 'I',"ingotIron",'W',new ItemStack(IEContent.itemWireCoil,1,2));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()), "IRI","RCR","IRI", 'I',"ingotIron",'C',"ingotCopper",'R',"dustRedstone");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()), "IGI","CCC","IGI", 'I',"ingotIron",'C',"ingotCopper",'G',componentIron);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()), "IGI","PEP","IGI", 'I',"ingotSteel",'E',"ingotElectrum",'G',componentSteel,'P',Blocks.piston);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.GENERATOR.getMeta()), "III","EDE","III", 'I',"ingotSteel",'E',"ingotElectrum",'D',new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.DYNAMO.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.RADIATOR.getMeta()), "ICI","CBC","ICI", 'I',"ingotSteel",'C',"ingotCopper",'B',Items.water_bucket);

		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 3, BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta()), "IRI","IRI", 'I',"ingotSteel",'R',"stickSteel");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 6, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()), "III"," R ","R R", 'I',"ingotSteel",'R',"stickSteel");
		addShapelessOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()));
		addShapelessOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()));
		addShapelessOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 3, BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta()), "IRI","IRI", 'I',"ingotAluminum",'R',"stickAluminum");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 6, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()), "III"," R ","R R", 'I',"ingotAluminum",'R',"stickAluminum");
		addShapelessOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()));
		addShapelessOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()));
		addShapelessOredictRecipe(new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration2,1,BlockTypes_MetalDecoration2.STEEL_POST.getMeta()), "F","F","S", 'F',"fenceSteel",'S',"bricksStone");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration2,4,BlockTypes_MetalDecoration2.STEEL_WALLMOUNT.getMeta()), "II","IS", 'I',"ingotSteel",'S',"stickSteel");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration2,1,BlockTypes_MetalDecoration2.ALUMINUM_POST.getMeta()), "F","F","S", 'F',"fenceAluminum",'S',"bricksStone");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration2,4,BlockTypes_MetalDecoration2.ALUMINUM_WALLMOUNT.getMeta()), "II","IS", 'I',"ingotAluminum",'S',"stickAluminum");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDecoration2,3,BlockTypes_MetalDecoration2.LANTERN.getMeta()), " I ","PGP"," I ", 'G',"glowstone",'I',"plateIron",'P',"paneGlass");

		//
		//CONNECTOR
		//
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 4, BlockTypes_Connector.CONNECTOR_LV.getMeta()), " I ","BIB","BIB", 'I',"ingotCopper",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 8, BlockTypes_Connector.RELAY_LV.getMeta()), " I ","BIB", 'I',"ingotCopper",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 4, BlockTypes_Connector.CONNECTOR_MV.getMeta()), " I ","BIB","BIB", 'I',"ingotIron",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 8, BlockTypes_Connector.RELAY_MV.getMeta()), " I ","BIB", 'I',"ingotIron",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 4, BlockTypes_Connector.CONNECTOR_HV.getMeta()), " I ","BIB","BIB", 'I',"ingotAluminum",'B',Blocks.hardened_clay);
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 8, BlockTypes_Connector.RELAY_HV.getMeta()), " I ","BIB","BIB", 'I',"ingotAluminum",'B',new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.INSULATING_GLASS.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 8, BlockTypes_Connector.CONNECTOR_STRUCTURAL.getMeta()), "ISI","I I", 'I',"ingotSteel",'S',"stickSteel");
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.TRANSFORMER.getMeta()), "L M","IBI","III", 'L',new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_LV.getMeta()),'M',new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_MV.getMeta()),'I',"ingotIron",'B',electrumCoil).setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.TRANSFORMER_HV.getMeta()), "M H","IBI","III", 'H',new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_HV.getMeta()),'M',new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_MV.getMeta()),'I',"ingotIron",'B',hvCoil).setMirrored(true);
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.BREAKERSWITCH.getMeta()), " L ","CIC", 'L',Blocks.lever,'C',Blocks.hardened_clay,'I',"ingotCopper");
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.REDSTONE_BREAKER.getMeta()), "H H","ICI","IRI", 'H',new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_HV.getMeta()), 'I',"ingotIron", 'C',Items.repeater, 'R',"dustRedstone");
		addOredictRecipe(new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.ENERGY_METER.getMeta()), " M ","BCB","ICI", 'M',new ItemStack(IEContent.itemTool,1,2), 'B', Blocks.hardened_clay, 'I',"ingotIron", 'C',copperCoil);

		//
		//METAL DEVICES
		//
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice0,1, BlockTypes_MetalDevice0.CAPACITOR_LV.getMeta()), "III","CLC","WRW", 'L',"ingotLead",'I',"ingotIron",'C',"ingotCopper",'R',"dustRedstone",'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice0,1, BlockTypes_MetalDevice0.CAPACITOR_MV.getMeta()), "III","ELE","WRW", 'L',"ingotLead",'I',"ingotIron",'E',"ingotElectrum",'R',"blockRedstone",'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice0,1, BlockTypes_MetalDevice0.CAPACITOR_HV.getMeta()), "III","ALA","WRW", 'L',"blockLead",'I',"ingotSteel",'A',"ingotAluminum",'R',"blockRedstone",'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice0,1, BlockTypes_MetalDevice0.BARREL.getMeta()), "SSS","B B","BBB", 'B',"blockSheetmetalIron",'S',"slabSheetmetalIron");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice0,1, BlockTypes_MetalDevice0.FLUID_PUMP.getMeta()), " I ","ICI","PPP", 'I',"plateIron",'C',componentIron,'P',new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta()), "SSS","S S","SHS", 'S',"blockSheetmetalIron", 'H',new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta()), "ICI","CBC","IRI", 'I',"ingotIron",'R',"dustRedstone",'C',"ingotCopper",'B',copperCoil);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.DYNAMO.getMeta()), "RCR","III", 'C',copperCoil,'I',"ingotIron",'R',"dustRedstone");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.THERMOELECTRIC_GEN.getMeta()), "III","CBC","CCC", 'I',"ingotSteel",'C',"plateConstantan",'B',copperCoil);
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,3, BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta()), " I ","PGP","IRI", 'P',"paneGlass",'I',"plateIron",'G',"glowstone",'R',"dustRedstone");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.CHARGING_STATION.getMeta()), "IMI","GGG","WCW", 'M',new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_MV.getMeta()), 'I',"ingotIron", 'G',"blockGlass", 'C',copperCoil, 'W',"plankTreatedWood");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,8, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()), "PPP","   ","PPP", 'P',"plateIron");
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta()), "SFS","SFS","BFB", 'F',"fenceSteel",'S',"scaffoldingSteel",'B',new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.TESLA_COIL.getMeta()), "III"," C ","MCM", 'I',"ingotAluminum",'C',copperCoil,'M',new ItemStack(IEContent.blockMetalDevice0,1,BlockTypes_MetalDevice0.CAPACITOR_MV.getMeta()));
		addOredictRecipe(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.FLOODLIGHT.getMeta()), "III","PGC","ILI", 'I',"ingotIron",'P',"paneGlass",'G',"glowstone",'C',copperCoil,'L',componentIron);

		addOredictRecipe(new ItemStack(IEContent.blockConveyor,8, BlockTypes_Conveyor.CONVEYOR.getMeta()), "LLL","IRI", 'I',"ingotIron",'R',"dustRedstone",'L',Items.leather);
		addOredictRecipe(new ItemStack(IEContent.blockConveyor,1, BlockTypes_Conveyor.CONVEYOR_DROPPER.getMeta()), "C","H", 'C',new ItemStack(IEContent.blockConveyor,1,BlockTypes_Conveyor.CONVEYOR.getMeta()),'H',Blocks.hopper);
		addOredictRecipe(new ItemStack(IEContent.blockConveyor,3, BlockTypes_Conveyor.CONVEYOR_VERTICAL.getMeta()), "CI","C ","CI", 'C',new ItemStack(IEContent.blockConveyor,1,BlockTypes_Conveyor.CONVEYOR.getMeta()),'I',"ingotIron");


		
		//		addOredictRecipe(new ItemStack(IEContent.blockMetalMultiblocks, 1,BlockMetalMultiblocks.META_lightningRod), "IFI","CBC","IHI", 'I',"ingotSteel",'F',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_fence),'B',new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevice.META_capacitorHV),'C',electrumCoil,'H',hvCoil);
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
	public static void addTwoWaySlabRecipe(ItemStack slab, ItemStack block)
	{
		addOredictRecipe(Utils.copyStackWithAmount(slab,6), "BBB", 'B',block);
		addOredictRecipe(block, "S","S", 'S',slab);
	}
	public static void addStairRecipe(Block stair, ItemStack block)
	{
		addOredictRecipe(new ItemStack(stair,4,0), "  B"," BB","BBB", 'B',block).setMirrored(true);
	}


	public static void initBlueprintRecipes()
	{
		//
		//BULLETS
		//

		//Casull
		BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,2), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetLead","nuggetLead");
		//Piercing
		BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetSteel","nuggetSteel","nuggetConstantan","nuggetConstantan");
		if(ApiUtils.isExistingOreName("nuggetTungsten"))
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetTungsten","nuggetTungsten");
		// We don't have depleted stuff atm
		//		if(ApiUtils.isExistingOreName("nuggetCyanite"))
		//			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetCyanite","nuggetCyanite");
		//		else if(ApiUtils.isExistingOreName("ingotCyanite"))
		//			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,3,3), new ItemStack(IEContent.itemBullet,3,0),new ItemStack(Items.gunpowder,3),"ingotCyanite");
		//Silver, useless atm
		//		BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,9), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetLead","nuggetLead","nuggetSilver");
		//Buckshot
		BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,4), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder,"dustIron");
		//HE
		BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,5), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,Blocks.tnt);
		//Dragonsbreath
		BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,6), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder,"dustAluminum","dustAluminum");
		//Potion
		BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,10), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,Items.glass_bottle);
		GameRegistry.addRecipe(new RecipePotionBullets());
		RecipeSorter.register(ImmersiveEngineering.MODID+":potionBullet", RecipePotionBullets.class, RecipeSorter.Category.SHAPELESS, "after:forge:shapelessore");
		//Flare
		ItemStack flare = new ItemStack(IEContent.itemBullet,1,11);
		ItemNBTHelper.setInt(flare, "flareColour", 0xcc2e06);
		BlueprintCraftingRecipe.addRecipe("specialBullet", flare.copy(), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder,"dustAluminum","dyeRed");
		ItemNBTHelper.setInt(flare, "flareColour", 0x2ca30b);
		BlueprintCraftingRecipe.addRecipe("specialBullet", flare.copy(), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder,"dustAluminum","dyeGreen");
		ItemNBTHelper.setInt(flare, "flareColour", 0xffff82);
		BlueprintCraftingRecipe.addRecipe("specialBullet", flare.copy(), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder,"dustAluminum","dyeYellow");
		GameRegistry.addRecipe(new RecipeFlareBullets());
		RecipeSorter.register(ImmersiveEngineering.MODID+":flareBullet", RecipeFlareBullets.class, RecipeSorter.Category.SHAPELESS, "after:forge:shapelessore");

		BlueprintCraftingRecipe.addVillagerTrade("bullet", new ItemStack(Items.emerald,1,2));
		BlueprintCraftingRecipe.addVillagerTrade("specialBullet", new ItemStack(Items.emerald,1,7));

		BlueprintCraftingRecipe.addRecipe("electrode", new ItemStack(IEContent.itemGraphiteElectrode), "ingotHOPGraphite","ingotHOPGraphite","ingotHOPGraphite","ingotHOPGraphite");
		BlueprintCraftingRecipe.addVillagerTrade("electrode", new ItemStack(Items.emerald,1,18));
		int blueprint = BlueprintCraftingRecipe.blueprintCategories.indexOf("electrode");
		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(IEContent.itemBlueprint,1,blueprint), 1,1, 10));
		ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(IEContent.itemBlueprint,1,blueprint), 1,1, 2));
		if(Config.getBoolean("arcfurnace_electrodeCrafting"))
			addOredictRecipe(new ItemStack(IEContent.itemBlueprint,1,blueprint), "GGG","GDG","GPG", 'G',"ingotHOPGraphite", 'D',"dyeBlue",'P',Items.paper);
	}

	public static void initFurnaceRecipes()
	{
		//Ores
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre,1,0), new ItemStack(IEContent.itemMetal,1,0), 0.3f);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre,1,1), new ItemStack(IEContent.itemMetal,1,1), 0.3F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre,1,2), new ItemStack(IEContent.itemMetal,1,2), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre,1,3), new ItemStack(IEContent.itemMetal,1,3), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre,1,4), new ItemStack(IEContent.itemMetal,1,4), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre,1,5), new ItemStack(IEContent.itemMetal,1,5), 1.0F);
		//Dusts
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,9), new ItemStack(IEContent.itemMetal,1,0), 0.3F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,10), new ItemStack(IEContent.itemMetal,1,1), 0.3F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,11), new ItemStack(IEContent.itemMetal,1,2), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,12), new ItemStack(IEContent.itemMetal,1,3), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,13), new ItemStack(IEContent.itemMetal,1,4), 1F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,14), new ItemStack(IEContent.itemMetal,1,5), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,15), new ItemStack(IEContent.itemMetal,1,6), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,16), new ItemStack(IEContent.itemMetal,1,7), 1F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,17), new ItemStack(IEContent.itemMetal,1,8), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,18), new ItemStack(Items.iron_ingot), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal,1,19), new ItemStack(Items.gold_ingot), 1.0F);

		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMaterial,1,18), new ItemStack(IEContent.itemMaterial,1,19), 0.5F);
	}

	public static HashMap<String, ItemStack> oreOutputModifier = new HashMap<String, ItemStack>();
	public static HashMap<String, Object[]> oreOutputSecondaries = new HashMap<String, Object[]>();
	public static ArrayList<String> hammerCrushingList = new ArrayList<String>();
	public static void initCrusherRecipes()
	{
		oreOutputSecondaries.put("Iron", new Object[]{"dustNickel",.1f});
		oreOutputSecondaries.put("Gold", new Object[]{"crystalCinnabar",.05f});
		oreOutputSecondaries.put("Copper", new Object[]{"dustGold",.1f});
		oreOutputSecondaries.put("Lead", new Object[]{"dustSilver",.1f});
		oreOutputSecondaries.put("Silver", new Object[]{"dustLead",.1f});
		oreOutputSecondaries.put("Nickel", new Object[]{"dustPlatinum",.1f});

		//		addCrusherRecipe(new ItemStack(IEContent.itemMetal,1,15), "ingotConstantan", 3600, null,0);
		//		addCrusherRecipe(new ItemStack(IEContent.itemMetal,1,16), "ingotElectrum", 3600, null,0);
		//		addCrusherRecipe(new ItemStack(IEContent.itemMetal,1,19), "ingotHOPGraphite", 3600, null,0);
		//		CrusherRecipe.addRecipe(new ItemStack(IEContent.itemMetal,1,17), "fuelCoke", 4800);
		//		CrusherRecipe.addRecipe(new ItemStack(IEContent.itemMetal,1,18), "gemQuartz", 4800);

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

		addCrusherRecipe(new ItemStack(Blocks.gravel), "cobblestone", 1600);
		addCrusherRecipe(new ItemStack(Blocks.sand), Blocks.gravel, 1600);
		addCrusherRecipe(new ItemStack(Blocks.sand), "blockGlass", 3200);
		addCrusherRecipe(new ItemStack(Items.quartz,4), "blockQuartz", 3200);
		addCrusherRecipe(new ItemStack(Items.glowstone_dust,4), "glowstone", 3200);
		addCrusherRecipe(new ItemStack(Items.blaze_powder,4), "rodBlaze", 3200, "dustSulfur",.5f);
		addCrusherRecipe(new ItemStack(Items.dye,6,15), Items.bone, 3200);
		addCrusherRecipe(new ItemStack(IEContent.itemMaterial,1,17), "fuelCoke", 2400);
		addItemToOreDictCrusherRecipe("dustCoal",1, new ItemStack(Items.coal), 2400);
		addItemToOreDictCrusherRecipe("dustObsidian",4, Blocks.obsidian, 6000);
		for(int i=0; i<16; i++)
		{
			CrusherRecipe r = CrusherRecipe.addRecipe(new ItemStack(Items.string,4), new ItemStack(Blocks.wool,1,i), 3200);
			if(i!=0)
				r.addToSecondaryOutput(new ItemStack(Items.dye,1,15-i), .05f);
		}
	}
	public static void postInitOreDictRecipes()
	{
		boolean allowHammerCrushing = !Config.getBoolean("disableHammerCrushing");
		ArrayListMultimap<String, ItemStack> registeredMoldBases = ArrayListMultimap.create();
		for(String name : OreDictionary.getOreNames())
			if(ApiUtils.isExistingOreName(name))
				if(name.startsWith("ore"))
				{
					String ore = name.substring("ore".length());
					ItemStack out = oreOutputModifier.get(ore);
					if(out==null)
					{
						if(ApiUtils.isExistingOreName("gem"+ore))
							out = Utils.copyStackWithAmount(IEApi.getPreferredOreStack("gem"+ore), 2);
						else
						{
							if(ApiUtils.isExistingOreName("dust"+ore))
							{
								ItemStack preferredDust = IEApi.getPreferredOreStack("dust"+ore);
								out = Utils.copyStackWithAmount(preferredDust, 2);
								if(allowHammerCrushing)
								{
									addShapelessOredictRecipe(preferredDust, name,new ItemStack(IEContent.itemTool));
									hammerCrushingList.add(ore);
								}
							}
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
						if(ApiUtils.isExistingOreName("ingot"+ore))
							out = Utils.copyStackWithAmount(IEApi.getPreferredOreStack("ingot"+ore),2);
					}
					if(out!=null && !arcBlacklist.contains(ore))
						addArcOreSmelting(out, ore);
				}
				else if(name.startsWith("gem"))
				{
					String ore = name.substring("gem".length());
					if(ApiUtils.isExistingOreName("dust"+ore))
						addCrusherRecipe(IEApi.getPreferredOreStack("dust"+ore), "gem"+ore, 6000, null,0);
				}
				else if(name.startsWith("dust"))
				{
					String ore = name.substring("dust".length());
					ItemStack out = arcOutputModifier.get(ore);
					if(out==null)
					{
						if(ApiUtils.isExistingOreName("ingot"+ore))
							out = IEApi.getPreferredOreStack("ingot"+ore);
					}
					else
						out = Utils.copyStackWithAmount(out, out.stackSize/2);
					if(out!=null && !arcBlacklist.contains(ore))
						addArcRecipe(out, "dust"+ore, 100,512, null);
					if(ApiUtils.isExistingOreName("ingot"+ore))
						addCrusherRecipe(IEApi.getPreferredOreStack("dust"+ore), "ingot"+ore, 3600, null,0);
				}
				else if(name.startsWith("plate"))
				{
					String ore = name.substring("plate".length());
					if(ApiUtils.isExistingOreName("ingot"+ore))
					{
						registeredMoldBases.putAll("plate",OreDictionary.getOres(name));
						MetalPressRecipe.addRecipe(IEApi.getPreferredOreStack(name), "ingot"+ore, new ItemStack(IEContent.itemMold,1,0), 2400);
					}
				}
				else if(name.startsWith("gear"))
				{
					IEContent.itemMold.setMetaUnhidden(1);
					String ore = name.substring("gear".length());
					if(ApiUtils.isExistingOreName("ingot"+ore))
					{
						registeredMoldBases.putAll("gear",OreDictionary.getOres(name));
						MetalPressRecipe.addRecipe(IEApi.getPreferredOreStack(name), "ingot"+ore, new ItemStack(IEContent.itemMold,1,1), 2400).setInputSize(4);
					}
				}
				else if(name.startsWith("stick")||name.startsWith("rod"))
				{
					String ore = name.startsWith("stick")?name.substring("stick".length()):name.substring("rod".length());
					if(ApiUtils.isExistingOreName("ingot"+ore))
					{
						registeredMoldBases.putAll("rod",OreDictionary.getOres(name));
						MetalPressRecipe.addRecipe(Utils.copyStackWithAmount(IEApi.getPreferredOreStack(name),2), "ingot"+ore, new ItemStack(IEContent.itemMold,1,2), 2400);
					}
				}
				else if(name.startsWith("wire"))
				{
					String ore = name.substring("wire".length());
					if(ApiUtils.isExistingOreName("ingot"+ore))
					{
						registeredMoldBases.putAll("wire",OreDictionary.getOres(name));
						MetalPressRecipe.addRecipe(Utils.copyStackWithAmount(IEApi.getPreferredOreStack(name),2), "ingot"+ore, new ItemStack(IEContent.itemMold,1,4), 2400);
					}
				}
		if(registeredMoldBases.containsKey("plate"))
			GameRegistry.addRecipe(new RecipeShapedArrayList(new ItemStack(IEContent.itemMold,1,0), " P ","PCP"," P ", 'P',"plateSteel",'C',registeredMoldBases.get("plate")));
		if(registeredMoldBases.containsKey("gear"))
			GameRegistry.addRecipe(new RecipeShapedArrayList(new ItemStack(IEContent.itemMold,1,1), " P ","PCP"," P ", 'P',"plateSteel",'C',registeredMoldBases.get("gear")));
		if(registeredMoldBases.containsKey("rod"))
			GameRegistry.addRecipe(new RecipeShapedArrayList(new ItemStack(IEContent.itemMold,1,2), " P ","PCP"," P ", 'P',"plateSteel",'C',registeredMoldBases.get("rod")));
		if(registeredMoldBases.containsKey("wire"))
			GameRegistry.addRecipe(new RecipeShapedArrayList(new ItemStack(IEContent.itemMold,1,4), " P ","PCP"," P ", 'P',"plateSteel",'C',registeredMoldBases.get("wire")));
		Config.setBoolean("crushingOreRecipe", !hammerCrushingList.isEmpty());
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
		if(ingot && ApiUtils.isExistingOreName("ingot"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(output, output.stackSize/2), "ingot"+ore, (int)(energy*.6f));
		if(ApiUtils.isExistingOreName("ore"+ore))
			addCrusherRecipe(output, "ore"+ore, energy, secondary,secChance);
		//		if(ApiUtils.isExistingOreName("oreNether"+ore))
		//			addCrusherRecipe(Utils.copyStackWithAmount(output, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, energy, secondary,secChance,Blocks.netherrack,.15f);

		//YAY GregTech!
		if(ApiUtils.isExistingOreName("oreNetherrack"+ore))
			addCrusherRecipe(output, "oreNetherrack"+ore, energy, secondary,secChance, new ItemStack(Blocks.netherrack),.15f);
		if(ApiUtils.isExistingOreName("oreEndstone"+ore))
			addCrusherRecipe(output, "oreEndstone"+ore, energy, secondary,secChance, "dustEndstone",.5f);
		if(ApiUtils.isExistingOreName("oreBlackgranite"+ore))
			addCrusherRecipe(output, "oreBlackgranite"+ore, energy, secondary,secChance, "dustGraniteBlack",.5f);
		if(ApiUtils.isExistingOreName("oreRedgranite"+ore))
			addCrusherRecipe(output, "oreRedgranite"+ore, energy, secondary,secChance, "dustGraniteBlack",.5f);
	}
	public static void addOreDictCrusherRecipe(String ore, Object secondary, float chance)
	{
		if(!ApiUtils.isExistingOreName("dust"+ore))
			return;
		ItemStack dust = IEApi.getPreferredOreStack("dust"+ore);
		if(dust==null)
			return;
		if(ApiUtils.isExistingOreName("ore"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "ore"+ore, 6000, secondary,chance);
		if(ApiUtils.isExistingOreName("ingot"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 1), "ingot"+ore, 3600);
		//		if(ApiUtils.isExistingOreName("oreNether"+ore))
		//			addCrusherRecipe(Utils.copyStackWithAmount(dust, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, 6000, secondary,chance,Blocks.netherrack,.15f);

		//YAY GregTech!
		if(ApiUtils.isExistingOreName("oreNetherrack"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreNetherrack"+ore, 6000, secondary,chance, new ItemStack(Blocks.netherrack),.15f);
		if(ApiUtils.isExistingOreName("oreEndstone"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreEndstone"+ore, 6000, secondary,chance, "dustEndstone",.5f);
		if(ApiUtils.isExistingOreName("oreBlackgranite"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreBlackgranite"+ore, 6000, secondary,chance, "dustGraniteBlack",.5f);
		if(ApiUtils.isExistingOreName("oreRedgranite"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreRedgranite"+ore, 6000, secondary,chance, "dustGraniteRed",.5f);
	}
	public static CrusherRecipe addItemToOreDictCrusherRecipe(String oreName, int outSize, Object input, int energy)
	{
		if(!ApiUtils.isExistingOreName(oreName))
			return null;
		ItemStack out = IEApi.getPreferredOreStack(oreName);
		if(out==null)
			return null;
		return CrusherRecipe.addRecipe(Utils.copyStackWithAmount(out, outSize), input, energy);
	}


	public static HashMap<String, ItemStack> arcOutputModifier = new HashMap<String, ItemStack>();
	public static HashSet<String> arcBlacklist = new HashSet<String>();
	public static void initArcSmeltingRecipes()
	{
		//Steel
		ArcFurnaceRecipe.addRecipe(new ItemStack(IEContent.itemMetal,1,8), "ingotIron", new ItemStack(IEContent.itemMaterial,1,7), 400,512, "dustCoke");
		ArcFurnaceRecipe.addRecipe(new ItemStack(IEContent.itemMetal,1,8), "dustIron", new ItemStack(IEContent.itemMaterial,1,7), 400,512, "dustCoke");
		//Vanilla+IE Ores
		arcOutputModifier.put("Iron", new ItemStack(Items.iron_ingot,2));
		arcOutputModifier.put("Gold", new ItemStack(Items.gold_ingot,2));
		arcOutputModifier.put("Copper", new ItemStack(IEContent.itemMetal,2,0));
		arcOutputModifier.put("Aluminum", new ItemStack(IEContent.itemMetal,2,1));
		arcOutputModifier.put("Aluminium", new ItemStack(IEContent.itemMetal,2,1));
		arcOutputModifier.put("Lead", new ItemStack(IEContent.itemMetal,2,2));
		arcOutputModifier.put("Silver", new ItemStack(IEContent.itemMetal,2,3));
		arcOutputModifier.put("Nickel", new ItemStack(IEContent.itemMetal,2,4));
		//IE Alloys
		addOreDictAlloyingRecipe(new ItemStack(IEContent.itemMetal,1,6), "Copper", 100,512, "dustNickel");
		addOreDictAlloyingRecipe(new ItemStack(IEContent.itemMetal,1,6), "Nickel", 100,512, "dustCopper");
		addOreDictAlloyingRecipe(new ItemStack(IEContent.itemMetal,1,7), "Gold", 100,512, "dustSilver");
		addOreDictAlloyingRecipe(new ItemStack(IEContent.itemMetal,1,7), "Silver", 100,512, "dustGold");
		//Common Alloys
		addOreDictAlloyingRecipe("ingotInvar",3, "Nickel", 200,512, "dustIron","dustIron");
		addOreDictAlloyingRecipe("ingotBronze",4, "Tin", 200,512, "dustCopper","dustCopper","dustCopper");

		addOreDictAlloyingRecipe("ingotBrass",4, "Zinc", 200,512, "dustCopper","dustCopper","dustCopper");
		addOreDictAlloyingRecipe("ingotBlueAlloy",1, "Silver", 100,512, "dustNikolite","dustNikolite","dustNikolite","dustNikolite");
		addOreDictAlloyingRecipe("ingotRedAlloy",1, "Copper", 100,512, "dustRedstone","dustRedstone","dustRedstone","dustRedstone");

		//Recycling
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration0,1,OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration1,1,OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration2,1,OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDevice0,1,OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDevice1,1,OreDictionary.WILDCARD_VALUE));

		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.itemWireCoil,1,OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.itemDrillhead,1,OreDictionary.WILDCARD_VALUE));
	}

	public static ArcFurnaceRecipe addArcRecipe(ItemStack output, Object input, int time, int energyPerTick, ItemStack slag, Object... additives)
	{
		return ArcFurnaceRecipe.addRecipe(output, input, slag, time, energyPerTick, additives);
	}
	public static void addArcOreSmelting(ItemStack output, String ore)
	{
		if(ApiUtils.isExistingOreName("ore"+ore))
			addArcRecipe(output, "ore"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,7)).setSpecialRecipeType("Ores");
		//		if(ApiUtils.isExistingOreName("oreNether"+ore))
		//			addArcRecipe(Utils.copyStackWithAmount(output, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,13)).setSpecialRecipeType("Ores");

		//YAY GregTech!
		if(ApiUtils.isExistingOreName("oreNetherrack"+ore))
			addArcRecipe(output, "oreNetherrack"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,7)).setSpecialRecipeType("Ores");
		if(ApiUtils.isExistingOreName("oreEndstone"+ore))
			addArcRecipe(output, "oreEndstone"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,7)).setSpecialRecipeType("Ores");
		if(ApiUtils.isExistingOreName("oreBlackgranite"+ore))
			addArcRecipe(output, "oreBlackgranite"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,7)).setSpecialRecipeType("Ores");
		if(ApiUtils.isExistingOreName("oreRedgranite"+ore))
			addArcRecipe(output, "oreRedgranite"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,7)).setSpecialRecipeType("Ores");
	}
	public static void addOreDictAlloyingRecipe(String outName, int outSize, String inputName, int time, int energyPerTick, Object... additives)
	{
		if(!ApiUtils.isExistingOreName(outName))
			return;
		ItemStack out = IEApi.getPreferredOreStack(outName);
		if(out==null)
			return;
		addOreDictAlloyingRecipe(Utils.copyStackWithAmount(out, outSize), inputName, time,energyPerTick, additives);
	}
	public static void addOreDictAlloyingRecipe(ItemStack out, String inputName, int time, int energyPerTick, Object... additives)
	{
		if(ApiUtils.isExistingOreName("ingot"+inputName))
			ArcFurnaceRecipe.addRecipe(out, "ingot"+inputName, null, time, energyPerTick, additives).setSpecialRecipeType("Alloying");
		if(ApiUtils.isExistingOreName("dust"+inputName))
			ArcFurnaceRecipe.addRecipe(out, "dust"+inputName, null, time, energyPerTick, additives).setSpecialRecipeType("Alloying");
	}
}