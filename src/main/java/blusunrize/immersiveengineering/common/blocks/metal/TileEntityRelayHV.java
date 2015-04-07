package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;

public class TileEntityRelayHV extends TileEntityConnectorHV
{
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		double conRadius = con.cableType==WireType.STEEL?.03125:.015625;
		return Vec3.createVectorHelper(.5, .125+conRadius, .5);
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(cableType==WireType.STEEL&&!canTakeHV())
			return false;
		return limitType==null||limitType==cableType;
	}
	@Override
	public boolean isEnergyOutput()
	{
		return false;
	}
	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return false;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,boolean simulate)
	{
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return 0;
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return 0;
	}
}