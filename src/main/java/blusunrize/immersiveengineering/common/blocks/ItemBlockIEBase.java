package blusunrize.immersiveengineering.common.blocks;

import java.util.List;

import blusunrize.immersiveengineering.common.util.Lib;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class ItemBlockIEBase extends ItemBlock
{
	public ItemBlockIEBase(Block b)
	{
		super(b);
		if(((BlockIEBase)b).subNames.length>1)
			setHasSubtypes(true);
	}

	@Override
	public int getMetadata (int damageValue)
	{
		return damageValue;
	}
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List itemList)
	{
		this.field_150939_a.getSubBlocks(item, tab, itemList);
	}
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		return getUnlocalizedName()+"."+((BlockIEBase)field_150939_a).subNames[ Math.min(((BlockIEBase)field_150939_a).subNames.length-1, itemstack.getItemDamage())];
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
		if(((BlockIEBase)field_150939_a).hasFlavour)
			list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+((BlockIEBase)field_150939_a).name+"."+((BlockIEBase)field_150939_a).subNames[ Math.min(((BlockIEBase)field_150939_a).subNames.length-1, stack.getItemDamage())]));
	}
}