package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityIESlab extends TileEntityIEBase
{
	public int slabType=0;
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		slabType = nbt.getInteger("slabType");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("slabType", slabType);
	}
}