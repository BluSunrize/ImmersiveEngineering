package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;

public class ItemBlockMetalDecorations extends ItemBlockIEBase
{
	public ItemBlockMetalDecorations(Block b)
	{
		super(b);
	}


	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		if(!ret)
			return ret;
		if(world.getTileEntity(x, y, z) instanceof TileEntityConnectorLV)
			((TileEntityConnectorLV)world.getTileEntity(x, y, z)).facing = ForgeDirection.getOrientation(side).getOpposite().ordinal();
		return ret;
	}
}