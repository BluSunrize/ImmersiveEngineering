package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
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
		if(pos!=0 || worldObj.isRemote || worldObj.isAirBlock(xCoord,yCoord-1,zCoord))
			return;
		if(process<Config.getInt("coredrill_time"))
			if(energyStorage.extractEnergy(Config.getInt("coredrill_consumption"), false)==Config.getInt("coredrill_consumption"))
			{
				process++;
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
	}

	public float getSampleProgress()
	{
		return process/(float)Config.getInt("coredrill_time");
	}
	public boolean isSamplingFinished()
	{
		return process>=Config.getInt("coredrill_time");
	}
	public String getVein()
	{
		ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(worldObj, (xCoord>>4), (zCoord>>4));
		return mineral==null?null: mineral.name;
	}
	public float getVeinIntegrity()
	{
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(worldObj, (xCoord>>4), (zCoord>>4));
		boolean deplOverride = info.depletion<0;
		if(ExcavatorHandler.mineralVeinCapacity<0||deplOverride)
			return 1;
		else if(info.mineralOverride==null && info.mineral==null)
			return 0;
		else
			return (Config.getInt("excavator_depletion")-info.depletion)/(float)Config.getInt("excavator_depletion");
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