package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;


public class TileEntityWindmillAdvanced extends TileEntityWindmill
{
	@Override
	protected float getSpeedModifier()
	{
		return 1f;
	}
	

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
	}
}