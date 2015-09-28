package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityDynamo extends TileEntityIEBase implements IEnergyConnection
{
	public int facing = 2;

	@Override
	public boolean canUpdate()
	{
		return false;
	}
	public void inputRotation(double rotation, int side)
	{
		if(side!=ForgeDirection.OPPOSITES[facing])
			return;
		int output = (int) (Config.getDouble("dynamo_output") * rotation);
		for(int i=0; i<6; i++)
		{
			ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[i];
			TileEntity te = worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
			if(te instanceof IEnergyReceiver)
			{
				IEnergyReceiver ier = (IEnergyReceiver)te;
				output -= ier.receiveEnergy(fd.getOpposite(), output, false);
			}
		}
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = nbt.getInteger("facing");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return true;
	}
}