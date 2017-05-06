package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityCapacitorCreative extends TileEntityCapacitorLV
{
	public TileEntityCapacitorCreative()
	{
		super();
		for(int i=0; i<sideConfig.length; i++)
			sideConfig[i] = SideConfig.OUTPUT;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		if(world.isRemote || from.ordinal()>=sideConfig.length || sideConfig[from.ordinal()]!=SideConfig.INPUT)
			return 0;
		return maxReceive;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		if(world.isRemote || from.ordinal()>=sideConfig.length || sideConfig[from.ordinal()]!=SideConfig.OUTPUT)
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
		if(sideConfig[side]!=SideConfig.OUTPUT)
			return;
		EnumFacing to = EnumFacing.getFront(side);
		if (world.isBlockLoaded(pos.offset(to)));
		{
			TileEntity te = world.getTileEntity(pos.offset(to));
			EnergyHelper.insertFlux(te, to.getOpposite(), Integer.MAX_VALUE, false);
		}
	}
}
