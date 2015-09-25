package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.List;

import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;

public class ItemBlockMetalDevices2 extends ItemBlockIEBase
{
	public ItemBlockMetalDevices2(Block b)
	{
		super(b);
	}


	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;

		if(meta==BlockMetalDevices2.META_fluidPump)
			if(!world.isAirBlock(x, y+1, z))
				return false;

		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		if(!ret) return false;
		if(world.getTileEntity(x, y, z) instanceof TileEntityBreakerSwitch)
		{
			if(side<2)
			{
				((TileEntityBreakerSwitch)world.getTileEntity(x, y, z)).sideAttached = ForgeDirection.OPPOSITES[side]+1;
				((TileEntityBreakerSwitch)world.getTileEntity(x, y, z)).facing = f;
			}
			else
				((TileEntityBreakerSwitch)world.getTileEntity(x, y, z)).facing = ForgeDirection.OPPOSITES[side];
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityFloodLight)
		{
			((TileEntityFloodLight)world.getTileEntity(x, y, z)).facing = f;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityFluidPump)
		{
			((TileEntityFluidPump)world.getTileEntity(x, y, z)).dummy = false;
			world.setBlock(x, y+1, z, IEContent.blockMetalDevice2, BlockMetalDevices2.META_fluidPump, 3);
		}
		return ret;
	}
}