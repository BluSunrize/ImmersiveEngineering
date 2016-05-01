package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityCapacitorCreative extends TileEntityCapacitorLV
{
	public TileEntityCapacitorCreative()
	{
		super();
		for(int i=0; i<sideConfig.length; i++)
			sideConfig[i] = 1;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		if(worldObj.isRemote || from.ordinal()>=sideConfig.length || sideConfig[from.ordinal()]!=0)
			return 0;
		return maxReceive;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		if(worldObj.isRemote || from.ordinal()>=sideConfig.length || sideConfig[from.ordinal()]!=1)
			return 0;
		return maxExtract;
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return Integer.MAX_VALUE;
	}
	@Override
	protected void transferEnergy(int side)
	{
		if (sideConfig[side]!=1)
			return;
		EnumFacing to = EnumFacing.getFront(side);
		if (worldObj.isBlockLoaded(pos.offset(to)));
		{
			TileEntity te = worldObj.getTileEntity(pos.offset(to));
			if (te instanceof IFluxReceiver)
				((IFluxReceiver)te).receiveEnergy(to.getOpposite(), Integer.MAX_VALUE, false);
		}
	}
}
