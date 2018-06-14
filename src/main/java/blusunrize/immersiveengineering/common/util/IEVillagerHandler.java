/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.world.VillageEngineersHouse;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author BluSunrize - 23.07.2016
 */
public class IEVillagerHandler
{
	private static final VillagerRegistry villageRegistry = VillagerRegistry.instance();
	public static VillagerRegistry.VillagerProfession villagerProfession_engineer;

	public static void initIEVillagerHouse()
	{
		if(!IEConfig.villagerHouse)
			return;

		villageRegistry.registerVillageCreationHandler(new VillageEngineersHouse.VillageManager());
		MapGenStructureIO.registerStructureComponent(VillageEngineersHouse.class, ImmersiveEngineering.MODID+":EngineersHouse");
	}

	public static void initIEVillagerTrades()
	{
		if(!IEConfig.enableVillagers)
			return;

		villagerProfession_engineer = new VillagerRegistry.VillagerProfession(ImmersiveEngineering.MODID+":engineer", "immersiveengineering:textures/models/villager_engineer.png", "immersiveengineering:textures/models/villager_engineer_zombie.png");
		ForgeRegistries.VILLAGER_PROFESSIONS.register(villagerProfession_engineer);

		/* Engineer
		 * Deals in treated wood, later metal rods, scaffold and concrete
		 */
		VillagerRegistry.VillagerCareer career_engineer = new VillagerRegistry.VillagerCareer(villagerProfession_engineer, ImmersiveEngineering.MODID+".engineer");
		career_engineer.addTrade(1,
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 0), new EntityVillager.PriceInfo(8, 16)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.blockWoodenDecoration, 1, 1), new EntityVillager.PriceInfo(-10, -6)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.blockClothDevice, 1, 1), new EntityVillager.PriceInfo(-3, -1))
		);
		career_engineer.addTrade(2,
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 1), new EntityVillager.PriceInfo(2, 6)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.blockMetalDecoration1, 1, 1), new EntityVillager.PriceInfo(-8, -4)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.blockMetalDecoration1, 1, 5), new EntityVillager.PriceInfo(-8, -4))
		);
		career_engineer.addTrade(3,
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 2), new EntityVillager.PriceInfo(2, 6)),
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 7), new EntityVillager.PriceInfo(4, 8)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.blockStoneDecoration, 1, 5), new EntityVillager.PriceInfo(-6, -2))
		);

		/* Machinist
		 * Sells tools, metals, blueprints and drillheads
		 */
		VillagerRegistry.VillagerCareer career_machinist = new VillagerRegistry.VillagerCareer(villagerProfession_engineer, ImmersiveEngineering.MODID+".machinist");
		career_machinist.addTrade(1,
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 6), new EntityVillager.PriceInfo(8, 16)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemTool, 1, 0), new EntityVillager.PriceInfo(4, 7))
		);
		career_machinist.addTrade(2,
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMetal, 1, 0), new EntityVillager.PriceInfo(4, 6)),
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMetal, 1, 1), new EntityVillager.PriceInfo(4, 6)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemMaterial, 1, 9), new EntityVillager.PriceInfo(1, 3))
		);
		career_machinist.addTrade(3,
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemToolbox, 1, 0), new EntityVillager.PriceInfo(6, 8)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemMaterial, 1, 10), new EntityVillager.PriceInfo(1, 3)),
				new IEVillagerHandler.ItemstackForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("specialBullet"), new EntityVillager.PriceInfo(5, 9))
		);
		career_machinist.addTrade(4,
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemDrillhead, 1, 0), new EntityVillager.PriceInfo(28, 40)),
				new IEVillagerHandler.ItemstackForEmerald(IEContent.itemEarmuffs, new EntityVillager.PriceInfo(4, 9))
		);
		career_machinist.addTrade(5,
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemDrillhead, 1, 1), new EntityVillager.PriceInfo(32, 48)),
				new IEVillagerHandler.ItemstackForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("electrode"), new EntityVillager.PriceInfo(12, 24))
		);

		/* Electrician
		 * Sells wires, tools and the faraday suit
		 */
		VillagerRegistry.VillagerCareer career_electrician = new VillagerRegistry.VillagerCareer(villagerProfession_engineer, ImmersiveEngineering.MODID+".electrician");
		career_electrician.addTrade(1,
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 20), new EntityVillager.PriceInfo(8, 16)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemTool, 1, 1), new EntityVillager.PriceInfo(4, 7)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemWireCoil, 1, 0), new EntityVillager.PriceInfo(-4, -2))
		);
		career_electrician.addTrade(2,
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 21), new EntityVillager.PriceInfo(6, 12)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemTool, 1, 2), new EntityVillager.PriceInfo(4, 7)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemWireCoil, 1, 1), new EntityVillager.PriceInfo(-4, -1))
		);
		career_electrician.addTrade(3,
				new IEVillagerHandler.EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 22), new EntityVillager.PriceInfo(4, 8)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemWireCoil, 1, 2), new EntityVillager.PriceInfo(-2, -1)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemToolUpgrades, 1, 6), new EntityVillager.PriceInfo(8, 12))
		);
		career_electrician.addTrade(4,
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemToolUpgrades, 1, 9), new EntityVillager.PriceInfo(8, 12)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemFluorescentTube), new EntityVillager.PriceInfo(8, 12)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemsFaradaySuit[0]), new EntityVillager.PriceInfo(5, 7)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemsFaradaySuit[1]), new EntityVillager.PriceInfo(9, 11)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemsFaradaySuit[2]), new EntityVillager.PriceInfo(5, 7)),
				new IEVillagerHandler.ItemstackForEmerald(new ItemStack(IEContent.itemsFaradaySuit[3]), new EntityVillager.PriceInfo(11, 15))
		);

		/* Outfitter
		 * Sells Shaderbags
		 */
		VillagerRegistry.VillagerCareer career_outfitter = new VillagerRegistry.VillagerCareer(villagerProfession_engineer, ImmersiveEngineering.MODID+".outfitter");

		ItemStack bag_common = new ItemStack(IEContent.itemShaderBag);
		ItemNBTHelper.setString(bag_common, "rarity", EnumRarity.COMMON.toString());
		ItemStack bag_uncommon = new ItemStack(IEContent.itemShaderBag);
		ItemNBTHelper.setString(bag_uncommon, "rarity", EnumRarity.UNCOMMON.toString());
		ItemStack bag_rare = new ItemStack(IEContent.itemShaderBag);
		ItemNBTHelper.setString(bag_rare, "rarity", EnumRarity.RARE.toString());

		career_outfitter.addTrade(1,
				new IEVillagerHandler.ItemstackForEmerald(bag_common, new EntityVillager.PriceInfo(8, 16))
		);
		career_outfitter.addTrade(2,
				new IEVillagerHandler.ItemstackForEmerald(bag_uncommon, new EntityVillager.PriceInfo(12, 20))
		);
		career_outfitter.addTrade(3,
				new IEVillagerHandler.ItemstackForEmerald(bag_rare, new EntityVillager.PriceInfo(16, 24))
		);
	}

	public static class EmeraldForItemstack implements EntityVillager.ITradeList
	{
		public ItemStack buyingItem;
		public EntityVillager.PriceInfo buyAmounts;

		public EmeraldForItemstack(@Nonnull ItemStack item, @Nonnull EntityVillager.PriceInfo buyAmounts)
		{
			this.buyingItem = item;
			this.buyAmounts = buyAmounts;
		}

		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			recipeList.add(new MerchantRecipe(Utils.copyStackWithAmount(this.buyingItem, this.buyAmounts.getPrice(random)), Items.EMERALD));
		}
	}

	public static class ItemstackForEmerald implements EntityVillager.ITradeList
	{
		public ItemStack sellingItem;
		public EntityVillager.PriceInfo priceInfo;

		public ItemstackForEmerald(Item par1Item, EntityVillager.PriceInfo priceInfo)
		{
			this.sellingItem = new ItemStack(par1Item);
			this.priceInfo = priceInfo;
		}

		public ItemstackForEmerald(ItemStack stack, EntityVillager.PriceInfo priceInfo)
		{
			this.sellingItem = stack;
			this.priceInfo = priceInfo;
		}

		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			int i = 1;
			if(this.priceInfo!=null)
				i = this.priceInfo.getPrice(random);
			ItemStack itemstack;
			ItemStack itemstack1;
			if(i < 0)
			{
				itemstack = new ItemStack(Items.EMERALD);
				itemstack1 = Utils.copyStackWithAmount(sellingItem, -i);
			}
			else
			{
				itemstack = new ItemStack(Items.EMERALD, i, 0);
				itemstack1 = Utils.copyStackWithAmount(sellingItem, 1);
			}
			recipeList.add(new MerchantRecipe(itemstack, itemstack1));
		}
	}
}
