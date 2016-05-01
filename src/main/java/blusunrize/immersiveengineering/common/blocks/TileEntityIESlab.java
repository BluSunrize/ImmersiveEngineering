package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityIESlab extends TileEntityIEBase
{
	public int slabType=0;
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		slabType = nbt.getInteger("slabType");
		if(descPacket && worldObj!=null)
			worldObj.markBlockForUpdate(this.pos);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("slabType", slabType);
	}
}