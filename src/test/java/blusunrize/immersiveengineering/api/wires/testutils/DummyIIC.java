/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.testutils;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.StringJoiner;

public class DummyIIC implements IImmersiveConnectable
{
	private final BlockPos pos;
	private final boolean isProxy;
	private final Collection<ConnectionPoint> points;
	private final Collection<Connection> internal;

	public DummyIIC(BlockPos pos, boolean isProxy, Collection<ConnectionPoint> points, Collection<Connection> internal)
	{
		this.pos = pos;
		this.isProxy = isProxy;
		this.points = points;
		this.internal = internal;
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target)
	{
		return pos;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return true;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{

	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeCable(@Nullable Connection connection, ConnectionPoint attachedPoint)
	{
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return Vec3.ZERO;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return points;
	}

	@Override
	public BlockPos getPosition()
	{
		return pos;
	}

	@Override
	public boolean isProxy()
	{
		return isProxy;
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		return internal;
	}

	@Override
	public String toString()
	{
		return new StringJoiner(", ", DummyIIC.class.getSimpleName()+"[", "]")
				.add("pos="+pos)
				.add("isProxy="+isProxy)
				.add("points="+points)
				.add("internal="+internal)
				.toString();
	}
}
