package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

public class TileEntityLantern extends TileEntityIEBase
{
	public int facing=1;

	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = nbt.getInteger("facing");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing",facing);
	}
}