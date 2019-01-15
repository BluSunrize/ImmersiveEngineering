/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

public class GlobalWireNetwork
{
	private Map<BlockPos, LocalWireNetwork> localNets = new HashMap<>();

	@Nonnull
	public static GlobalWireNetwork getNetwork(World w) {
		if (!w.hasCapability(NetHandlerCapability.NET_CAPABILITY, null))
			throw new RuntimeException("No net handler found for dimension "+w.provider.getDimension()+", remote: "+w.isRemote);
		return Objects.requireNonNull(w.getCapability(NetHandlerCapability.NET_CAPABILITY, null));
	}

	public Connection addConnection(BlockPos posA, BlockPos posB, WireType type)
	{
		IImmersiveConnectable iicA = getLocalNet(posA).getConnector(posA);
		IImmersiveConnectable iicB = getLocalNet(posB).getConnector(posB);
		return addConnection(iicA, iicB, posA, posB, type);
	}

	public Connection addConnection(IImmersiveConnectable iicA, IImmersiveConnectable iicB, WireType type)
	{
		BlockPos posA = ApiUtils.toBlockPos(iicA);
		BlockPos posB = ApiUtils.toBlockPos(iicB);
		return addConnection(iicA, iicB, posA, posB, type);
	}
	private Connection addConnection(IImmersiveConnectable iicA, IImmersiveConnectable iicB, BlockPos posA, BlockPos posB,
									 WireType type)
	{
		LocalWireNetwork netA = getNullableLocalNet(posA);
		LocalWireNetwork netB = getNullableLocalNet(posB);
		IELogger.info("Connecting: {} to {}", netA, netB);
		Connection conn = new Connection(type, posA, posB);

		Collection<BlockPos> toSet = new ArrayList<>(2);
		LocalWireNetwork joined;
		if(netA==null&&netB==null)
		{
			IELogger.info("null-null");
			joined = new LocalWireNetwork(this);
			toSet.add(posA);
			toSet.add(posB);
			joined.addConnector(posA, iicA);
			joined.addConnector(posB, iicB);
		}
		else if(netA==null)
		{
			IELogger.info("null-non");
			toSet.add(posA);
			joined = netB;
			joined.addConnector(posA, iicA);
		}
		else if(netB==null)
		{
			IELogger.info("non-null");
			toSet.add(posB);
			joined = netA;
			joined.addConnector(posB, iicB);
		}
		else if(netA!=netB)
		{
			IELogger.info("non-non-different");
			joined = netA.merge(netB);
			toSet = joined.getConnectors();
		}
		else
		{
			IELogger.info("non-non-same");
			joined = netA;
		}
		IELogger.logger.info("Result: {}, to set: {}", joined, toSet);
		joined.addConnection(conn);
		for(BlockPos p : toSet)
			localNets.put(p, joined);
		return conn;
	}

	public void removeAllConnectionsAt(BlockPos pos, Consumer<Connection> handler)
	{
		LocalWireNetwork net = getLocalNet(pos);
		List<Connection> conns = new ArrayList<>(net.getConnections(pos));
		//TODO batch removal method
		for(Connection conn : conns)
		{
			handler.accept(conn);
			removeConnection(conn);
		}
	}

	public void removeConnection(Connection c)
	{
		LocalWireNetwork oldNet = localNets.get(c.getEndA());
		oldNet.removeConnection(c);
		//TODO move to private method!
		Collection<LocalWireNetwork> newNets = oldNet.split();
		for(LocalWireNetwork net : newNets)
			if(net!=oldNet)
				for(BlockPos p : net.getConnectors())
					localNets.put(p, net);
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		localNets.clear();
		NBTTagList locals = nbt.getTagList("locals", NBT.TAG_COMPOUND);
		for(NBTBase b : locals)
		{
			NBTTagCompound subnet = (NBTTagCompound)b;
			LocalWireNetwork localNet = new LocalWireNetwork(subnet, this);
			for(BlockPos p : localNet.getConnectors())
				localNets.put(p, localNet);
		}
	}

	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound ret = new NBTTagCompound();
		NBTTagList locals = new NBTTagList();
		Set<LocalWireNetwork> savedNets = Collections.newSetFromMap(new IdentityHashMap<>());
		for(LocalWireNetwork local : localNets.values())
			if(savedNets.add(local))
				locals.appendTag(local.writeToNBT());
		ret.setTag("locals", locals);
		return ret;
	}

	public LocalWireNetwork getLocalNet(BlockPos pos) {
		return localNets.computeIfAbsent(pos, p->new LocalWireNetwork(this));
	}

	public LocalWireNetwork getNullableLocalNet(BlockPos pos) {
		return localNets.get(pos);
	}

	public void removeConnector(BlockPos pos, IImmersiveConnectable iic)
	{
		LocalWireNetwork local = getLocalNet(pos);
		local.removeConnector(pos);
		localNets.remove(pos);
	}
}
