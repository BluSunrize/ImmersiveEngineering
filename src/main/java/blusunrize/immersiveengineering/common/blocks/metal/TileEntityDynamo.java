package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityDynamo extends TileEntityIEBase
{
	public int facing = 2;

	@Override
	public boolean canUpdate()
	{
		return false;
	}
	public void inputRotation(double rotation)
	{
		int output = (int) (Config.getInt("dynamo_output") * rotation);
		for(int i=0; i<6; i++)
		{
			ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[i];
			if(worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ) instanceof IEnergyReceiver)
			{
				IEnergyReceiver ier = (IEnergyReceiver)worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
				output -= ier.receiveEnergy(fd.getOpposite(), output, false);
			}
		}
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		facing = nbt.getInteger("facing");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("facing", facing);
	}

}
