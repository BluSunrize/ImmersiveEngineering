package blusunrize.immersiveengineering.common.blocks.wooden;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityWoodenPost extends TileEntityIEBase
{
	public byte type;
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public static boolean _Immovable()
	{
		return true;
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		type = nbt.getByte("type");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setByte("type", type);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(type==0)
			return AxisAlignedBB.getBoundingBox(xCoord-1,yCoord,zCoord-1, xCoord+2,yCoord+4,zCoord+2);
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
}
