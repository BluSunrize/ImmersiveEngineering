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
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.crafting.MetalPressPackingRecipe;
import blusunrize.immersiveengineering.common.crafting.MetalPressUnpackingRecipe;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEBaseItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Molds;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.common.blocks.EnumMetals.*;
import static blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals.ores;
import static blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals.storage;
import static blusunrize.immersiveengineering.common.items.IEItems.Ingredients.*;
import static blusunrize.immersiveengineering.common.items.IEItems.Metals.ingots;
import static blusunrize.immersiveengineering.common.items.IEItems.Molds.*;

public class IERecipes
{

	public static void initCraftingRecipes(IForgeRegistry<?> registry)
	{
		//TODO move to JSON recipes
		/*
		//Loop, special or colouration recipes
		registry.register(new RecipeBannerAdvanced().setRegistryName(ImmersiveEngineering.MODID, "banners"));
		registry.register(new RecipeJerrycan(id).setRegistryName(ImmersiveEngineering.MODID, "jerrycan"));
		registry.register(new RecipeShaderBags(id).setRegistryName(ImmersiveEngineering.MODID, "shader_bags"));
		registry.register(new RecipeEarmuffs().setRegistryName(ImmersiveEngineering.MODID, "earmuffs"));
		registry.register(new RecipePowerpack(id).setRegistryName(ImmersiveEngineering.MODID, "powerpack"));
		final ItemStack stripCurtain = new ItemStack(IEContent.blockClothDevice, 1, BlockTypes_ClothDevice.STRIPCURTAIN.getMeta());
		registry.register(new RecipeRGBColouration((s) -> (OreDictionary.itemMatches(stripCurtain, s, true)),
				(s) -> (ItemNBTHelper.hasKey(s, "colour")?ItemNBTHelper.getInt(s, "colour"): 0xffffff),
				(s, i) -> ItemNBTHelper.putInt(s, "colour", i))
				.setRegistryName(ImmersiveEngineering.MODID, "stripcurtain_colour"));
		*/
	}

