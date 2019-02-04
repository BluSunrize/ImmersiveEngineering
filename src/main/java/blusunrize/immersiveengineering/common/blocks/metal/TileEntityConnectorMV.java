/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

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
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vec3d(.5+side.getXOffset()*(.0625-conRadius), .5+side.getYOffset()*(.0625-conRadius), .5+side.getZOffset()*(.0625-conRadius));
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