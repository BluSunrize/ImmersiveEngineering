/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

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

//	@Override
//	public boolean canConnectCable(WireType cableType, TargetingInfo target)
//	{
//		return limitType==null&&super.canConnectCable(cableType, target);
//	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return new Vec3d(.5+side.getFrontOffsetX()*(.25-conRadius), .5+side.getFrontOffsetY()*(.25-conRadius), .5+side.getFrontOffsetZ()*(.25-conRadius));
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