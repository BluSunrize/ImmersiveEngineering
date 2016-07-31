package blusunrize.immersiveengineering.common.blocks;

import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockIEStairs extends ItemBlock
{
	public ItemBlockIEStairs(Block b)
	{
		super(b);
	}

	@Override
	public int getMetadata (int damageValue)
	{
		return damageValue;
	}
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List itemList)
	{
		this.block.getSubBlocks(item, tab, itemList);
	}
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		return super.getUnlocalizedName(itemstack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
		if(((BlockIEStairs)block).hasFlavour)
			list.add(I18n.format(Lib.DESC_FLAVOUR+((BlockIEStairs)block).name));
	}
}