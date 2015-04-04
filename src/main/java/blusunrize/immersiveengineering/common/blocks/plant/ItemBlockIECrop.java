package blusunrize.immersiveengineering.common.blocks.plant;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockIECrop extends ItemBlock
{
	public ItemBlockIECrop(Block b)
	{
		super(b);
		if(((BlockIECrop)b).subNames.length>1)
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
		return getUnlocalizedName()+"."+((BlockIECrop)field_150939_a).subNames[itemstack.getItemDamage()];
	}
}
