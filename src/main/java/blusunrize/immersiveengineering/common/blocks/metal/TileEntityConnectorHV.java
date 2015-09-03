package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;

public class TileEntityConnectorHV extends TileEntityConnectorMV
{
	@Override
	protected boolean canTakeHV()
	{
		return true;
	}
	@Override
	protected boolean canTakeMV()
	{
		return false;
	}
	
	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		return super.canConnectCable(cableType, target) && limitType==null;
	}
	
	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		return Vec3.createVectorHelper(.5+fd.offsetX*.3125, .5+fd.offsetY*.3125, .5+fd.offsetZ*.3125);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return Vec3.createVectorHelper(.5+fd.offsetX*(.25-conRadius), .5+fd.offsetY*(.25-conRadius), .5+fd.offsetZ*(.25-conRadius));
	}
	
	@Override
	int getRenderRadiusIncrease()
	{
		return WireType.STEEL.getMaxLength();
	}
	
	@Override
	public int getMaxInput()
	{
		return WireType.STEEL.getTransferRate();
	}
	@Override
	public int getMaxOutput()
	{
		return WireType.STEEL.getTransferRate();
	}
}