	public static void initBlueprintRecipes()
	{
		//
		//MATERIALS
		//
		Tag<Item> ironPlate = IETags.getTagsFor(IRON).plate;
		Tag<Item> steelPlate = IETags.getTagsFor(STEEL).plate;
		Tag<Item> nickelPlate = IETags.getTagsFor(NICKEL).plate;
		Tag<Item> copperIngot = IETags.getTagsFor(COPPER).ingot;
		Tag<Item> glassBlock = Tags.Items.GLASS;
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(componentIron), ironPlate, ironPlate, copperIngot);
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(componentSteel), steelPlate, steelPlate, copperIngot);
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(electronTube, 3), "blockGlass", "plateNickel", "wireCopper", "dustRedstone");
		BlueprintCraftingRecipe.addRecipe("components", new ItemStack(circuitBoard),
				new ItemStack(StoneDecoration.insulatingGlass), "plateCopper", "electronTube", "electronTube");

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
						steelPlate, steelPlate, steelPlate, steelPlate, steelPlate, new ItemStack(Tools.hammer));

		//
		//BULLETS
		//
		//Casull
		ItemStack bullet = BulletHandler.getBulletStack(BulletItem.CASULL);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, "nuggetLead", "nuggetLead");
		//Piercing
		bullet = BulletHandler.getBulletStack(BulletItem.ARMOR_PIERCING);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, "nuggetSteel", "nuggetSteel", "nuggetConstantan", "nuggetConstantan");
		if(ApiUtils.isNonemptyItemTag(getNugget("tungsten")))
			BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, "nuggetTungsten", "nuggetTungsten");
		// We don't have depleted stuff atm
		//		if(ApiUtils.isNonemptyItemTag("nuggetCyanite"))
		//			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetCyanite","nuggetCyanite");
		//		else if(ApiUtils.isNonemptyItemTag("ingotCyanite"))
		//			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,3,3), new ItemStack(IEContent.itemBullet,3,0),new ItemStack(Items.gunpowder,3),"ingotCyanite");
		//Silver
		bullet = BulletHandler.getBulletStack(BulletItem.SILVER);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, "nuggetLead", "nuggetLead", "nuggetSilver");
		//Buckshot
		bullet = BulletHandler.getBulletStack(BulletItem.BUCKSHOT);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyShell), Items.GUNPOWDER, getDust("iron"));
		//HE
		bullet = BulletHandler.getBulletStack(BulletItem.HIGH_EXPLOSIVE);
		BlueprintCraftingRecipe.addRecipe("bullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, Blocks.TNT);
		//Dragonsbreath
		bullet = BulletHandler.getBulletStack(BulletItem.DRAGONS_BREATH);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet, new ItemStack(emptyShell), Items.GUNPOWDER, "dustAluminum", "dustAluminum");
		//Potion
		bullet = BulletHandler.getBulletStack(BulletItem.POTION);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet, new ItemStack(emptyCasing), Items.GUNPOWDER, Items.GLASS_BOTTLE);
		//Flare
		bullet = BulletHandler.getBulletStack(BulletItem.FLARE);
		ItemNBTHelper.putInt(bullet, "flareColour", 0xcc2e06);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(emptyShell), Items.GUNPOWDER, "dustAluminum", "dyeRed");
		ItemNBTHelper.putInt(bullet, "flareColour", 0x2ca30b);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(emptyShell), Items.GUNPOWDER, "dustAluminum", "dyeGreen");
		ItemNBTHelper.putInt(bullet, "flareColour", 0xffff82);
		BlueprintCraftingRecipe.addRecipe("specialBullet", bullet.copy(), new ItemStack(emptyShell), Items.GUNPOWDER, "dustAluminum", "dyeYellow");

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

		BlueprintCraftingRecipe.addRecipe("electrode", new ItemStack(Misc.graphiteElectrode), "ingotHOPGraphite", "ingotHOPGraphite", "ingotHOPGraphite", "ingotHOPGraphite");
		BlueprintCraftingRecipe.addVillagerTrade("electrode", new ItemStack(Items.EMERALD, 1));
	}

	public static void initBlastFurnaceRecipes()
	{
		Tag<Item> ironIngot = IETags.getTagsFor(IRON).ingot;
		Tag<Block> ironBlock = IETags.getTagsFor(IRON).storage;
		BlastFurnaceRecipe.addRecipe(new ItemStack(ingots.get(STEEL)), ironIngot, 1200, new ItemStack(slag));
		BlastFurnaceRecipe.addRecipe(new ItemStack(storage.get(STEEL)), ironBlock, 1200*9, new ItemStack(slag, 9));

		BlastFurnaceRecipe.addBlastFuel(IETags.coalCoke, 1200);
		BlastFurnaceRecipe.addBlastFuel(IETags.coalCokeBlock, 1200*10);
		/*TODO
		BlastFurnaceRecipe.addBlastFuel("charcoal", 300);
		BlastFurnaceRecipe.addBlastFuel("blockCharcoal", 300*10);
		 */
	}

	public static void initMetalPressRecipes()
	{
		//Bullet casing
		MetalPressRecipe.addRecipe(new ItemStack(emptyCasing, 2), IETags.getTagsFor(COPPER).ingot, new ItemStack(moldBulletCasing), 2400);

		//Damaged Graphite Electrodes
		ItemStack shoddyElectrode = new ItemStack(Misc.graphiteElectrode);
		shoddyElectrode.setDamage(IEConfig.MACHINES.arcfurnace_electrodeDamage.get()/2);
		MetalPressRecipe.addRecipe(shoddyElectrode, IETags.hopGraphiteIngot, new ItemStack(moldRod), 4800).setInputSize(4);

		//Slicing Melons
		MetalPressRecipe.addRecipe(new ItemStack(Items.MELON, 9), new ItemStack(Blocks.MELON), new ItemStack(moldUnpacking), 3200);

		//Packing & Unpacking
		ComparableItemStack pack2x2 = ApiUtils.createComparableItemStack(new ItemStack(moldPacking4), false);
		MetalPressRecipe.recipeList.put(pack2x2, new MetalPressPackingRecipe(pack2x2, 3200, 2));
		ComparableItemStack pack3x3 = ApiUtils.createComparableItemStack(new ItemStack(moldPacking9), false);
		MetalPressRecipe.recipeList.put(pack3x3, new MetalPressPackingRecipe(pack3x3, 3200, 3));
		ComparableItemStack unpack = ApiUtils.createComparableItemStack(new ItemStack(moldUnpacking), false);
		MetalPressRecipe.recipeList.put(unpack, new MetalPressUnpackingRecipe(unpack, 3200));
	}

	public static HashMap<String, ItemStack> oreOutputModifier = new HashMap<String, ItemStack>();
	public static HashMap<String, Object[]> oreOutputSecondaries = new HashMap<String, Object[]>();
	public static ArrayList<String> hammerCrushingList = new ArrayList<String>();

	public static void initCrusherRecipes()
	{
		//TODO replace oredict names with tags
		if(true) return;
		oreOutputSecondaries.put("Iron", new Object[]{getDust("nickel"), .1f});
		oreOutputSecondaries.put("Gold", new Object[]{"crystalCinnabar", .05f});
		oreOutputSecondaries.put("Copper", new Object[]{getDust("gold"), .1f});
		oreOutputSecondaries.put("Lead", new Object[]{getDust("silver"), .1f});
		oreOutputSecondaries.put("Silver", new Object[]{"dustLead", .1f});
		oreOutputSecondaries.put("Nickel", new Object[]{"dustPlatinum", .1f});

		oreOutputModifier.put("Lapis", new ItemStack(Items.LAPIS_LAZULI, 9));
		oreOutputSecondaries.put("Lapis", new Object[]{"dustSulfur", .15f});
		oreOutputModifier.put("Diamond", new ItemStack(Items.DIAMOND, 2));
		oreOutputModifier.put("Redstone", new ItemStack(Items.REDSTONE, 6));
		oreOutputSecondaries.put("Redstone", new Object[]{"crystalCinnabar", .25f});
		oreOutputModifier.put("Emerald", new ItemStack(Items.EMERALD, 2));
		oreOutputModifier.put("Quartz", new ItemStack(Items.QUARTZ, 3));
		oreOutputSecondaries.put("Quartz", new Object[]{"dustSulfur", .15f});
		oreOutputModifier.put("Coal", new ItemStack(Items.COAL, 4));

		oreOutputSecondaries.put("Platinum", new Object[]{getDust("nickel"), .1f});
		oreOutputSecondaries.put("Tungsten", new Object[]{"dustManganese", .1f});
		oreOutputSecondaries.put("Uranium", new Object[]{"dustLead", .1f});
		oreOutputSecondaries.put("Yellorium", new Object[]{"dustLead", .1f});
		oreOutputSecondaries.put("Plutonium", new Object[]{"dustUranium", .1f});
		Item item = GameRegistry.findRegistry(Item.class).getValue(new ResourceLocation("IC2", "itemOreIridium"));
		oreOutputSecondaries.put("Osmium", new Object[]{item, .01f});
		oreOutputSecondaries.put("Iridium", new Object[]{"dustPlatium", .1f});
		oreOutputSecondaries.put("FzDarkIron", new Object[]{getDust("iron"), .1f});
		item = GameRegistry.findRegistry(Item.class).getValue(new ResourceLocation("Railcraft", "firestone.raw"));
		if(item!=null)
			oreOutputModifier.put("Firestone", new ItemStack(item));
		oreOutputSecondaries.put("Nikolite", new Object[]{Items.DIAMOND, .025f});

		addCrusherRecipe(new ItemStack(Blocks.GRAVEL), "cobblestone", 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), Blocks.GRAVEL, 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), "itemSlag", 1600);
		addCrusherRecipe(new ItemStack(Blocks.SAND), "blockGlass", 3200);
		addCrusherRecipe(new ItemStack(Blocks.SAND, 2), "sandstone", 1600, new ItemStack(dustSaltpeter), .5f);
		addCrusherRecipe(new ItemStack(Items.CLAY_BALL, 4), "blockClay", 1600);
		addCrusherRecipe(new ItemStack(Items.QUARTZ, 4), "blockQuartz", 3200);
		addCrusherRecipe(new ItemStack(Items.GLOWSTONE_DUST, 4), "glowstone", 3200);
		addCrusherRecipe(new ItemStack(Items.BLAZE_POWDER, 4), "rodBlaze", 3200, new ItemStack(dustSulfur), .5f);
		addCrusherRecipe(new ItemStack(Items.BONE_MEAL), Items.BONE, 3200);
		addCrusherRecipe(new ItemStack(dustCoke), "fuelCoke", 2400);
		addCrusherRecipe(new ItemStack(dustCoke, 9), "blockFuelCoke", 4800);
		addItemToOreDictCrusherRecipe(getDust("coal"), 1, new ItemStack(Items.COAL), 2400);
		addItemToOreDictCrusherRecipe(getDust("obsidian"), 4, Blocks.OBSIDIAN, 6000);
		//TODO is there a better way than enumerating these?
		//for(int i = 0; i < 16; i++)
		//{
		//	CrusherRecipe r = CrusherRecipe.addRecipe(new ItemStack(Items.STRING, 4), new ItemStack(Blocks.WOOL, 1, i), 3200);
		//	if(i!=0)
		//		r.addToSecondaryOutput(new ItemStack(Items.DYE, 1, 15-i), .05f);
		//}
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

	public static void addTagBasedRecipes()
	{
		//TODO remove recipes from previous world loads!
		boolean allowHammerCrushing = !IEConfig.TOOLS.disableHammerCrushing.get();
		ComparableItemStack compMoldPlate = ApiUtils.createComparableItemStack(new ItemStack(moldPlate), false);
		ComparableItemStack compMoldGear = ApiUtils.createComparableItemStack(new ItemStack(moldGear), false);
		ComparableItemStack compMoldRod = ApiUtils.createComparableItemStack(new ItemStack(moldRod), false);
		ComparableItemStack compMoldWire = ApiUtils.createComparableItemStack(new ItemStack(Molds.moldWire), false);

		for(Entry<ResourceLocation, Tag<Block>> tag : BlockTags.getCollection().getTagMap().entrySet())
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
								ItemStack preferredDust = IEApi.getPreferredTagStack(dust);
								out = Utils.copyStackWithAmount(preferredDust, 2);
								//TODO custom singleton recipe or similar
								//if(allowHammerCrushing)
								//{
								//	addShapelessOredictRecipe("hammercrushing_"+baseName, preferredDust, tag, new ItemStack(IEContent.itemTool));
								//	hammerCrushingList.add(baseName);
								//}
							}
						}
					}
					if(out!=null&&!out.isEmpty())
					{
						Object[] secondaries = oreOutputSecondaries.get(baseName);
						Object s = secondaries!=null&&secondaries.length > 1?secondaries[0]: null;
						float f = secondaries!=null&&secondaries.length > 1&&secondaries[1] instanceof Float?(Float)secondaries[1]: 0;
						addOreProcessingRecipe(out, baseName, 6000, true, s, f);
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
			}

		for(Entry<ResourceLocation, Tag<Item>> tag : ItemTags.getCollection().getTagMap().entrySet())
			if(!tag.getValue().getAllElements().isEmpty()&&tag.getKey().getNamespace().equals("forge"))
			{
				String path = tag.getKey().getPath();
				if(path.startsWith("gems/"))
				{
					String ore = path.substring("gems/".length());
					ResourceLocation dust = getDust(ore);
					if(ApiUtils.isNonemptyItemTag(dust))
						addCrusherRecipe(IEApi.getPreferredTagStack(dust), getGem(ore), 6000, null, 0);
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
						addCrusherRecipe(IEApi.getPreferredTagStack(getDust(ore)), getIngot(ore), 3600, null, 0);
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
				else if(path.startsWith("wires/"))
				{
					String ore = path.substring("wires/".length());
					if(ApiUtils.isNonemptyItemTag(getIngot(ore)))
						MetalPressRecipe.addRecipe(Utils.copyStackWithAmount(IEApi.getPreferredTagStack(tag.getKey()), 2),
								getIngot(ore), compMoldWire, 2400);
				}
			}
		//TODO Config.manual_bool.put("crushingOreRecipe", !hammerCrushingList.isEmpty());
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
		if(ingot&&ApiUtils.isNonemptyItemTag(getIngot(ore)))
			addCrusherRecipe(Utils.copyStackWithAmount(output, output.getCount()/2), getIngot(ore), (int)(energy*.6f));
		if(ApiUtils.isNonemptyItemTag(getOre(ore)))
			addCrusherRecipe(output, getOre(ore), energy, secondary, secChance);
	}

	public static void addOreDictCrusherRecipe(String ore, Object secondary, float chance)
	{
		if(!ApiUtils.isNonemptyItemTag(getDust(ore)))
			return;
		ItemStack dust = IEApi.getPreferredTagStack(getDust(ore));
		if(dust.isEmpty())
			return;
		if(ApiUtils.isNonemptyItemTag(getOre(ore)))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 2), getOre(ore), 6000, secondary, chance);
		if(ApiUtils.isNonemptyItemTag(getIngot(ore)))
			addCrusherRecipe(Utils.copyStackWithAmount(dust, 1), getIngot(ore), 3600);
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

	public static void initAlloySmeltingRecipes()
	{
		//IE Alloys
		addAlloyingRecipe(new ItemStack(ingots.get(CONSTANTAN), 2), "copper", 1, "nickel", 1, 200);
		addAlloyingRecipe(new ItemStack(ingots.get(ELECTRUM), 2), "gold", 1, "silver", 1, 200);
		//Common Alloys
		addOreDictAlloyingRecipe(getIngot("invar"), 3, "iron", 2, "nickel", 1, 200);
		addOreDictAlloyingRecipe(getIngot("bronze"), 4, "copper", 3, "tin", 1, 200);
		addOreDictAlloyingRecipe(getIngot("brass"), 4, "copper", 3, "zinc", 1, 200);
		//TODO addOreDictAlloyingRecipe(getIngot("blueAlloy"), 1, "silver", 1, "nikolite", 4, 200);
		//TODO addOreDictAlloyingRecipe(getIngot("redAlloy"), 1, "copper", 1, "redstone", 4, 200);

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
		AlloyRecipe.addRecipe(output, new IngredientStack(getIngot(input0), size0), new IngredientStack(getIngot(input1), size1), time);
		AlloyRecipe.addRecipe(output, new IngredientStack(getDust(input0), size0), new IngredientStack(getDust(input1), size1), time);
		AlloyRecipe.addRecipe(output, new IngredientStack(getIngot(input0), size0), new IngredientStack(getDust(input1), size1), time);
		AlloyRecipe.addRecipe(output, new IngredientStack(getDust(input0), size0), new IngredientStack(getIngot(input1), size1), time);
	}

	public static HashMap<String, ItemStack> arcOutputModifier = new HashMap<>();
	public static HashSet<String> arcBlacklist = new HashSet<>();

	public static void initArcSmeltingRecipes()
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
		if(ApiUtils.isNonemptyItemTag(getOre(ore)))
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
}
