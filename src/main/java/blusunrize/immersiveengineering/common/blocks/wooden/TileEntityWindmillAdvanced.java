package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;


public class TileEntityWindmillAdvanced extends TileEntityWindmill
{
	public byte[] dye = new byte[]{15,15,15,15,15,15,15,15};
	@Override
	protected float getSpeedModifier()
	{
		return .88f;
	}
	

	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		super.readCustomNBT(nbt);
		dye = nbt.getByteArray("dye");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		super.writeCustomNBT(nbt);
		nbt.setByteArray("dye", dye);
	}
}