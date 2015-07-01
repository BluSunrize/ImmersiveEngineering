package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

public class TileEntityWallmount extends TileEntityIEBase
{
	public int facing = 3;
	public boolean inverted = false;
	public int sideAttached = 0;

	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = nbt.getInteger("facing");
		inverted = nbt.getBoolean("inverted");
		sideAttached = nbt.getInteger("sideAttached");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing);
		nbt.setBoolean("inverted", inverted);
		nbt.setInteger("sideAttached", sideAttached);
	}
}
