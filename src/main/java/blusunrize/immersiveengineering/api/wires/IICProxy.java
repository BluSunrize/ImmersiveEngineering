/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.TargetingInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IICProxy implements IImmersiveConnectable
{
	private DimensionType dim;
	private BlockPos pos;
	private List<Connection> internalConns;
	private List<ConnectionPoint> points;

	public IICProxy(DimensionType dimension, BlockPos pos, Collection<Connection> internal,
					Collection<ConnectionPoint> points)
	{
		dim = dimension;
		this.pos = pos;
		this.internalConns = new ArrayList<>(internal);
		this.points = new ArrayList<>(points);
	}

	public IICProxy(DimensionType dimension, BlockPos pos)
	{
		this(dimension, pos, ImmutableList.of(), ImmutableList.of());
	}

	public IICProxy(TileEntity te)
	{
		if(!(te instanceof IImmersiveConnectable))
			throw new IllegalArgumentException("Can't create an IICProxy for a null/non-IIC TileEntity");
		dim = te.getWorld().getDimension().getType();
		pos = te.getPos();
		internalConns = Lists.newArrayList(((IImmersiveConnectable)te).getInternalConnections());
		points = new ArrayList<>(((IImmersiveConnectable)te).getConnectionPoints());
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		return internalConns;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public DimensionType getDimension()
	{
		return dim;
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		//TODO clean up
		//this will load the chunk the TE is in for 1 tick since it needs to be notified about the removed wires
		World w = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), dim, false, true);
		if(w==null)
		{
			WireLogger.logger.warn("Tried to remove a wire in dimension "+dim+" which does not exist");
			return;
		}
		TileEntity te = w.getTileEntity(pos);
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
	public void onEnergyPassthrough(int amount)
	{
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return null;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return points;
	}

	public static IICProxy readFromNBT(CompoundNBT nbt)
	{
		ListNBT internalNBT = nbt.getList("internal", NBT.TAG_COMPOUND);
		List<Connection> internal = new ArrayList<>(internalNBT.size());
		for(INBT c : internalNBT)
			internal.add(new Connection((CompoundNBT)c));
		ListNBT pointNBT = nbt.getList("points", NBT.TAG_COMPOUND);
		List<ConnectionPoint> points = new ArrayList<>();
		for(INBT c : pointNBT)
			points.add(new ConnectionPoint((CompoundNBT)c));
		return new IICProxy(DimensionType.byName(new ResourceLocation(nbt.getString("dim"))),
				NBTUtil.readBlockPos(nbt.getCompound("pos")), internal, points);
	}

	public CompoundNBT writeToNBT()
	{
		CompoundNBT ret = new CompoundNBT();
		ret.putString("dim", dim.getRegistryName().toString());
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
}