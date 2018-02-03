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

public class TileEntityConnectorMV extends TileEntityConnectorLV
{
	@Override
	protected boolean canTakeMV()
	{
		return true;
	}
	@Override
	protected boolean canTakeLV()
	{
		return false;
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return new Vec3d(.5+side.getFrontOffsetX()*(.0625-conRadius), .5+side.getFrontOffsetY()*(.0625-conRadius), .5+side.getFrontOffsetZ()*(.0625-conRadius));
	}

	@Override
	int getRenderRadiusIncrease()
	{
		return WireType.ELECTRUM.getMaxLength();
	}
	
	@Override
	public int getMaxInput()
	{
		return connectorInputValues[1];
	}
	@Override
	public int getMaxOutput()
	{
		return connectorInputValues[1];
	}
}