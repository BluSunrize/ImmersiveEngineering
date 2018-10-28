/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockIEStairs extends ItemBlock
{
	public ItemBlockIEStairs(Block b)
	{
		super(b);
	}

	@Override
	public int getMetadata(int damageValue)
	{
		return damageValue;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> itemList)
	{
		if(this.isInCreativeTab(tab))
			this.block.getSubBlocks(tab, itemList);
	}

	@Override
	public String getTranslationKey(ItemStack itemstack)
	{
		return super.getTranslationKey(itemstack);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag tooltipFlag)
	{
		if(((BlockIEStairs)block).hasFlavour)
			tooltip.add(I18n.format(Lib.DESC_FLAVOUR+((BlockIEStairs)block).name));
	}
}