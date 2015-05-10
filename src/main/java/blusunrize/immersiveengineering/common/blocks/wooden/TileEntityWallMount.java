package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

public class TileEntityWallMount extends TileEntityIEBase
{
	public int facing = 2;
	public boolean inverted = false;

	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		facing = nbt.getInteger("facing");
		inverted = nbt.getBoolean("inverted");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("facing", facing);
		nbt.setBoolean("inverted", inverted);
	}
}