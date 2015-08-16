package blusunrize.immersiveengineering.common.blocks.stone;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;

public class ItemBlockStoneDevices extends ItemBlockIEBase
{
	public ItemBlockStoneDevices(Block b)
	{
		super(b);
	}


	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
		if(stack.getItemDamage()<4)
			list.add("This item is deprecated. Hold it in your inventory to update it.");
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean hand)
	{
		if(ent instanceof EntityPlayer)
		{
			int meta = stack.getItemDamage();
			if(meta<4)
				((EntityPlayer)ent).inventory.setInventorySlotContents(slot, new ItemStack(IEContent.blockStoneDecoration,stack.stackSize,stack.getItemDamage()));
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		return ret;
	}
}