package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;

public class TileEntityRelayHV extends TileEntityConnectorHV
{
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		double conRadius = con.cableType.getRenderDiameter()/2;
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
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		return Vec3.createVectorHelper(.5+fd.offsetX*.4375, .5+fd.offsetY*.4375, .5+fd.offsetZ*.4375);
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