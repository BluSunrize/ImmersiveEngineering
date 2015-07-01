package blusunrize.immersiveengineering.common.blocks.stone;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;

public class ItemBlockStoneDevices extends ItemBlockIEBase
{
	public ItemBlockStoneDevices(Block b)
	{
		super(b);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		return ret;
	}
}