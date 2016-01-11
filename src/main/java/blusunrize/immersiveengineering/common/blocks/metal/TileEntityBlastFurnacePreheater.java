package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityBlastFurnacePreheater extends TileEntityIEBase implements IEnergyReceiver
{
	public int dummy = 0;
	public EnergyStorage energyStorage = new EnergyStorage(8000);
	public int facing = 2;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public int doSpeedup()
	{
		int consumed = Config.getInt("preheater_consumption"); 
		if(this.energyStorage.extractEnergy(consumed, true)==consumed)
		{
			this.energyStorage.extractEnergy(consumed, false);
			return 1;
		}
		return 0;
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getInteger("dummy");
		facing = nbt.getInteger("facing");
		energyStorage.readFromNBT(nbt);
		if(descPacket)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("dummy", dummy);
		nbt.setInteger("facing", facing);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return from==ForgeDirection.UP&&dummy==2;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(dummy>0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-dummy, zCoord);
			if(te instanceof TileEntityBlastFurnacePreheater)	
				return ((TileEntityBlastFurnacePreheater)te).receiveEnergy(from, maxReceive, simulate);
			return 0;
		}
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(dummy>0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-dummy, zCoord);
			if(te instanceof TileEntityBlastFurnacePreheater)	
				return ((TileEntityBlastFurnacePreheater)te).getEnergyStored(from);
			return 0;
		}
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(dummy>0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-dummy, zCoord);
			if(te instanceof TileEntityBlastFurnacePreheater)	
				return ((TileEntityBlastFurnacePreheater)te).getMaxEnergyStored(from);
			return 0;
		}
		return energyStorage.getMaxEnergyStored();
	}
}