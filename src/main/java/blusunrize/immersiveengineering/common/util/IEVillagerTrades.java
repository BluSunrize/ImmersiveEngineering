/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author BluSunrize - 23.07.2016
 */
public class IEVillagerTrades
{
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
