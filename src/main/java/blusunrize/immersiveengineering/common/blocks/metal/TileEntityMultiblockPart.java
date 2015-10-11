package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

public abstract class TileEntityMultiblockPart extends TileEntityIEBase
{
	public boolean formed = false;
	public int pos=-1;
	public int[] offset = {0,0,0};
	public boolean mirrored = false;
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		formed = nbt.getBoolean("formed");
		pos = nbt.getInteger("pos");
		offset = nbt.getIntArray("offset");
		mirrored = nbt.getBoolean("mirrored");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("formed", formed);
		nbt.setInteger("pos", pos);
		nbt.setIntArray("offset", offset);
		nbt.setBoolean("mirrored", mirrored);
	}

	public abstract float[] getBlockBounds();
	
	public static boolean _Immovable()
	{
		return true;
	}
	public abstract TileEntityMultiblockPart master();
	public abstract ItemStack getOriginalBlock();
}