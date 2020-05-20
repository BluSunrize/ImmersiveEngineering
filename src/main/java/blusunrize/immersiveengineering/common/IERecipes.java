/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe.SecondaryOutput;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.utils.ClocheRenderHelper.RenderFunctionChorus;
import blusunrize.immersiveengineering.client.utils.ClocheRenderHelper.RenderFunctionHemp;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.crafting.MetalPressPackingRecipe;
import blusunrize.immersiveengineering.common.crafting.MetalPressUnpackingRecipe;
import blusunrize.immersiveengineering.common.crafting.OreCrushingRecipe;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEBaseItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Molds;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.common.IEContent.*;
import static blusunrize.immersiveengineering.common.blocks.EnumMetals.*;
import static blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals.ores;
import static blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals.storage;
import static blusunrize.immersiveengineering.common.items.IEItems.Ingredients.*;
import static blusunrize.immersiveengineering.common.items.IEItems.Metals.ingots;
import static blusunrize.immersiveengineering.common.items.IEItems.Misc.hempSeeds;
import static blusunrize.immersiveengineering.common.items.IEItems.Molds.*;

public class IERecipes
{
	private static void initBlueprintRecipes()
	{
		//
		//MATERIALS
		//
		Tag<Item> ironPlate = IETags.getTagsFor(IRON).plate;
		Tag<Item> steelPlate = IETags.getTagsFor(STEEL).plate;
		Tag<Item> nickelPlate = IETags.getTagsFor(NICKEL).plate;
		Tag<Item> copperPlate = IETags.getTagsFor(COPPER).plate;
		Tag<Item> copperIngot = IETags.getTagsFor(COPPER).ingot;
		Tag<Item> hopGraphiteIngot = IETags.hopGraphiteIngot;
		Tag<Item> leadNugget = IETags.getTagsFor(LEAD).nugget;
		Tag<Item> silverNugget = IETags.getTagsFor(SILVER).nugget;
		Tag<Item> steelNugget = IETags.getTagsFor(LEAD).nugget;
		Tag<Item> aluDust = IETags.getTagsFor(ALUMINUM).dust;
		Tag<Item> constantanNugget = IETags.getTagsFor(CONSTANTAN).nugget;
		Tag<Item> glassBlock = Tags.Items.GLASS;
		Tag<Item> dustRedstone = Tags.Items.DUSTS_REDSTONE;
		Tag<Item> copperWire = IETags.copperWire;
		Tag<Item> redDye = Tags.Items.DYES_RED;
		Tag<Item> greenDye = Tags.Items.DYES_GREEN;
		Tag<Item> yellowDye = Tags.Items.DYES_YELLOW;
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(componentIron), ironPlate, ironPlate, copperIngot);
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(componentSteel), steelPlate, steelPlate, copperIngot);
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(electronTube, 3), glassBlock, nickelPlate, copperWire, dustRedstone);
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(circuitBoard),
				new ItemStack(StoneDecoration.insulatingGlass), copperPlate, electronTube, electronTube);

		//
		//MOLDS
		//
		for(Item curr : new Item[]{moldPlate,
				moldGear,
				moldRod,
				moldBulletCasing,
				moldWire,
				moldPacking4,
				moldPacking9,
				moldUnpacking})
			if(!((IEBaseItem)curr).isHidden())
				BlueprintCraftingRecipe.addRecipe("molds", new ItemStack(curr),
						steelPlate, steelPlate, steelPlate, steelPlate, steelPlate, new ItemStack(Tools.wirecutter));

		//
		//BULLETS
		//
		//Casull
		ItemStack bullet = BulletHandler.getBulletStack(BulletItem.CASULL);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, leadNugget, leadNugget);
		//Piercing
		bullet = BulletHandler.getBulletStack(BulletItem.ARMOR_PIERCING);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, steelNugget, steelNugget, constantanNugget, constantanNugget);
		ResourceLocation tungstenNugget = getNugget("tungsten");
		if(ApiUtils.isNonemptyItemTag(tungstenNugget))
			BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, tungstenNugget, tungstenNugget);
		// We don't have depleted stuff atm
		//		if(ApiUtils.isNonemptyItemTag("nuggetCyanite"))
		//			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetCyanite","nuggetCyanite");
		//		else if(ApiUtils.isNonemptyItemTag("ingotCyanite"))
		//			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,3,3), new ItemStack(IEContent.itemBullet,3,0),new ItemStack(Items.gunpowder,3),"ingotCyanite");
		//Silver
		bullet = BulletHandler.getBulletStack(BulletItem.SILVER);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, leadNugget, leadNugget, silverNugget);
		//Buckshot
		bullet = BulletHandler.getBulletStack(BulletItem.BUCKSHOT);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyShell), Items.GUNPOWDER, getDust("iron"));
		//HE
		bullet = BulletHandler.getBulletStack(BulletItem.HIGH_EXPLOSIVE);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, Blocks.TNT);
		//Dragonsbreath
		bullet = BulletHandler.getBulletStack(BulletItem.DRAGONS_BREATH);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet, new ItemStack(emptyShell), Items.GUNPOWDER, aluDust, aluDust);
		//Potion
		bullet = BulletHandler.getBulletStack(BulletItem.POTION);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, Items.GLASS_BOTTLE);
		//Flare
		bullet = BulletHandler.getBulletStack(BulletItem.FLARE);
		ItemNBTHelper.putInt(bullet, "flareColour", 0xcc2e06);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(emptyShell), Items.GUNPOWDER, aluDust, redDye);
		ItemNBTHelper.putInt(bullet, "flareColour", 0x2ca30b);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(emptyShell), Items.GUNPOWDER, aluDust, greenDye);
		ItemNBTHelper.putInt(bullet, "flareColour", 0xffff82);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(emptyShell), Items.GUNPOWDER, aluDust, yellowDye);

		//Wolfpack
		/*TODO new condition, or maybe an event?
		if(!BulletHandler.homingCartridges.isEmpty())
		{
			bullet = BulletHandler.getBulletStack(BulletItem.WOLFPACK);
			ArrayList<ItemStack> homingCartridges = new ArrayList<>();
			for(String s : BulletHandler.homingCartridges)
				homingCartridges.add(BulletHandler.getBulletStack(s));
			BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(emptyShell), Items.GUNPOWDER, homingCartridges, homingCartridges, homingCartridges, homingCartridges);
		}
		 */

		BlueprintCraftingRecipe.addVillagerTrade("bullet", new ItemStack(Items.EMERALD, 1));
		BlueprintCraftingRecipe.addVillagerTrade("specialBullet", new ItemStack(Items.EMERALD, 1));

		BlueprintCraftingRecipe.addRecipe("electrode", new ItemStack(Misc.graphiteElectrode),
				hopGraphiteIngot, hopGraphiteIngot, hopGraphiteIngot, hopGraphiteIngot);
		BlueprintCraftingRecipe.addVillagerTrade("electrode", new ItemStack(Items.EMERALD, 1));
	}

	private static void initBlastFurnaceRecipes()
	{
		Ingredient ironIngot = Ingredient.fromTag(IETags.getTagsFor(IRON).ingot);
		Ingredient ironBlock = Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON);
		BlastFurnaceRecipe.addRecipe(new ItemStack(ingots.get(STEEL)), ironIngot, 1200, new ItemStack(slag));
		BlastFurnaceRecipe.addRecipe(new ItemStack(storage.get(STEEL)), ironBlock, 1200*9, new ItemStack(slag, 9));

		BlastFurnaceRecipe.addBlastFuel(Ingredient.fromTag(IETags.coalCoke), 1200);
		BlastFurnaceRecipe.addBlastFuel(Ingredient.fromTag(IETags.getItemTag(IETags.coalCokeBlock)), 1200*10);

		BlastFurnaceRecipe.addBlastFuel(Ingredient.fromTag(IETags.charCoal), 300);
		BlastFurnaceRecipe.addBlastFuel(Ingredient.fromTag(IETags.getItemTag(IETags.charCoalBlocks)), 300*10);
	}

	private static void initClocheRecipes()
	{
		ClocheRecipe.registerSoilTexture(new ItemStack[]{new ItemStack(Items.DIRT), new ItemStack(Items.COARSE_DIRT),
				new ItemStack(Items.GRASS_BLOCK), new ItemStack(Items.GRASS_PATH),}, new ResourceLocation("block/farmland_moist"));

		ClocheRecipe.addFertilizer(Items.BONE_MEAL, 1.25f);

		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.WHEAT, 2), new ItemStack(Items.WHEAT_SEEDS, 1)),
				Items.WHEAT_SEEDS, Tags.Blocks.DIRT, 640, ClocheRecipe.RENDER_FUNCTION_CROP.apply(Blocks.WHEAT));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.POTATO, 2)), Items.POTATO, Tags.Blocks.DIRT, 800,
				ClocheRecipe.RENDER_FUNCTION_CROP.apply(Blocks.POTATOES));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.CARROT, 2)), Items.CARROT, Tags.Blocks.DIRT, 800,
				ClocheRecipe.RENDER_FUNCTION_CROP.apply(Blocks.CARROTS));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.BEETROOT, 2), new ItemStack(Items.BEETROOT_SEEDS, 1)),
				Items.BEETROOT_SEEDS, Tags.Blocks.DIRT, 800, ClocheRecipe.RENDER_FUNCTION_CROP.apply(Blocks.BEETROOTS));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.NETHER_WART, 2)), Items.NETHER_WART, Tags.Blocks.NETHERRACK, 800,
				ClocheRecipe.RENDER_FUNCTION_CROP.apply(Blocks.CARROTS));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.SWEET_BERRIES, 2)), Items.SWEET_BERRIES, Tags.Blocks.DIRT, 560,
				ClocheRecipe.RENDER_FUNCTION_CROP.apply(Blocks.SWEET_BERRY_BUSH));

		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.PUMPKIN)), Items.PUMPKIN_SEEDS, Tags.Blocks.DIRT, 800,
				ClocheRecipe.RENDER_FUNCTION_STEM.apply(Blocks.PUMPKIN));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Blocks.MELON)), Items.MELON_SEEDS, Tags.Blocks.DIRT, 800,
				ClocheRecipe.RENDER_FUNCTION_STEM.apply(Blocks.MELON));

		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.SUGAR_CANE)), Items.SUGAR_CANE, Tags.Blocks.SAND, 560,
				ClocheRecipe.RENDER_FUNCTION_STACK.apply(Blocks.SUGAR_CANE));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Blocks.CACTUS)), Items.CACTUS, Tags.Blocks.SAND, 560,
				ClocheRecipe.RENDER_FUNCTION_STACK.apply(Blocks.CACTUS));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.CHORUS_FRUIT)), Items.CHORUS_FLOWER, Tags.Blocks.END_STONES, 480,
				new RenderFunctionChorus());
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(hempFiber), new ItemStack(hempSeeds, 2)), hempSeeds, Tags.Blocks.DIRT, 800,
				new RenderFunctionHemp());

		IngredientStack shroomSoil = new IngredientStack(ImmutableList.of(new ItemStack(Blocks.MYCELIUM), new ItemStack(Blocks.PODZOL)));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.RED_MUSHROOM, 1)), Items.RED_MUSHROOM, shroomSoil, 480,
				ClocheRecipe.RENDER_FUNCTION_GENERIC.apply(Blocks.RED_MUSHROOM));
		ClocheRecipe.addRecipe(ImmutableList.of(new ItemStack(Items.BROWN_MUSHROOM, 1)), Items.BROWN_MUSHROOM, shroomSoil, 480,
				ClocheRecipe.RENDER_FUNCTION_GENERIC.apply(Blocks.BROWN_MUSHROOM));
	}

	private static void initMetalPressRecipes()
	{
		//Bullet casing
		MetalPressRecipe.addRecipe(new ItemStack(emptyCasing, 2), IETags.getTagsFor(COPPER).ingot, new ItemStack(moldBulletCasing), 2400);

		//Damaged Graphite Electrodes
		ItemStack shoddyElectrode = new ItemStack(Misc.graphiteElectrode);
		shoddyElectrode.setDamage(IEConfig.MACHINES.arcfurnace_electrodeDamage.get()/2);
		MetalPressRecipe.addRecipe(shoddyElectrode, IETags.hopGraphiteIngot, new ItemStack(moldRod), 4800).setInputSize(4);

		//Slicing Melons
		MetalPressRecipe.addRecipe(new ItemStack(Items.MELON_SLICE, 9), new ItemStack(Blocks.MELON), new ItemStack(moldUnpacking), 3200);

		//Packing & Unpacking
		ComparableItemStack pack2x2 = ApiUtils.createComparableItemStack(new ItemStack(moldPacking4), false);
		MetalPressRecipe.recipeList.put(pack2x2, new MetalPressPackingRecipe(pack2x2, 3200, 2));
		ComparableItemStack pack3x3 = ApiUtils.createComparableItemStack(new ItemStack(moldPacking9), false);
		MetalPressRecipe.recipeList.put(pack3x3, new MetalPressPackingRecipe(pack3x3, 3200, 3));
		ComparableItemStack unpack = ApiUtils.createComparableItemStack(new ItemStack(moldUnpacking), false);
		MetalPressRecipe.recipeList.put(unpack, new MetalPressUnpackingRecipe(unpack, 3200));
	}

	public static HashMap<String, ItemStack> oreOutputModifier = new HashMap<>();
	public static HashMap<String, SecondaryOutput> oreOutputSecondaries = new HashMap<>();
	public static ArrayList<String> hammerCrushingList = new ArrayList<>();

	private static void initCrusherRecipes()
	{
		oreOutputSecondaries.put("iron", new SecondaryOutput(getDust("nickel"), .1f));
		oreOutputSecondaries.put("gold", new SecondaryOutput(getCrystal("cinnabar"), .05f));
		oreOutputSecondaries.put("copper", new SecondaryOutput(getDust("gold"), .1f));
		oreOutputSecondaries.put("lead", new SecondaryOutput(getDust("silver"), .1f));
		oreOutputSecondaries.put("silver", new SecondaryOutput(getDust("lead"), .1f));
		oreOutputSecondaries.put("nickel", new SecondaryOutput(getDust("platinum"), .1f));

		oreOutputModifier.put("lapis", new ItemStack(Items.LAPIS_LAZULI, 9));
		oreOutputSecondaries.put("lapis", new SecondaryOutput(getDust("sulfur"), .15f));
		oreOutputModifier.put("diamond", new ItemStack(Items.DIAMOND, 2));
		oreOutputModifier.put("redstone", new ItemStack(Items.REDSTONE, 6));
		oreOutputSecondaries.put("redstone", new SecondaryOutput(getCrystal("cinnabar"), .25f));
		oreOutputModifier.put("emerald", new ItemStack(Items.EMERALD, 2));
		oreOutputModifier.put("quartz", new ItemStack(Items.QUARTZ, 3));
		oreOutputSecondaries.put("quartz", new SecondaryOutput(getDust("sulfur"), .15f));
		oreOutputModifier.put("coal", new ItemStack(Items.COAL, 4));

		oreOutputSecondaries.put("platinum", new SecondaryOutput(getDust("nickel"), .1f));
		oreOutputSecondaries.put("tungsten", new SecondaryOutput(getDust("manganese"), .1f));
		oreOutputSecondaries.put("uranium", new SecondaryOutput(getDust("lead"), .1f));
		oreOutputSecondaries.put("yellorium", new SecondaryOutput(getDust("lead"), .1f));
		oreOutputSecondaries.put("plutonium", new SecondaryOutput(getDust("uranium"), .1f));
		oreOutputSecondaries.put("iridium", new SecondaryOutput(getDust("platium"), .1f));
		oreOutputSecondaries.put("nikolite", new SecondaryOutput(Tags.Items.GEMS_DIAMOND, .025f));

		addCrusherRecipe(new ItemStack(Blocks.GRAVEL), Tags.Items.COBBLESTONE, 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), Tags.Items.GRAVEL, 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), IETags.slag, 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), Tags.Items.GLASS, 3200);
		addCrusherRecipe(new ItemStack(Blocks.SAND, 2), Tags.Items.SANDSTONE, 1600, new SecondaryOutput(dustSaltpeter, .5f));
		addCrusherRecipeBlockTag(new ItemStack(Items.CLAY_BALL, 4), IETags.clayBlock, 1600);
		addCrusherRecipe(new ItemStack(Items.QUARTZ, 4), Tags.Items.STORAGE_BLOCKS_QUARTZ, 3200);
		addCrusherRecipeBlockTag(new ItemStack(Items.GLOWSTONE_DUST, 4), IETags.glowstoneBlock, 3200);
		addCrusherRecipe(new ItemStack(Items.BLAZE_POWDER, 4), Tags.Items.RODS_BLAZE, 3200, new SecondaryOutput(dustSulfur, .5f));
		addCrusherRecipe(new ItemStack(Items.BONE_MEAL, 6), Tags.Items.BONES, 3200);
		addCrusherRecipe(new ItemStack(dustCoke), IETags.coalCoke, 2400);
		addCrusherRecipeBlockTag(new ItemStack(dustCoke, 9), IETags.coalCokeBlock, 4800);
		addItemToOreDictCrusherRecipe(getDust("coal"), 1, new ItemStack(Items.COAL), 2400);
		addItemToOreDictCrusherRecipe(getDust("obsidian"), 4, Blocks.OBSIDIAN, 6000);
		for(Entry<Tag<Item>, Item> dyeAndWool : Utils.WOOL_DYE_BIMAP.entrySet())
		{
			CrusherRecipe r = CrusherRecipe.addRecipe(new ItemStack(Items.STRING, 4), dyeAndWool.getValue(), 3200);
			if(dyeAndWool.getValue()!=Blocks.WHITE_WOOL.asItem())
				r.addToSecondaryOutput(new SecondaryOutput(dyeAndWool.getKey(), .05f));
		}
	}

	public static ResourceLocation getCrystal(String type)
	{
		//TODO dos anyone use this?
		return new ResourceLocation("forge", "crystal/"+type);
	}

	public static ResourceLocation getGem(String type)
	{
		return new ResourceLocation("forge", "gems/"+type);
	}

	public static ResourceLocation getDust(String type)
	{
		return new ResourceLocation("forge", "dusts/"+type);
	}

	public static ResourceLocation getIngot(String type)
	{
		return new ResourceLocation("forge", "ingots/"+type);
	}

	public static ResourceLocation getPlate(String type)
	{
		return new ResourceLocation("forge", "plates/"+type);
	}

	public static ResourceLocation getStick(String type)
	{
		return new ResourceLocation("forge", "sticks/"+type);
	}

	public static ResourceLocation getRod(String type)
	{
		return new ResourceLocation("forge", "rods/"+type);
	}

	public static ResourceLocation getOre(String type)
	{
		return new ResourceLocation("forge", "ores/"+type);
	}

	public static ResourceLocation getStorageBlock(String type)
	{
		return new ResourceLocation("forge", "storage_blocks/"+type);
	}

	public static ResourceLocation getSheetmetalBlock(String type)
	{
		return new ResourceLocation("forge", "sheetmetal/"+type);
	}

	public static ResourceLocation getNugget(String type)
	{
		return new ResourceLocation("forge", "nuggets/"+type);
	}

	public static void readdRecipes()
	{
		clearRecipes();
		initCrusherRecipes();
		addTagBasedRecipes();
		initBlastFurnaceRecipes();
		initMetalPressRecipes();
		initAlloySmeltingRecipes();
		initArcSmeltingRecipes();
		initBlueprintRecipes();
		initClocheRecipes();
		addExcavatorVeins();
		addMiscRecipes();
	}

	private static void clearRecipes()
	{
		AlloyRecipe.recipeList.clear();
		ArcFurnaceRecipe.recipeList.clear();
		BlastFurnaceRecipe.recipeList.clear();
		BlueprintCraftingRecipe.recipeList.clear();
		BottlingMachineRecipe.recipeList.clear();
		ClocheRecipe.recipeList.clear();
		CokeOvenRecipe.recipeList.clear();
		CrusherRecipe.recipeList.clear();
		FermenterRecipe.recipeList.clear();
		MetalPressRecipe.recipeList.clear();
		MixerRecipe.recipeList.clear();
		RefineryRecipe.recipeList.clear();
		SqueezerRecipe.recipeList.clear();

		oreOutputModifier.clear();
		oreOutputSecondaries.clear();
		hammerCrushingList.clear();
	}

	private static void addTagBasedRecipes()
	{
		ComparableItemStack compMoldPlate = ApiUtils.createComparableItemStack(new ItemStack(moldPlate), false);
		ComparableItemStack compMoldGear = ApiUtils.createComparableItemStack(new ItemStack(moldGear), false);
		ComparableItemStack compMoldRod = ApiUtils.createComparableItemStack(new ItemStack(moldRod), false);
		ComparableItemStack compMoldWire = ApiUtils.createComparableItemStack(new ItemStack(Molds.moldWire), false);

		OreCrushingRecipe.CRUSHABLE_ORES_WITH_OUTPUT.clear();
		for(Entry<ResourceLocation, Tag<Item>> tag : ItemTags.getCollection().getTagMap().entrySet())
			if(!tag.getValue().getAllElements().isEmpty()&&tag.getKey().getNamespace().equals("forge"))
			{
				String path = tag.getKey().getPath();
				if(path.startsWith("ores/"))
				{
					String baseName = tag.getKey().getPath().substring("ores/".length());
					ItemStack out = oreOutputModifier.get(baseName);
					if(out==null||out.isEmpty())
					{
						ResourceLocation gem = getGem(baseName);
						if(ApiUtils.isNonemptyItemTag(gem))
							out = Utils.copyStackWithAmount(IEApi.getPreferredTagStack(gem), 2);
						else
						{
							ResourceLocation dust = getDust(baseName);
							if(ApiUtils.isNonemptyItemTag(dust))
							{
								OreCrushingRecipe.CRUSHABLE_ORES_WITH_OUTPUT.add(new ImmutablePair<>(tag.getValue(),
										ItemTags.getCollection().get(dust)));
								out = Utils.copyStackWithAmount(IEApi.getPreferredTagStack(dust), 2);
							}
						}
					}
					if(out!=null&&!out.isEmpty())
					{
						SecondaryOutput secondaries = oreOutputSecondaries.get(baseName);
						addOreProcessingRecipe(out, baseName, 6000, secondaries);
					}
					out = arcOutputModifier.get(baseName);
					if(out==null||out.isEmpty())
					{
						ResourceLocation ingot = getIngot(baseName);
						if(ApiUtils.isNonemptyItemTag(ingot))
							out = Utils.copyStackWithAmount(IEApi.getPreferredTagStack(ingot), 2);
					}
					if(out!=null&&!out.isEmpty()&&!arcBlacklist.contains(baseName))
						addArcOreSmelting(out, baseName);
				}
				else if(path.startsWith("gems/"))
				{
					String ore = path.substring("gems/".length());
					ResourceLocation dust = getDust(ore);
					if(ApiUtils.isNonemptyItemTag(dust))
						addCrusherRecipe(IEApi.getPreferredTagStack(dust), getGem(ore), 6000);
				}
				else if(path.startsWith("dusts/"))
				{
					String ore = path.substring("dusts/".length());
					ItemStack out = arcOutputModifier.get(ore);
					if(out==null||out.isEmpty())
					{
						if(ApiUtils.isNonemptyItemTag(getIngot(ore)))
							out = IEApi.getPreferredTagStack(getIngot(ore));
					}
					else
						out = Utils.copyStackWithAmount(out, out.getCount()/2);
					if(out!=null&&!out.isEmpty()&&!arcBlacklist.contains(ore))
						addArcRecipe(out, getDust(ore), 100, 512, ItemStack.EMPTY);
					if(ApiUtils.isNonemptyItemTag(getIngot(ore)))
						addCrusherRecipe(IEApi.getPreferredTagStack(getDust(ore)), getIngot(ore), 3600);
				}
				else if(path.startsWith("plates/"))
				{
					String ore = path.substring("plates/".length());
					if(ApiUtils.isNonemptyItemTag(getIngot(ore)))
						MetalPressRecipe.addRecipe(IEApi.getPreferredTagStack(tag.getKey()), getIngot(ore), compMoldPlate, 2400);
				}
				else if(path.startsWith("gears/"))
				{
					((IEBaseItem)moldGear).unhide();
					String ore = path.substring("gears/".length());
					if(ApiUtils.isNonemptyItemTag(getIngot(ore)))
						MetalPressRecipe.addRecipe(IEApi.getPreferredTagStack(tag.getKey()), getIngot(ore), compMoldGear, 2400).setInputSize(4);
				}
				else if(path.startsWith("sticks/")||path.startsWith("rods/"))
				{
					boolean isStick = path.startsWith("sticks/");
					String ore = isStick?path.substring("sticks/".length()): path.substring("rods/".length());
					boolean priorityStick = isStick||!ApiUtils.isNonemptyItemTag(getStick(ore));
					if(priorityStick&&ApiUtils.isNonemptyItemTag(getIngot(ore)))
						MetalPressRecipe.addRecipe(Utils.copyStackWithAmount(IEApi.getPreferredTagStack(tag.getKey()), 2),
								getIngot(ore), compMoldRod, 2400);
				}
				else if(path.startsWith("wire/"))
				{
					String ore = path.substring("wire/".length());
					if(ApiUtils.isNonemptyItemTag(getIngot(ore)))
						MetalPressRecipe.addRecipe(Utils.copyStackWithAmount(IEApi.getPreferredTagStack(tag.getKey()), 2),
								getIngot(ore), compMoldWire, 2400);
				}
			}
	}

	public static CrusherRecipe addCrusherRecipeBlockTag(ItemStack output, Tag<Block> input, int energy, SecondaryOutput... secondary)
	{
		return addCrusherRecipe(output, IETags.getItemTag(input), energy, secondary);
	}

	public static CrusherRecipe addCrusherRecipe(ItemStack output, Tag<Item> input, int energy, SecondaryOutput... secondary)
	{
		return addCrusherRecipe(output, new IngredientStack(input), energy, secondary);
	}

	public static CrusherRecipe addCrusherRecipe(ItemStack output, ResourceLocation input, int energy, SecondaryOutput... secondary)
	{
		return addCrusherRecipe(output, new IngredientStack(input), energy, secondary);
	}

	public static CrusherRecipe addCrusherRecipe(ItemStack output, IngredientStack input, int energy, SecondaryOutput... secondary)
	{
		CrusherRecipe r = CrusherRecipe.addRecipe(output, input, energy);
		if(secondary!=null&&secondary.length > 0)
			r.addToSecondaryOutput(secondary);
		return r;
	}

	public static void addOreProcessingRecipe(ItemStack output, String ore, int energy, @Nullable SecondaryOutput secondary)
	{
		if(secondary!=null)
			addCrusherRecipe(output, getOre(ore), energy, secondary);
		else
			addCrusherRecipe(output, getOre(ore), energy);
	}

	public static CrusherRecipe addItemToOreDictCrusherRecipe(ResourceLocation tagOutput, int outSize, Object input, int energy)
	{
		if(!ApiUtils.isNonemptyItemTag(tagOutput))
			return null;
		ItemStack out = IEApi.getPreferredTagStack(tagOutput);
		if(out.isEmpty())
			return null;
		return CrusherRecipe.addRecipe(Utils.copyStackWithAmount(out, outSize), input, energy);
	}

	private static void initAlloySmeltingRecipes()
	{
		//IE Alloys
		addOreDictAlloyingRecipe(IETags.getTagsFor(CONSTANTAN).ingot, 2, "copper", 1, "nickel", 1, 200);
		addOreDictAlloyingRecipe(IETags.getTagsFor(ELECTRUM).ingot, 2, "gold", 1, "silver", 1, 200);
		//Common Alloys
		addOreDictAlloyingRecipe(getIngot("invar"), 3, "iron", 2, "nickel", 1, 200);
		addOreDictAlloyingRecipe(getIngot("bronze"), 4, "copper", 3, "tin", 1, 200);
		addOreDictAlloyingRecipe(getIngot("brass"), 4, "copper", 3, "zinc", 1, 200);
		//TODO addOreDictAlloyingRecipe(getIngot("blueAlloy"), 1, "silver", 1, "nikolite", 4, 200);
		//TODO addOreDictAlloyingRecipe(getIngot("redAlloy"), 1, "copper", 1, "redstone", 4, 200);
	}

	public static void addOreDictAlloyingRecipe(Tag<Item> outName, int outSize, String input0, int size0, String input1, int size1, int time)
	{
		addOreDictAlloyingRecipe(outName.getId(), outSize, input0, size0, input1, size1, time);
	}

	public static void addOreDictAlloyingRecipe(ResourceLocation outName, int outSize, String input0, int size0, String input1, int size1, int time)
	{
		if(!ApiUtils.isNonemptyItemTag(outName))
			return;
		ItemStack out = IEApi.getPreferredTagStack(outName);
		if(out.isEmpty())
			return;
		addAlloyingRecipe(Utils.copyStackWithAmount(out, outSize), input0, size0, input1, size1, time);
	}

	public static void addAlloyingRecipe(ItemStack output, String input0, int size0, String input1, int size1, int time)
	{
		//todo replace with JSON
//		AlloyRecipe.addRecipe(output, new IngredientStack(getIngot(input0), size0), new IngredientStack(getIngot(input1), size1), time);
//		AlloyRecipe.addRecipe(output, new IngredientStack(getDust(input0), size0), new IngredientStack(getDust(input1), size1), time);
//		AlloyRecipe.addRecipe(output, new IngredientStack(getIngot(input0), size0), new IngredientStack(getDust(input1), size1), time);
//		AlloyRecipe.addRecipe(output, new IngredientStack(getDust(input0), size0), new IngredientStack(getIngot(input1), size1), time);
	}

	public static HashMap<String, ItemStack> arcOutputModifier = new HashMap<>();
	public static HashSet<String> arcBlacklist = new HashSet<>();

	private static void initArcSmeltingRecipes()
	{
		//Steel
		ArcFurnaceRecipe.addRecipe(new ItemStack(ingots.get(STEEL)), getIngot("iron"), new ItemStack(slag), 400, 512, getDust("coke"));
		ArcFurnaceRecipe.addRecipe(new ItemStack(ingots.get(STEEL)), getDust("iron"), new ItemStack(slag), 400, 512, getDust("coke"));
		//Vanilla+IE Ores
		for(EnumMetals metal : ores.keySet())
			arcOutputModifier.put(metal.tagName(), new ItemStack(ingots.get(metal), 2));
		//IE Alloys
		addOreDictArcAlloyingRecipe(new ItemStack(ingots.get(CONSTANTAN), 2), "copper", 100, 512, getDust("nickel"));
		addOreDictArcAlloyingRecipe(new ItemStack(ingots.get(CONSTANTAN), 2), "nickel", 100, 512, getDust("copper"));
		addOreDictArcAlloyingRecipe(new ItemStack(ingots.get(ELECTRUM), 2), "gold", 100, 512, getDust("silver"));
		addOreDictArcAlloyingRecipe(new ItemStack(ingots.get(ELECTRUM), 2), "silver", 100, 512, getDust("gold"));
		//Common Alloys
		addOreDictArcAlloyingRecipe(getIngot("invar"), 3, "nickel", 200, 512, getDust("iron"), getDust("iron"));
		addOreDictArcAlloyingRecipe(getIngot("bronze"), 4, "tin", 200, 512, new IngredientStack(getDust("copper"), 3));
		addOreDictArcAlloyingRecipe(getIngot("brass"), 4, "zinc", 200, 512, new IngredientStack(getDust("copper"), 3));
		//addOreDictArcAlloyingRecipe(getIngot("blueAlloy"), 1, "silver", 100, 512, new IngredientStack(getDust("nikolite"), 4));
		//addOreDictArcAlloyingRecipe(getIngot("redAlloy"), 1, "copper", 100, 512, new IngredientStack(getDust("redstone"), 4));

		//Recycling
		/*TODO better, possibly tag-based system
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.itemWireCoil, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.itemDrillhead, 1, OreDictionary.WILDCARD_VALUE));

		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration0, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration1, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDecoration2, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDevice0, 1, OreDictionary.WILDCARD_VALUE));
		ArcFurnaceRecipe.allowItemForRecycling(new ItemStack(IEContent.blockMetalDevice1, 1, OreDictionary.WILDCARD_VALUE));
		 */
	}

	public static ArcFurnaceRecipe addArcRecipe(ItemStack output, Object input, int time, int energyPerTick, @Nonnull ItemStack slag, Object... additives)
	{
		return ArcFurnaceRecipe.addRecipe(output, input, slag, time, energyPerTick, additives);
	}

	public static void addArcOreSmelting(ItemStack output, String ore)
	{
		if(ApiUtils.isNonemptyItemTag(getOre(ore)))//ToDo this should allow blocks too
			addArcRecipe(output, getOre(ore), 200, 512, new ItemStack(slag)).setSpecialRecipeType("Ores");
	}

	public static void addOreDictArcAlloyingRecipe(ResourceLocation outName, int outSize, String inputName, int time, int energyPerTick, Object... additives)
	{
		if(!ApiUtils.isNonemptyItemTag(outName))
			return;
		ItemStack out = IEApi.getPreferredTagStack(outName);
		if(out.isEmpty())
			return;
		addOreDictArcAlloyingRecipe(Utils.copyStackWithAmount(out, outSize), inputName, time, energyPerTick, additives);
	}

	public static void addOreDictArcAlloyingRecipe(ItemStack out, String inputName, int time, int energyPerTick, Object... additives)
	{
		ArcFurnaceRecipe.addRecipe(out, getIngot(inputName), ItemStack.EMPTY, time, energyPerTick, additives).setSpecialRecipeType("Alloying");
		ArcFurnaceRecipe.addRecipe(out, getDust(inputName), ItemStack.EMPTY, time, energyPerTick, additives).setSpecialRecipeType("Alloying");
	}

	private static void addExcavatorVeins()
	{
		/*EXCAVATOR*/
		//TODO remove
		ExcavatorHandler.mineralVeinCapacity = IEConfig.MACHINES.excavator_depletion.get();
		ExcavatorHandler.mineralChance = IEConfig.MACHINES.excavator_chance.get();
		ExcavatorHandler.defaultDimensionBlacklist = ImmutableSet.of();//IEConfig.MACHINES.excavator_dimBlacklist.get();
		//TODO String sulfur = OreDictionary.doesOreNameExist(oreSulfur)?"oreSulfur: "dustSulfur;
		ResourceLocation invalid = new ResourceLocation("immersiveengineering:invalid");
		ResourceLocation sulfur = IERecipes.getDust("sulfur");
		ResourceLocation oreIron = IERecipes.getOre("iron");
		ResourceLocation oreNickel = IERecipes.getOre("nickel");
		ResourceLocation oreTin = IERecipes.getOre("tin");
		ResourceLocation oreGold = IERecipes.getOre("gold");
		ResourceLocation oreAluminum = IERecipes.getOre("aluminum");
		ResourceLocation oreTitanium = IERecipes.getOre("titanium");
		ResourceLocation oreCopper = IERecipes.getOre("copper");
		ResourceLocation orePlatinum = IERecipes.getOre("platinum");
		ResourceLocation oreUranium = IERecipes.getOre("uranium");
		ResourceLocation oreLead = IERecipes.getOre("lead");
		//TODO is this the correct name?
		ResourceLocation oreQuartz = IERecipes.getOre("quartz");
		ResourceLocation oreIridium = IERecipes.getOre("iridium");
		ResourceLocation orePlutonium = IERecipes.getOre("plutonium");
		ResourceLocation oreCertusQuartz = IERecipes.getOre("certus_quartz");
		ResourceLocation oreSilver = IERecipes.getOre("silver");
		ResourceLocation oreSulfur = IERecipes.getOre("sulfur");
		ResourceLocation oreLapis = IERecipes.getOre("lapis");
		ResourceLocation oreRedstone = IERecipes.getOre("redstone");
		ResourceLocation oreRuby = IERecipes.getOre("ruby");
		ResourceLocation oreCinnabar = IERecipes.getOre("cinnabar");
		ResourceLocation oreCoal = IERecipes.getOre("coal");
		ResourceLocation oreDiamond = IERecipes.getOre("diamond");
		ResourceLocation oreEmerald = IERecipes.getOre("emerald");
		ResourceLocation oreYellorium = IERecipes.getOre("yellorium");
		//TODO is this the correct name?
		ResourceLocation blockClay = IERecipes.getStorageBlock("clay");
		ResourceLocation sand = BlockTags.SAND.getId();
		ResourceLocation gravel = Tags.Blocks.GRAVEL.getId();
		ResourceLocation denseoreIron = invalid;
		ResourceLocation denseoreAluminum = invalid;
		ResourceLocation denseoreCopper = invalid;
		ResourceLocation denseoreTin = invalid;
		ResourceLocation denseoreGold = invalid;
		ResourceLocation denseoreNickel = invalid;
		ResourceLocation denseorePlatinum = invalid;
		ResourceLocation denseoreLead = invalid;
		ResourceLocation denseoreSilver = invalid;
		ResourceLocation denseoreLapis = invalid;
		ResourceLocation denseoreRedstone = invalid;
		ResourceLocation denseoreCoal = invalid;
		ResourceLocation denseoreUranium = invalid;

		ExcavatorHandler.addMineral("Iron", 25, .1f, new ResourceLocation[]{oreIron, oreNickel, oreTin, denseoreIron}, new float[]{.5f, .25f, .20f, .05f});
		ExcavatorHandler.addMineral("Magnetite", 25, .1f, new ResourceLocation[]{oreIron, oreGold}, new float[]{.85f, .15f});
		ExcavatorHandler.addMineral("Pyrite", 20, .1f, new ResourceLocation[]{oreIron, sulfur}, new float[]{.5f, .5f});
		ExcavatorHandler.addMineral("Bauxite", 20, .2f, new ResourceLocation[]{oreAluminum, oreTitanium, denseoreAluminum}, new float[]{.90f, .05f, .05f});
		ExcavatorHandler.addMineral("Copper", 30, .2f, new ResourceLocation[]{oreCopper, oreGold, oreNickel, denseoreCopper}, new float[]{.65f, .25f, .05f, .05f});
		ExcavatorHandler.addMineral("Cassiterite", 15, .2f, new ResourceLocation[]{oreTin, denseoreTin}, new float[]{.95f, .05f});
		ExcavatorHandler.addMineral("Gold", 20, .3f, new ResourceLocation[]{oreGold, oreCopper, oreNickel, denseoreGold}, new float[]{.65f, .25f, .05f, .05f});
		ExcavatorHandler.addMineral("Nickel", 20, .3f, new ResourceLocation[]{oreNickel, orePlatinum, oreIron, denseoreNickel}, new float[]{.85f, .05f, .05f, .05f});
		if(ApiUtils.isNonemptyBlockTag(orePlatinum))
			ExcavatorHandler.addMineral("Platinum", 5, .35f, new ResourceLocation[]{orePlatinum, oreNickel, oreIridium, denseorePlatinum}, new float[]{.40f, .30f, .15f, .1f});
		ExcavatorHandler.addMineral("Uranium", 10, .35f, new ResourceLocation[]{oreUranium, oreLead, orePlutonium, denseoreUranium}, new float[]{.55f, .3f, .1f, .05f})
				.addReplacement(oreUranium, oreYellorium);
		MineralMix mineralMix = ExcavatorHandler.addMineral("Quartzite", 5, .3f, new ResourceLocation[]{oreQuartz, oreCertusQuartz}, new float[]{.6f, .4f});
		mineralMix.dimensionWhitelist.add(DimensionType.THE_NETHER);
		ExcavatorHandler.addMineral("Galena", 15, .2f, new ResourceLocation[]{oreLead, oreSilver, oreSulfur, denseoreLead, denseoreSilver}, new float[]{.40f, .40f, .1f, .05f, .05f});
		ExcavatorHandler.addMineral("Lead", 10, .15f, new ResourceLocation[]{oreLead, oreSilver, denseoreLead}, new float[]{.55f, .4f, .05f});
		ExcavatorHandler.addMineral("Silver", 10, .2f, new ResourceLocation[]{oreSilver, oreLead, denseoreSilver}, new float[]{.55f, .4f, .05f});
		ExcavatorHandler.addMineral("Lapis", 10, .2f, new ResourceLocation[]{oreLapis, oreIron, sulfur, denseoreLapis}, new float[]{.65f, .275f, .025f, .05f});
		ExcavatorHandler.addMineral("Cinnabar", 15, .1f, new ResourceLocation[]{oreRedstone, denseoreRedstone, oreRuby, oreCinnabar, sulfur}, new float[]{.75f, .05f, .05f, .1f, .05f});
		ExcavatorHandler.addMineral("Coal", 25, .1f, new ResourceLocation[]{oreCoal, denseoreCoal, oreDiamond, oreEmerald}, new float[]{.92f, .1f, .015f, .015f});
		ExcavatorHandler.addMineral("Silt", 25, .05f, new ResourceLocation[]{blockClay, sand, gravel}, new float[]{.5f, .3f, .2f});
		ExcavatorHandler.addMineral("Igneous Rock", 25, .05f, new ResourceLocation[]{Blocks.GRANITE.getRegistryName(), Blocks.DIORITE.getRegistryName(), Blocks.ANDESITE.getRegistryName(), Tags.Blocks.OBSIDIAN.getId()}, new float[]{.3f, .3f, .3f, .1f});
	}

	private static void addMiscRecipes()
	{
		/*MULTIBLOCK RECIPES*/
		Tag<Block> coalBlock = Tags.Blocks.STORAGE_BLOCKS_COAL;
		Tag<Block> logWood = BlockTags.LOGS;
		ResourceLocation sand = BlockTags.SAND.getId();
		ResourceLocation gravel = Tags.Blocks.GRAVEL.getId();
		CokeOvenRecipe.addRecipe(new ItemStack(Ingredients.coalCoke), new ItemStack(Items.COAL), 1800, 500);
		CokeOvenRecipe.addRecipe(new ItemStack(StoneDecoration.coke), coalBlock, 1800*9, 5000);
		CokeOvenRecipe.addRecipe(new ItemStack(Items.CHARCOAL), logWood, 900, 250);

		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 80), ItemStack.EMPTY, Items.WHEAT_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 60), ItemStack.EMPTY, Items.BEETROOT_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 40), ItemStack.EMPTY, Items.PUMPKIN_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 20), ItemStack.EMPTY, Items.MELON_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 120), ItemStack.EMPTY, IEItems.Misc.hempSeeds, 6400);
		SqueezerRecipe.addRecipe(null, new ItemStack(Ingredients.dustHopGraphite, 1), new ItemStack(Ingredients.dustCoke, 8), 19200);
		/* TODO
		Fluid fluidBlood = FluidRegistry.getFluid("blood");
		if(fluidBlood!=null)
			SqueezerRecipe.addRecipe(new FluidStack(fluidBlood, 5), new ItemStack(Items.LEATHER), new ItemStack(Items.ROTTEN_FLESH), 6400);
		 */

		Tag<Item> potatoes = Tags.Items.CROPS_POTATO;
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.SUGAR_CANE, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.MELON_SLICE, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.APPLE, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, potatoes, 6400);

		RefineryRecipe.addRecipe(new FluidStack(fluidBiodiesel, 16), new FluidStack(fluidPlantoil, 8), new FluidStack(fluidEthanol, 8), 80);

		MixerRecipe.addRecipe(new FluidStack(fluidConcrete, 500), new FluidStack(Fluids.WATER, 500), new Object[]{sand, sand, Items.CLAY_BALL, gravel}, 3200);
		MixerRecipe.addRecipe(new FluidStack(fluidHerbicide, 500), new FluidStack(fluidEthanol, 500), new Object[]{IETags.saltpeterDust, IETags.getTagsFor(COPPER).dust}, 3200);

		BottlingMachineRecipe.addRecipe(new ItemStack(Blocks.WET_SPONGE, 1), new ItemStack(Blocks.SPONGE, 1), new FluidStack(Fluids.WATER, 1000));

		IECompatModule.doModulesRecipes();
	}
}
