/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.proxy;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Collection;

public class DefaultProxyProvider implements IICProxyProvider
{
	private final DimensionType dimension;

	public DefaultProxyProvider(DimensionType dimension)
	{
		this.dimension = dimension;
	}

	public DefaultProxyProvider(World w)
	{
		this(w.dimension.getType());
	}

	@Override
	public CompoundNBT toNBT(IImmersiveConnectable proxy)
	{
		Preconditions.checkArgument(proxy instanceof IICProxy, "Expected IICProxy, got "+proxy);
		return ((IICProxy)proxy).writeToNBT();
	}

	@Override
	public IImmersiveConnectable fromNBT(CompoundNBT nbt)
	{
		return IICProxy.readFromNBT(nbt);
	}

	@Override
	public IImmersiveConnectable create(BlockPos pos, Collection<Connection> internal, Collection<ConnectionPoint> points)
	{
		return new IICProxy(dimension, pos, internal, points);
	}
}
