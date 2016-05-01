package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;


public class TileEntityWindmillAdvanced extends TileEntityWindmill
{
	public byte[] dye = new byte[]{15,15,15,15,15,15,15,15};
	@Override
	protected float getSpeedModifier()
	{
		return 1f;
	}
	

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		dye = nbt.getByteArray("dye");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setByteArray("dye", dye);
	}
}