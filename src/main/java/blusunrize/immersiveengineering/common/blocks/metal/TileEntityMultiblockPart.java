package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

public abstract class TileEntityMultiblockPart extends TileEntityIEBase
{

	public boolean formed = false;
	public int pos=-1;
	public int[] offset = {0,0,0};
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		formed = nbt.getBoolean("formed");
		pos = nbt.getInteger("pos");
		offset = nbt.getIntArray("offset");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setBoolean("formed", formed);
		nbt.setInteger("pos", pos);
		nbt.setIntArray("offset", offset);
	}

	public abstract ItemStack getOriginalBlock();
}