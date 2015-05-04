package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

public class TileEntityConveyorBelt extends TileEntityIEBase
{
	public boolean transportUp=false;
	public boolean transportDown=false;
	public int facing=2;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		transportUp = nbt.getBoolean("transportUp");
		transportDown = nbt.getBoolean("transportDown");
		facing = nbt.getInteger("facing");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setBoolean("transportUp", transportUp);
		nbt.setBoolean("transportDown", transportDown);
		nbt.setInteger("facing", facing);
	}

}
