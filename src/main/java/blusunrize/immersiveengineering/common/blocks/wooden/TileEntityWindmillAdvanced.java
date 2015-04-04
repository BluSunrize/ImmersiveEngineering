package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;


public class TileEntityWindmillAdvanced extends TileEntityWindmill
{
	public int dye = 15;
	@Override
	protected float getSpeedModifier()
	{
		return .66f;
	}
	

	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		super.readCustomNBT(nbt);
		dye = nbt.getInteger("dye");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		super.writeCustomNBT(nbt);
		nbt.setInteger("dye", dye);
	}
}