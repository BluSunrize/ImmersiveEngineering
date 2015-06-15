package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntitySampleDrill extends TileEntityIEBase implements IEnergyReceiver
{
	public EnergyStorage energyStorage = new EnergyStorage(8000);
	public int pos=0;
	public int process=0;

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public void updateEntity()
	{
		if(pos!=0 || worldObj.isRemote)
			return;
		if(process<Config.getInt("coredrill_time"))
			if(energyStorage.extractEnergy(Config.getInt("coredrill_consumption"), false)==Config.getInt("coredrill_consumption"))
			{
				process++;
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("pos", pos);
		nbt.setInteger("process", process);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		pos = nbt.getInteger("pos");
		process = nbt.getInteger("process");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==0)
			return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord+1,yCoord+3,zCoord+1);
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return pos==0;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(pos!=0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-pos, zCoord);
			if(te instanceof TileEntitySampleDrill)
				return ((TileEntitySampleDrill)te).receiveEnergy(from, maxReceive, simulate);
		}
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(pos!=0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-pos, zCoord);
			if(te instanceof TileEntitySampleDrill)
				return ((TileEntitySampleDrill)te).getEnergyStored(from);
		}
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(pos!=0)
		{
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-pos, zCoord);
			if(te instanceof TileEntitySampleDrill)
				return ((TileEntitySampleDrill)te).getMaxEnergyStored(from);
		}
		return energyStorage.getMaxEnergyStored();
	}
}