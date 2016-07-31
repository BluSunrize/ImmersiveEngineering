package blusunrize.immersiveengineering.common.util;

import com.sun.istack.internal.NotNull;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

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

		public EmeraldForItemstack(@NotNull ItemStack item, @NotNull EntityVillager.PriceInfo buyAmounts)
		{
			this.buyingItem = item;
			this.buyAmounts = buyAmounts;
		}

		@Override
		public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random)
		{
			recipeList.add(new MerchantRecipe(Utils.copyStackWithAmount(this.buyingItem, this.buyAmounts.getPrice(random)), Items.EMERALD));
		}
	}
}
