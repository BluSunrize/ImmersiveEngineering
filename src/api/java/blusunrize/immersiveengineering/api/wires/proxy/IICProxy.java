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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IICProxy implements IImmersiveConnectable
{
	private final Level world;
	private final BlockPos pos;
	private final List<Connection> internalConns;
	private final List<ConnectionPoint> points;

	public IICProxy(Level world, BlockPos pos, Collection<Connection> internal,
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
		BlockEntity te = world.getBlockEntity(pos);
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
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return false;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		return null;
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return null;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return points;
	}

	public static IICProxy readFromNBT(Level world, CompoundTag nbt)
	{
		ListTag internalNBT = nbt.getList("internal", NBT.TAG_COMPOUND);
		List<Connection> internal = new ArrayList<>(internalNBT.size());
		for(Tag c : internalNBT)
			internal.add(new Connection((CompoundTag)c));
		ListTag pointNBT = nbt.getList("points", NBT.TAG_COMPOUND);
		List<ConnectionPoint> points = new ArrayList<>();
		for(Tag c : pointNBT)
			points.add(new ConnectionPoint((CompoundTag)c));
		return new IICProxy(world, NbtUtils.readBlockPos(nbt.getCompound("pos")), internal, points);
	}

	public CompoundTag writeToNBT()
	{
		CompoundTag ret = new CompoundTag();
		ret.put("pos", NbtUtils.writeBlockPos(pos));
		ListTag points = new ListTag();
		for(ConnectionPoint cp : this.points)
			points.add(cp.createTag());
		ret.put("points", points);
		ListTag internal = new ListTag();
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