package blusunrize.immersiveengineering.common.blocks.metal;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityCreativeCap extends TileEntityCapacitorLV {

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		if(worldObj.isRemote || from.ordinal()>=sideConfig.length || sideConfig[from.ordinal()]!=0)
			return 0;
		return maxReceive;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		if(worldObj.isRemote || from.ordinal()>=sideConfig.length || sideConfig[from.ordinal()]!=1)
			return 0;
		return maxExtract;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return Integer.MAX_VALUE;
	}
	@Override
	protected void transferEnergy(int side) {
		if (sideConfig[side]!=1)
			return;
		ForgeDirection to = ForgeDirection.getOrientation(side);
		if (worldObj.blockExists(xCoord+to.offsetX, yCoord+to.offsetY, zCoord+to.offsetZ));
		{
			TileEntity te = worldObj.getTileEntity(xCoord+to.offsetX, yCoord+to.offsetY, zCoord+to.offsetZ);
			if (te instanceof IEnergyReceiver)
				((IEnergyReceiver)te).receiveEnergy(to.getOpposite(), Integer.MAX_VALUE, false);
		}
	}
}
