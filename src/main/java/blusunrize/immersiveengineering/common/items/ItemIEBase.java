/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IItemDamageableIE;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;

import javax.annotation.Nullable;
import java.util.Random;

public class ItemIEBase extends Item implements IColouredItem
{
	public String itemName;
	private int burnTime = -1;

	public ItemIEBase(String name, Properties props)
	{
		super(props.group(ImmersiveEngineering.itemGroup));
		this.itemName = name;
		IEContent.registeredIEItems.add(this);
	}

	public ItemIEBase setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public int getBurnTime(ItemStack itemStack)
	{
		return burnTime;
	}

	protected void damageIETool(ItemStack stack, int amount, Random rand, @Nullable EntityPlayer player)
	{
		if(amount <= 0||!(this instanceof IItemDamageableIE))
			return;

		int unbreakLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
		for(int i = 0; unbreakLevel > 0&&i < amount; i++)
			if(EnchantmentDurability.negateDamage(stack, unbreakLevel, rand))
				amount--;
		if(amount <= 0)
			return;

		int curDamage = ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE);
		curDamage += amount;

		if(player instanceof EntityPlayerMP)
			CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger((EntityPlayerMP)player, stack, curDamage);

		if(curDamage >= ((IItemDamageableIE)this).getItemDamageIE(stack))
		{
			if(player!=null)
			{
				player.renderBrokenItemStack(stack);
				player.addStat(StatList.ITEM_BROKEN.get(this));
			}
			stack.shrink(1);
			return;
		}
		ItemNBTHelper.setInt(stack, Lib.NBT_DAMAGE, curDamage);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return this instanceof IItemDamageableIE&&((IItemDamageableIE)this).getItemDamageIE(stack) > 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		if(!(this instanceof IItemDamageableIE))
			return 0;
		double max = (double)((IItemDamageableIE)this).getMaxDamageIE(stack);
		return ((IItemDamageableIE)this).getItemDamageIE(stack)/max;
	}
}
