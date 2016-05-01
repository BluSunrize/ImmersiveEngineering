package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

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
		return limitType==null&&super.canConnectCable(cableType, target);
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		EnumFacing side = facing.getOpposite();
		return new Vec3(.5+side.getFrontOffsetX()*.3125, .5+side.getFrontOffsetY()*.3125, .5+side.getFrontOffsetZ()*.3125);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return new Vec3(.5+side.getFrontOffsetX()*(.25-conRadius), .5+side.getFrontOffsetY()*(.25-conRadius), .5+side.getFrontOffsetZ()*(.25-conRadius));
	}

	@Override
	int getRenderRadiusIncrease()
	{
		return WireType.STEEL.getMaxLength();
	}

	@Override
	public int getMaxInput()
	{
		return connectorInputValues[2];
	}
	@Override
	public int getMaxOutput()
	{
		return connectorInputValues[2];
	}
}