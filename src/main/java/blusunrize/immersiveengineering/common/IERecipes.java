/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.cloth.BlockTypes_ClothDevice;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.crafting.*;
import blusunrize.immersiveengineering.common.items.ItemGraphiteElectrode;
import blusunrize.immersiveengineering.common.items.ItemIEBase;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class IERecipes
{

	public static void initCraftingRecipes(IForgeRegistry<IRecipe> registry)
	{
		//Recipe Sorter is deprecated apparently
		//RecipeSorter.register(ImmersiveEngineering.MODID+":shapedIngredient", RecipeShapedIngredient.class, Category.SHAPED, "after:forge:shapedore");
		//RecipeSorter.register(ImmersiveEngineering.MODID+":shapelessIngredient", RecipeShapelessIngredient.class, Category.SHAPELESS, "after:forge:shapedore");
		//RecipeSorter.register(ImmersiveEngineering.MODID+":banners", RecipeBannerAdvanced.class, Category.SHAPELESS, "after:forge:shapelessore");
		//RecipeSorter.register(ImmersiveEngineering.MODID+":RGBColour", RecipeRGBColouration.class, Category.SHAPELESS, "after:forge:shapelessore");
		//RecipeSorter.register(ImmersiveEngineering.MODID+":revolverLoop", RecipeRevolver.class, Category.SHAPELESS, "after:forge:shapelessore");
		//RecipeSorter.register(ImmersiveEngineering.MODID+":jerrycan", RecipeJerrycan.class, Category.SHAPELESS, "after:forge:shapelessore");
		//RecipeSorter.register(ImmersiveEngineering.MODID+":shaderbags", RecipeShaderBags.class, Category.SHAPELESS, "after:forge:shapelessore");
		//RecipeSorter.register(ImmersiveEngineering.MODID+":earmuffs", RecipeEarmuffs.class, Category.SHAPELESS, "after:forge:shapelessore");
		//RecipeSorter.register(ImmersiveEngineering.MODID+":powerpack", RecipePowerpack.class, Category.SHAPELESS, "after:forge:shapelessore");

		//Loop, special or colouration recipes
		registry.register(new RecipeBannerAdvanced().setRegistryName(ImmersiveEngineering.MODID, "banners"));
		registry.register(new RecipeRevolver().setRegistryName(ImmersiveEngineering.MODID, "revolver_loop"));
		registry.register(new RecipeSpeeloader().setRegistryName(ImmersiveEngineering.MODID, "speedloader_load"));
		registry.register(new RecipeJerrycan().setRegistryName(ImmersiveEngineering.MODID, "jerrycan"));
		registry.register(new RecipeShaderBags().setRegistryName(ImmersiveEngineering.MODID, "shader_bags"));
		registry.register(new RecipeEarmuffs().setRegistryName(ImmersiveEngineering.MODID, "earmuffs"));
		registry.register(new RecipePowerpack().setRegistryName(ImmersiveEngineering.MODID, "powerpack"));
		final ItemStack stripCurtain = new ItemStack(IEContent.blockClothDevice, 1, BlockTypes_ClothDevice.STRIPCURTAIN.getMeta());
		registry.register(new RecipeRGBColouration((s) -> (OreDictionary.itemMatches(stripCurtain, s, true)), (s) -> (ItemNBTHelper.hasKey(s, "colour")?ItemNBTHelper.getInt(s, "colour"): 0xffffff), (s, i) -> ItemNBTHelper.setInt(s, "colour", i)).setRegistryName(ImmersiveEngineering.MODID, "stripcurtain_colour"));
	}

	public static void addShapelessOredictRecipe(String registryName, ItemStack output, Object... recipe)
	{
		ShapelessOreRecipe sor = new ShapelessOreRecipe(null, output, recipe);
		if(registryName==null)
		{
			registryName = ImmersiveEngineering.MODID+":";
			if(output.getItem() instanceof ItemIEBase)
				registryName += ((ItemIEBase)output.getItem()).itemName+"_"+((ItemIEBase)output.getItem()).getSubNames()[output.getMetadata()]+"*"+output.getCount();
			else
			{
				int idx = output.getTranslationKey().lastIndexOf(":");
				registryName += output.getTranslationKey().substring(idx < 0?0: idx)+"_"+output.getMetadata()+"*"+output.getCount();
			}
		}
		else if(!registryName.startsWith(ImmersiveEngineering.MODID))
			registryName = ImmersiveEngineering.MODID+":"+registryName;
		ForgeRegistries.RECIPES.register(sor.setRegistryName(registryName));
	}

	public static void initBlueprintRecipes()
	{
		//
		//MATERIALS
		//
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(IEContent.itemMaterial, 1, 8), "plateIron", "plateIron", "ingotCopper");
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(IEContent.itemMaterial, 1, 9), "plateSteel", "plateSteel", "ingotCopper");
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(IEContent.itemMaterial, 3, 26), "blockGlass", "plateNickel", "wireCopper", "dustRedstone");
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(IEContent.itemMaterial, 1, 27), new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.INSULATING_GLASS.getMeta()), "plateCopper", "electronTube", "electronTube");

		//
		//MOLDS
		//
		for(int i = 0; i < IEContent.itemMold.getSubNames().length; i++)
			if(!IEContent.itemMold.isMetaHidden(i))
				BlueprintCraftingRecipe.addRecipe("molds", new ItemStack(IEContent.itemMold, 1, i), "plateSteel", "plateSteel", "plateSteel", "plateSteel", "plateSteel", new ItemStack(IEContent.itemTool, 1, 1));

		//
		//BULLETS
		//
		//Casull
		ItemStack bullet = BulletHandler.getBulletStack("casull");
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, "nuggetLead", "nuggetLead");
		//Piercing
		bullet = BulletHandler.getBulletStack("armorPiercing");
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, "nuggetSteel", "nuggetSteel", "nuggetConstantan", "nuggetConstantan");
		if(ApiUtils.isExistingOreName("nuggetTungsten"))
			BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, "nuggetTungsten", "nuggetTungsten");
		// We don't have depleted stuff atm
		//		if(ApiUtils.isExistingOreName("nuggetCyanite"))
		//			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetCyanite","nuggetCyanite");
		//		else if(ApiUtils.isExistingOreName("ingotCyanite"))
		//			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,3,3), new ItemStack(IEContent.itemBullet,3,0),new ItemStack(Items.gunpowder,3),"ingotCyanite");
		//Silver
		bullet = BulletHandler.getBulletStack("silver");
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, "nuggetLead", "nuggetLead", "nuggetSilver");
		//Buckshot
		bullet = BulletHandler.getBulletStack("buckshot");
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(IEContent.itemBullet, 1, 1), Items.GUNPOWDER, "dustIron");
		//HE
		bullet = BulletHandler.getBulletStack("HE");
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, Blocks.TNT);
		//Dragonsbreath
		bullet = BulletHandler.getBulletStack("dragonsbreath");
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet, new ItemStack(IEContent.itemBullet, 1, 1), Items.GUNPOWDER, "dustAluminum", "dustAluminum");
		//Potion
		bullet = BulletHandler.getBulletStack("potion");
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet, new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, Items.GLASS_BOTTLE);
		ForgeRegistries.RECIPES.register(new RecipePotionBullets().setRegistryName(ImmersiveEngineering.MODID, "bullet_potion"));
		//RecipeSorter.register(ImmersiveEngineering.MODID+":potionBullet", RecipePotionBullets.class, Category.SHAPELESS, "after:forge:shapelessore");
		//Flare
		bullet = BulletHandler.getBulletStack("flare");
		ItemNBTHelper.setInt(bullet, "flareColour", 0xcc2e06);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(IEContent.itemBullet, 1, 1), Items.GUNPOWDER, "dustAluminum", "dyeRed");
		ItemNBTHelper.setInt(bullet, "flareColour", 0x2ca30b);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(IEContent.itemBullet, 1, 1), Items.GUNPOWDER, "dustAluminum", "dyeGreen");
		ItemNBTHelper.setInt(bullet, "flareColour", 0xffff82);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(IEContent.itemBullet, 1, 1), Items.GUNPOWDER, "dustAluminum", "dyeYellow");
		ForgeRegistries.RECIPES.register(new RecipeFlareBullets().setRegistryName(ImmersiveEngineering.MODID, "potion_flare"));
		//RecipeSorter.register(ImmersiveEngineering.MODID+":flareBullet", RecipeFlareBullets.class, Category.SHAPELESS, "after:forge:shapelessore");

		//Wolfpack
		if(!BulletHandler.homingCartridges.isEmpty())
		{
			bullet = BulletHandler.getBulletStack("wolfpack");
			ArrayList<ItemStack> homingCartridges = new ArrayList();
			for(String s : BulletHandler.homingCartridges)
				homingCartridges.add(BulletHandler.getBulletStack(s));
			BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(IEContent.itemBullet, 1, 1), Items.GUNPOWDER, homingCartridges, homingCartridges, homingCartridges, homingCartridges);
		}

		BlueprintCraftingRecipe.addVillagerTrade("bullet", new ItemStack(Items.EMERALD, 1, 2));
		BlueprintCraftingRecipe.addVillagerTrade("specialBullet", new ItemStack(Items.EMERALD, 1, 7));

		BlueprintCraftingRecipe.addRecipe("electrode", new ItemStack(IEContent.itemGraphiteElectrode), "ingotHOPGraphite", "ingotHOPGraphite", "ingotHOPGraphite", "ingotHOPGraphite");
		BlueprintCraftingRecipe.addVillagerTrade("electrode", new ItemStack(Items.EMERALD, 1, 18));
	}

	public static void initFurnaceRecipes()
	{
		//Ores
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre, 1, 0), new ItemStack(IEContent.itemMetal, 1, 0), 0.3f);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre, 1, 1), new ItemStack(IEContent.itemMetal, 1, 1), 0.3F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre, 1, 2), new ItemStack(IEContent.itemMetal, 1, 2), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre, 1, 3), new ItemStack(IEContent.itemMetal, 1, 3), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre, 1, 4), new ItemStack(IEContent.itemMetal, 1, 4), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.blockOre, 1, 5), new ItemStack(IEContent.itemMetal, 1, 5), 1.0F);
		//Dusts
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 9), new ItemStack(IEContent.itemMetal, 1, 0), 0.3F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 10), new ItemStack(IEContent.itemMetal, 1, 1), 0.3F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 11), new ItemStack(IEContent.itemMetal, 1, 2), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 12), new ItemStack(IEContent.itemMetal, 1, 3), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 13), new ItemStack(IEContent.itemMetal, 1, 4), 1F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 14), new ItemStack(IEContent.itemMetal, 1, 5), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 15), new ItemStack(IEContent.itemMetal, 1, 6), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 16), new ItemStack(IEContent.itemMetal, 1, 7), 1F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 17), new ItemStack(IEContent.itemMetal, 1, 8), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 18), new ItemStack(Items.IRON_INGOT), 0.7F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMetal, 1, 19), new ItemStack(Items.GOLD_INGOT), 1.0F);

		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(IEContent.itemMaterial, 1, 18), new ItemStack(IEContent.itemMaterial, 1, 19), 0.5F);
	}

	public static void initBlastFurnaceRecipes()
	{
		BlastFurnaceRecipe.addRecipe(new ItemStack(IEContent.itemMetal, 1, 8), "ingotIron", 1200, new ItemStack(IEContent.itemMaterial, 1, 7));
		BlastFurnaceRecipe.addRecipe(new ItemStack(IEContent.blockStorage, 1, 8), "blockIron", 1200*9, new ItemStack(IEContent.itemMaterial, 9, 7));

		BlastFurnaceRecipe.addBlastFuel("fuelCoke", 1200);
		BlastFurnaceRecipe.addBlastFuel("blockFuelCoke", 1200*10);
		BlastFurnaceRecipe.addBlastFuel("charcoal", 300);
		BlastFurnaceRecipe.addBlastFuel("blockCharcoal", 300*10);
	}

	public static void initMetalPressRecipes()
	{
		//Bullet casing
		MetalPressRecipe.addRecipe(new ItemStack(IEContent.itemBullet, 2, 0), "ingotCopper", new ItemStack(IEContent.itemMold, 1, 3), 2400);

		//Damaged Graphite Electrodes
		ItemStack shoddyElectrode = new ItemStack(IEContent.itemGraphiteElectrode);
		shoddyElectrode.setItemDamage(ItemGraphiteElectrode.electrodeMaxDamage/2);
		MetalPressRecipe.addRecipe(shoddyElectrode, "ingotHOPGraphite", new ItemStack(IEContent.itemMold, 1, 2), 4800).setInputSize(4);

		//Slicing Melons
		MetalPressRecipe.addRecipe(new ItemStack(Items.MELON, 9), new ItemStack(Blocks.MELON_BLOCK), new ItemStack(IEContent.itemMold, 1, 7), 3200);

		//Packing & Unpacking
		ComparableItemStack mold = ApiUtils.createComparableItemStack(new ItemStack(IEContent.itemMold, 1, 5), false);
		MetalPressRecipe.recipeList.put(mold, new MetalPressPackingRecipe(mold, 3200, 2));
		mold = ApiUtils.createComparableItemStack(new ItemStack(IEContent.itemMold, 1, 6), false);
		MetalPressRecipe.recipeList.put(mold, new MetalPressPackingRecipe(mold, 3200, 3));
		mold = ApiUtils.createComparableItemStack(new ItemStack(IEContent.itemMold, 1, 7), false);
		MetalPressRecipe.recipeList.put(mold, new MetalPressUnpackingRecipe(mold, 3200));
	}

	public static HashMap<String, ItemStack> oreOutputModifier = new HashMap<String, ItemStack>();
	public static HashMap<String, Object[]> oreOutputSecondaries = new HashMap<String, Object[]>();
	public static ArrayList<String> hammerCrushingList = new ArrayList<String>();

	public static void initCrusherRecipes()
	{
		oreOutputSecondaries.put("Iron", new Object[]{"dustNickel", .1f});
		oreOutputSecondaries.put("Gold", new Object[]{"crystalCinnabar", .05f});
		oreOutputSecondaries.put("Copper", new Object[]{"dustGold", .1f});
		oreOutputSecondaries.put("Lead", new Object[]{"dustSilver", .1f});
		oreOutputSecondaries.put("Silver", new Object[]{"dustLead", .1f});
		oreOutputSecondaries.put("Nickel", new Object[]{"dustPlatinum", .1f});

		oreOutputModifier.put("Lapis", new ItemStack(Items.DYE, 9, 4));
		oreOutputSecondaries.put("Lapis", new Object[]{"dustSulfur", .15f});
		oreOutputModifier.put("Diamond", new ItemStack(Items.DIAMOND, 2));
		oreOutputModifier.put("Redstone", new ItemStack(Items.REDSTONE, 6));
		oreOutputSecondaries.put("Redstone", new Object[]{"crystalCinnabar", .25f});
		oreOutputModifier.put("Emerald", new ItemStack(Items.EMERALD, 2));
		oreOutputModifier.put("Quartz", new ItemStack(Items.QUARTZ, 3));
		oreOutputSecondaries.put("Quartz", new Object[]{"dustSulfur", .15f});
		oreOutputModifier.put("Coal", new ItemStack(Items.COAL, 4));

		oreOutputSecondaries.put("Platinum", new Object[]{"dustNickel", .1f});
		oreOutputSecondaries.put("Tungsten", new Object[]{"dustManganese", .1f});
		oreOutputSecondaries.put("Uranium", new Object[]{"dustLead", .1f});
		oreOutputSecondaries.put("Yellorium", new Object[]{"dustLead", .1f});
		oreOutputSecondaries.put("Plutonium", new Object[]{"dustUranium", .1f});
		Item item = GameRegistry.findRegistry(Item.class).getValue(new ResourceLocation("IC2", "itemOreIridium"));
		oreOutputSecondaries.put("Osmium", new Object[]{item, .01f});
		oreOutputSecondaries.put("Iridium", new Object[]{"dustPlatium", .1f});
		oreOutputSecondaries.put("FzDarkIron", new Object[]{"dustIron", .1f});
		item = GameRegistry.findRegistry(Item.class).getValue(new ResourceLocation("Railcraft", "firestone.raw"));
		if(item!=null)
			oreOutputModifier.put("Firestone", new ItemStack(item));
		oreOutputSecondaries.put("Nikolite", new Object[]{Items.DIAMOND, .025f});

		addCrusherRecipe(new ItemStack(Blocks.GRAVEL), "cobblestone", 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), Blocks.GRAVEL, 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), "itemSlag", 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), "blockGlass", 3200);
		addCrusherRecipe(new ItemStack(Blocks.SAND, 2), "sandstone", 1600, new ItemStack(IEContent.itemMaterial, 1, 24), .5f);
		addCrusherRecipe(new ItemStack(Items.CLAY_BALL, 4), "blockClay", 1600);
		addCrusherRecipe(new ItemStack(Items.QUARTZ, 4), "blockQuartz", 3200);
		addCrusherRecipe(new ItemStack(Items.GLOWSTONE_DUST, 4), "glowstone", 3200);
		addCrusherRecipe(new ItemStack(Items.BLAZE_POWDER, 4), "rodBlaze", 3200, new ItemStack(IEContent.itemMaterial, 1, 25), .5f);
		addCrusherRecipe(new ItemStack(Items.DYE, 6, 15), Items.BONE, 3200);
		addCrusherRecipe(new ItemStack(IEContent.itemMaterial, 1, 17), "fuelCoke", 2400);
		addCrusherRecipe(new ItemStack(IEContent.itemMaterial, 9, 17), "blockFuelCoke", 4800);
		addItemToOreDictCrusherRecipe("dustCoal", 1, new ItemStack(Items.COAL), 2400);
		addItemToOreDictCrusherRecipe("dustObsidian", 4, Blocks.OBSIDIAN, 6000);
		for(int i = 0; i < 16; i++)
		{
			CrusherRecipe r = CrusherRecipe.addRecipe(new ItemStack(Items.STRING, 4), new ItemStack(Blocks.WOOL, 1, i), 3200);
			if(i!=0)
				r.addToSecondaryOutput(new ItemStack(Items.DYE, 1, 15-i), .05f);
		}
	}

	public static void postInitOreDictRecipes()
	{
		boolean allowHammerCrushing = !IEConfig.Tools.disableHammerCrushing;
		ComparableItemStack compMoldPlate = ApiUtils.createComparableItemStack(new ItemStack(IEContent.itemMold, 1, 0), false);
		ComparableItemStack compMoldGear = ApiUtils.createComparableItemStack(new ItemStack(IEContent.itemMold, 1, 1), false);
		ComparableItemStack compMoldRod = ApiUtils.createComparableItemStack(new ItemStack(IEContent.itemMold, 1, 2), false);
		ComparableItemStack compMoldWire = ApiUtils.createComparableItemStack(new ItemStack(IEContent.itemMold, 1, 4), false);

		for(String name : OreDictionary.getOreNames())
			if(ApiUtils.isExistingOreName(name))
				if(name.startsWith("ore"))
				{
					String ore = name.substring("ore".length());
					ItemStack out = oreOutputModifier.get(ore);
					if(out==null||out.isEmpty())
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
									addShapelessOredictRecipe("hammercrushing_"+ore, preferredDust, name, new ItemStack(IEContent.itemTool));
									hammerCrushingList.add(ore);
								}
							}
						}
					}
					if(out!=null&&!out.isEmpty())
					{
						Object[] secondaries = oreOutputSecondaries.get(ore);
						Object s = secondaries!=null&&secondaries.length > 1?secondaries[0]: null;
						Float f = secondaries!=null&&secondaries.length > 1&&secondaries[1] instanceof Float?(Float)secondaries[1]: 0;
						addOreProcessingRecipe(out, ore, 6000, true, s, f);
					}
					out = arcOutputModifier.get(ore);
					if(out==null||out.isEmpty())
					{
						if(ApiUtils.isExistingOreName("ingot"+ore))
							out = Utils.copyStackWithAmount(IEApi.getPreferredOreStack("ingot"+ore), 2);
					}
					if(out!=null&&!out.isEmpty()&&!arcBlacklist.contains(ore))
						addArcOreSmelting(out, ore);
				}
				else if(name.startsWith("gem"))
				{
					String ore = name.substring("gem".length());
					if(ApiUtils.isExistingOreName("dust"+ore))
						addCrusherRecipe(IEApi.getPreferredOreStack("dust"+ore), "gem"+ore, 6000, null, 0);
				}
				else if(name.startsWith("dust"))
				{
					String ore = name.substring("dust".length());
					ItemStack out = arcOutputModifier.get(ore);
					if(out==null||out.isEmpty())
					{
						if(ApiUtils.isExistingOreName("ingot"+ore))
							out = IEApi.getPreferredOreStack("ingot"+ore);
					}
					else
						out = Utils.copyStackWithAmount(out, out.getCount()/2);
					if(out!=null&&!out.isEmpty()&&!arcBlacklist.contains(ore))
						addArcRecipe(out, "dust"+ore, 100, 512, ItemStack.EMPTY);
					if(ApiUtils.isExistingOreName("ingot"+ore))
						addCrusherRecipe(IEApi.getPreferredOreStack("dust"+ore), "ingot"+ore, 3600, null, 0);
				}
				else if(name.startsWith("plate"))
				{
					String ore = name.substring("plate".length());
					if(ApiUtils.isExistingOreName("ingot"+ore))
						MetalPressRecipe.addRecipe(IEApi.getPreferredOreStack(name), "ingot"+ore, compMoldPlate, 2400);
				}
				else if(name.startsWith("gear"))
				{
					IEContent.itemMold.setMetaUnhidden(1);
					String ore = name.substring("gear".length());
					if(ApiUtils.isExistingOreName("ingot"+ore))
						MetalPressRecipe.addRecipe(IEApi.getPreferredOreStack(name), "ingot"+ore, compMoldGear, 2400).setInputSize(4);
				}
				else if(name.startsWith("stick")||name.startsWith("rod"))
				{
					String ore = name.startsWith("stick")?name.substring("stick".length()): name.substring("rod".length());
					boolean priorityStick = !name.startsWith("rod")||!ApiUtils.isExistingOreName("stick"+ore);
					if(priorityStick&&ApiUtils.isExistingOreName("ingot"+ore))
						MetalPressRecipe.addRecipe(Utils.copyStackWithAmount(IEApi.getPreferredOreStack(name), 2), "ingot"+ore, compMoldRod, 2400);
				}
				else if(name.startsWith("wire"))
				{
					String ore = name.substring("wire".length());
					if(ApiUtils.isExistingOreName("ingot"+ore))
						MetalPressRecipe.addRecipe(Utils.copyStackWithAmount(IEApi.getPreferredOreStack(name), 2), "ingot"+ore, compMoldWire, 2400);
				}
		Config.manual_bool.put("crushingOreRecipe", !hammerCrushingList.isEmpty());
	}

	public static CrusherRecipe addCrusherRecipe(ItemStack output, Object input, int energy, Object... secondary)
	{
		CrusherRecipe r = CrusherRecipe.addRecipe(output, input, energy);
		if(secondary!=null&&secondary.length > 0)
			r.addToSecondaryOutput(secondary);
		return r;
	}

	public static void addOreProcessingRecipe(ItemStack output, String ore, int energy, boolean ingot, Object secondary, float secChance)
	{
		if(ingot&&ApiUtils.isExistingOreName("ingot"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(output, output.getCount()/2), "ingot"+ore, (int)(energy*.6f));
		if(ApiUtils.isExistingOreName("ore"+ore))
			addCrusherRecipe(output, "ore"+ore, energy, secondary, secChance);
		//		if(ApiUtils.isExistingOreName("oreNether"+ore))
		//			addCrusherRecipe(Utils.copyStackWithAmount(output, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, energy, secondary,secChance,Blocks.netherrack,.15f);

		//YAY GregTech!
		if(ApiUtils.isExistingOreName("oreNetherrack"+ore))
			addCrusherRecipe(output, "oreNetherrack"+ore, energy, secondary, secChance, new ItemStack(Blocks.NETHERRACK), .15f);
		if(ApiUtils.isExistingOreName("oreEndstone"+ore))
			addCrusherRecipe(output, "oreEndstone"+ore, energy, secondary, secChance, "dustEndstone", .5f);
		if(ApiUtils.isExistingOreName("oreBlackgranite"+ore))
			addCrusherRecipe(output, "oreBlackgranite"+ore, energy, secondary, secChance, "dustGraniteBlack", .5f);
		if(ApiUtils.isExistingOreName("oreRedgranite"+ore))
			addCrusherRecipe(output, "oreRedgranite"+ore, energy, secondary, secChance, "dustGraniteBlack", .5f);
	}

	public static void addOreDictCrusherRecipe(String ore, Object secondary, float chance)
	{
		if(!ApiUtils.isExistingOreName("dust"+ore))
			return;
		ItemStack dust = IEApi.getPreferredOreStack("dust"+ore);
		if(dust.isEmpty())
			return;
		if(ApiUtils.isExistingOreName("ore"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "ore"+ore, 6000, secondary, chance);
		if(ApiUtils.isExistingOreName("ingot"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 1), "ingot"+ore, 3600);
		//		if(ApiUtils.isExistingOreName("oreNether"+ore))
		//			addCrusherRecipe(Utils.copyStackWithAmount(dust, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, 6000, secondary,chance,Blocks.netherrack,.15f);

		//YAY GregTech!
		if(ApiUtils.isExistingOreName("oreNetherrack"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreNetherrack"+ore, 6000, secondary, chance, new ItemStack(Blocks.NETHERRACK), .15f);
		if(ApiUtils.isExistingOreName("oreEndstone"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreEndstone"+ore, 6000, secondary, chance, "dustEndstone", .5f);
		if(ApiUtils.isExistingOreName("oreBlackgranite"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreBlackgranite"+ore, 6000, secondary, chance, "dustGraniteBlack", .5f);
		if(ApiUtils.isExistingOreName("oreRedgranite"+ore))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), "oreRedgranite"+ore, 6000, secondary, chance, "dustGraniteRed", .5f);
	}

	public static CrusherRecipe addItemToOreDictCrusherRecipe(String oreName, int outSize, Object input, int energy)
	{
		if(!ApiUtils.isExistingOreName(oreName))
			return null;
		ItemStack out = IEApi.getPreferredOreStack(oreName);
		if(out.isEmpty())
			return null;
		return CrusherRecipe.addRecipe(Utils.copyStackWithAmount(out, outSize), input, energy);
	}

	public static void initAlloySmeltingRecipes()
	{
		//IE Alloys
		addAlloyingRecipe(new ItemStack(IEContent.itemMetal, 2, 6), "Copper", 1, "Nickel", 1, 200);
		addAlloyingRecipe(new ItemStack(IEContent.itemMetal, 2, 7), "Gold", 1, "Silver", 1, 200);
		//Common Alloys
		addOreDictAlloyingRecipe("ingotInvar", 3, "Iron", 2, "Nickel", 1, 200);
		addOreDictAlloyingRecipe("ingotBronze", 4, "Copper", 3, "Tin", 1, 200);
		addOreDictAlloyingRecipe("ingotBrass", 4, "Copper", 3, "Zinc", 1, 200);
		addOreDictAlloyingRecipe("ingotBlueAlloy", 1, "Silver", 1, "Nikolite", 4, 200);
		addOreDictAlloyingRecipe("ingotRedAlloy", 1, "Copper", 1, "Redstone", 4, 200);

	}

	public static void addOreDictAlloyingRecipe(String outName, int outSize, String input0, int size0, String input1, int size1, int time)
	{
		if(!ApiUtils.isExistingOreName(outName))
			return;
		ItemStack out = IEApi.getPreferredOreStack(outName);
		if(out.isEmpty())
			return;
		addAlloyingRecipe(Utils.copyStackWithAmount(out, outSize), input0, size0, input1, size1, time);
	}

	public static void addAlloyingRecipe(ItemStack output, String input0, int size0, String input1, int size1, int time)
	{
		boolean ingot0 = ApiUtils.isExistingOreName("ingot"+input0);
		boolean ingot1 = ApiUtils.isExistingOreName("ingot"+input1);
		boolean dust0 = ApiUtils.isExistingOreName("dust"+input0);
		boolean dust1 = ApiUtils.isExistingOreName("dust"+input1);
		if(ingot0&&ingot1)
			AlloyRecipe.addRecipe(output, new IngredientStack("ingot"+input0, size0), new IngredientStack("ingot"+input1, size1), time);
		if(dust0&&dust1)
			AlloyRecipe.addRecipe(output, new IngredientStack("dust"+input0, size0), new IngredientStack("dust"+input1, size1), time);
		if(ingot0&&dust1)
			AlloyRecipe.addRecipe(output, new IngredientStack("ingot"+input0, size0), new IngredientStack("dust"+input1, size1), time);
		if(dust0&&ingot1)
			AlloyRecipe.addRecipe(output, new IngredientStack("dust"+input0, size0), new IngredientStack("ingot"+input1, size1), time);
	}

	public static HashMap<String, ItemStack> arcOutputModifier = new HashMap<String, ItemStack>();
	public static HashSet<String> arcBlacklist = new HashSet<String>();

	public static void initArcSmeltingRecipes()
	{
		//Steel
		ArcFurnaceRecipe.addRecipe(new ItemStack(IEContent.itemMetal, 1, 8), "ingotIron", new ItemStack(IEContent.itemMaterial, 1, 7), 400, 512, "dustCoke");
		ArcFurnaceRecipe.addRecipe(new ItemStack(IEContent.itemMetal, 1, 8), "dustIron", new ItemStack(IEContent.itemMaterial, 1, 7), 400, 512, "dustCoke");
		//Vanilla+IE Ores
		arcOutputModifier.put("Iron", new ItemStack(Items.IRON_INGOT, 2));
		arcOutputModifier.put("Gold", new ItemStack(Items.GOLD_INGOT, 2));
		arcOutputModifier.put("Copper", new ItemStack(IEContent.itemMetal, 2, 0));
		arcOutputModifier.put("Aluminum", new ItemStack(IEContent.itemMetal, 2, 1));
		arcOutputModifier.put("Aluminium", new ItemStack(IEContent.itemMetal, 2, 1));
		arcOutputModifier.put("Lead", new ItemStack(IEContent.itemMetal, 2, 2));
		arcOutputModifier.put("Silver", new ItemStack(IEContent.itemMetal, 2, 3));
		arcOutputModifier.put("Nickel", new ItemStack(IEContent.itemMetal, 2, 4));
		//IE Alloys
		addOreDictArcAlloyingRecipe(new ItemStack(IEContent.itemMetal, 2, 6), "Copper", 100, 512, "dustNickel");
		addOreDictArcAlloyingRecipe(new ItemStack(IEContent.itemMetal, 2, 6), "Nickel", 100, 512, "dustCopper");
		addOreDictArcAlloyingRecipe(new ItemStack(IEContent.itemMetal, 2, 7), "Gold", 100, 512, "dustSilver");
		addOreDictArcAlloyingRecipe(new ItemStack(IEContent.itemMetal, 2, 7), "Silver", 100, 512, "dustGold");
		//Common Alloys
		addOreDictArcAlloyingRecipe("ingotInvar", 3, "Nickel", 200, 512, "dustIron", "dustIron");
		addOreDictArcAlloyingRecipe("ingotBronze", 4, "Tin", 200, 512, new IngredientStack("dustCopper", 3));
		addOreDictArcAlloyingRecipe("ingotBrass", 4, "Zinc", 200, 512, new IngredientStack("dustCopper", 3));
		addOreDictArcAlloyingRecipe("ingotBlueAlloy", 1, "Silver", 100, 512, new IngredientStack("dustNikolite", 4));
		addOreDictArcAlloyingRecipe("ingotRedAlloy", 1, "Copper", 100, 512, new IngredientStack("dustRedstone", 4));

		//Recycling
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.itemWireCoil, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.itemDrillhead, 1, OreDictionary.WILDCARD_VALUE));

		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration0, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration1, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration2, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDevice0, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDevice1, 1, OreDictionary.WILDCARD_VALUE));
	}

	public static ArcFurnaceRecipe addArcRecipe(ItemStack output, Object input, int time, int energyPerTick, @Nonnull ItemStack slag, Object... additives)
	{
		return ArcFurnaceRecipe.addRecipe(output, input, slag, time, energyPerTick, additives);
	}

	public static void addArcOreSmelting(ItemStack output, String ore)
	{
		if(ApiUtils.isExistingOreName("ore"+ore))
			addArcRecipe(output, "ore"+ore, 200, 512, new ItemStack(IEContent.itemMaterial, 1, 7)).setSpecialRecipeType("Ores");
		//		if(ApiUtils.isExistingOreName("oreNether"+ore))
		//			addArcRecipe(Utils.copyStackWithAmount(output, NetherOresHelper.getCrushingResult(ore)), "oreNether"+ore, 200,512, new ItemStack(IEContent.itemMaterial,1,13)).setSpecialRecipeType("Ores");

		//YAY GregTech!
		if(ApiUtils.isExistingOreName("oreNetherrack"+ore))
			addArcRecipe(output, "oreNetherrack"+ore, 200, 512, new ItemStack(IEContent.itemMaterial, 1, 7)).setSpecialRecipeType("Ores");
		if(ApiUtils.isExistingOreName("oreEndstone"+ore))
			addArcRecipe(output, "oreEndstone"+ore, 200, 512, new ItemStack(IEContent.itemMaterial, 1, 7)).setSpecialRecipeType("Ores");
		if(ApiUtils.isExistingOreName("oreBlackgranite"+ore))
			addArcRecipe(output, "oreBlackgranite"+ore, 200, 512, new ItemStack(IEContent.itemMaterial, 1, 7)).setSpecialRecipeType("Ores");
		if(ApiUtils.isExistingOreName("oreRedgranite"+ore))
			addArcRecipe(output, "oreRedgranite"+ore, 200, 512, new ItemStack(IEContent.itemMaterial, 1, 7)).setSpecialRecipeType("Ores");
	}

	public static void addOreDictArcAlloyingRecipe(String outName, int outSize, String inputName, int time, int energyPerTick, Object... additives)
	{
		if(!ApiUtils.isExistingOreName(outName))
			return;
		ItemStack out = IEApi.getPreferredOreStack(outName);
		if(out.isEmpty())
			return;
		addOreDictArcAlloyingRecipe(Utils.copyStackWithAmount(out, outSize), inputName, time, energyPerTick, additives);
	}

	public static void addOreDictArcAlloyingRecipe(ItemStack out, String inputName, int time, int energyPerTick, Object... additives)
	{
		if(ApiUtils.isExistingOreName("ingot"+inputName))
			ArcFurnaceRecipe.addRecipe(out, "ingot"+inputName, ItemStack.EMPTY, time, energyPerTick, additives).setSpecialRecipeType("Alloying");
		if(ApiUtils.isExistingOreName("dust"+inputName))
			ArcFurnaceRecipe.addRecipe(out, "dust"+inputName, ItemStack.EMPTY, time, energyPerTick, additives).setSpecialRecipeType("Alloying");
	}
}
