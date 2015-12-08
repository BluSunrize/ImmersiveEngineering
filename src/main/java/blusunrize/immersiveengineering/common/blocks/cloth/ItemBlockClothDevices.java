package blusunrize.immersiveengineering.common.blocks.cloth;

import java.util.List;

import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemBlockClothDevices extends ItemBlockIEBase
{
	public ItemBlockClothDevices(Block b)
	{
		super(b);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String s = super.getItemStackDisplayName(stack);
		if(ItemNBTHelper.hasKey(stack, "offset"))
			s += "(+"+ItemNBTHelper.getInt(stack, "offset")+")";
		return s;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote && player.isSneaking())
		{
			int offset = ItemNBTHelper.getInt(stack, "offset");
			offset++;
			if(offset>5)
				ItemNBTHelper.remove(stack, "offset");
			else
				ItemNBTHelper.setInt(stack, "offset", offset);
			return true;
		}
		return false;
	}
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(world.isRemote)
			return stack;
		if(player.isSneaking())
		{
			int offset = ItemNBTHelper.getInt(stack, "offset");
			offset++;
			if(offset>5)
				ItemNBTHelper.remove(stack, "offset");
			else
				ItemNBTHelper.setInt(stack, "offset", offset);
		}
		else
		{
			int x = (int)Math.floor(player.posX);
			int y = (int)Math.floor(player.posY)+1 + ItemNBTHelper.getInt(stack, "offset");
			int z = (int)Math.floor(player.posZ);
			Vec3 look = player.getLookVec();
			double max = Math.max(Math.max(Math.abs(look.xCoord), Math.abs(look.yCoord)), Math.abs(look.zCoord));
			if(look.yCoord==max)
				y += 1;
			else if(-look.yCoord==max)
				y -= 2;
			else if(look.xCoord==max)
				x += 1;
			else if(-look.xCoord==max)
				x -= 1;
			else if(look.zCoord==max)
				z += 1;
			else if(-look.zCoord==max)
				z -= 1;
			if (world.canPlaceEntityOnSide(this.field_150939_a, x, y, z, false, 0, player, stack))
				stack.tryPlaceItemIntoWorld(player, world, x, y, z, 0, 0.0F, 0.0F, 0.0F);
		}
		return stack;
	}
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		if(player.isSneaking())
			return false;
		y += ItemNBTHelper.getInt(stack, "offset");
		if(!world.isAirBlock(x, y, z))
			return false;
		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		if(!ret)
			return false;
		return true;
	}
}