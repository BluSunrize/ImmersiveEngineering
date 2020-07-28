/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.proxy;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IICProxy implements IImmersiveConnectable
{
	private final World world;
	private final BlockPos pos;
	private final List<Connection> internalConns;
	private final List<ConnectionPoint> points;

	public IICProxy(World world, BlockPos pos, Collection<Connection> internal,
					Collection<ConnectionPoint> points)
	{
		this.world = world;
		this.pos = pos;
		this.internalConns = new ArrayList<>(internal);
		this.points = new ArrayList<>(points);
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		return internalConns;
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		//TODO clean up
		//this will load the chunk the TE is in for 1 tick since it needs to be notified about the removed wires
		TileEntity te = world.getTileEntity(pos);
		if(!(te instanceof IImmersiveConnectable))
			return;
		((IImmersiveConnectable)te).removeCable(connection, attachedPoint);
	}

	@Override
	public boolean canConnect()
	{
		return false;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		return false;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset)
	{
		return null;
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return null;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return points;
	}

	public static IICProxy readFromNBT(World world, CompoundNBT nbt)
	{
		ListNBT internalNBT = nbt.getList("internal", NBT.TAG_COMPOUND);
		List<Connection> internal = new ArrayList<>(internalNBT.size());
		for(INBT c : internalNBT)
			internal.add(new Connection((CompoundNBT)c));
		ListNBT pointNBT = nbt.getList("points", NBT.TAG_COMPOUND);
		List<ConnectionPoint> points = new ArrayList<>();
		for(INBT c : pointNBT)
			points.add(new ConnectionPoint((CompoundNBT)c));
		return new IICProxy(world, NBTUtil.readBlockPos(nbt.getCompound("pos")), internal, points);
	}

	public CompoundNBT writeToNBT()
	{
		CompoundNBT ret = new CompoundNBT();
		ret.put("pos", NBTUtil.writeBlockPos(pos));
		ListNBT points = new ListNBT();
		for(ConnectionPoint cp : this.points)
			points.add(cp.createTag());
		ret.put("points", points);
		ListNBT internal = new ListNBT();
		for(Connection conn : this.internalConns)
			internal.add(conn.toNBT());
		ret.put("internal", internal);

		return ret;
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return pos;
	}

	@Override
	public boolean isProxy()
	{
		return true;
	}

	@Override
	public BlockPos getPosition()
	{
		return pos;
	}
}