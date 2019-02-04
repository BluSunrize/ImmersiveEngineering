/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
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
	private Map<ConnectionPoint, LocalWireNetwork> localNets = new HashMap<>();

	@Nonnull
	public static GlobalWireNetwork getNetwork(World w)
	{
		if(!w.hasCapability(NetHandlerCapability.NET_CAPABILITY, null))
			throw new RuntimeException("No net handler found for dimension "+w.provider.getDimension()+", remote: "+w.isRemote);
		return Objects.requireNonNull(w.getCapability(NetHandlerCapability.NET_CAPABILITY, null));
	}

	public void addConnection(Connection conn)
	{
		ConnectionPoint posA = conn.getEndA();
		ConnectionPoint posB = conn.getEndB();
		IImmersiveConnectable iicA = getLocalNet(posA).getConnector(posA.getPosition());
		IImmersiveConnectable iicB = getLocalNet(posB).getConnector(posB.getPosition());
		LocalWireNetwork netA = getNullableLocalNet(posA);
		LocalWireNetwork netB = getNullableLocalNet(posB);
		Collection<ConnectionPoint> toSet = new ArrayList<>(2);
		LocalWireNetwork joined;
		if(netA==null&&netB==null)
		{
			IELogger.info("null-null");
			joined = new LocalWireNetwork(this);
			toSet.add(posA);
			toSet.add(posB);
			joined.loadConnector(posA.getPosition(), iicA);
			joined.loadConnector(posB.getPosition(), iicB);
		}
		else if(netA==null)
		{
			IELogger.info("null-non");
			toSet.add(posA);
			joined = netB;
			joined.loadConnector(posA.getPosition(), iicA);
		}
		else if(netB==null)
		{
			IELogger.info("non-null");
			toSet.add(posB);
			joined = netA;
			joined.loadConnector(posB.getPosition(), iicB);
		}
		else if(netA!=netB)
		{
			IELogger.info("non-non-different");
			joined = netA.merge(netB);
			toSet = joined.getConnectionPoints();
		}
		else
		{
			IELogger.info("non-non-same");
			joined = netA;
		}
		IELogger.logger.info("Result: {}, to set: {}", joined, toSet);
		joined.addConnection(conn);
		for(ConnectionPoint p : toSet)
			localNets.put(p, joined);
	}

	public void removeAllConnectionsAt(IImmersiveConnectable iic, Consumer<Connection> handler)
	{
		for(ConnectionPoint cp : iic.getConnectionPoints())
			removeAllConnectionsAt(cp, handler);
	}

	public void removeAllConnectionsAt(ConnectionPoint pos, Consumer<Connection> handler)
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
		splitNet(oldNet);
	}

	private void splitNet(LocalWireNetwork oldNet)
	{
		Collection<LocalWireNetwork> newNets = oldNet.split();
		for(LocalWireNetwork net : newNets)
			if(net!=oldNet)
				for(ConnectionPoint p : net.getConnectionPoints())
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
			IELogger.logger.info("Loading net {}", localNet);
			for(ConnectionPoint p : localNet.getConnectionPoints())
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

	public LocalWireNetwork getLocalNet(ConnectionPoint pos)
	{
		return localNets.computeIfAbsent(pos, p -> new LocalWireNetwork(this));
	}

	public LocalWireNetwork getNullableLocalNet(ConnectionPoint pos)
	{
		return localNets.get(pos);
	}

	public void removeConnector(IImmersiveConnectable iic)
	{
		for(ConnectionPoint c : iic.getConnectionPoints())
		{
			LocalWireNetwork local = getNullableLocalNet(c);
			if(local!=null)
			{
				local.removeConnector(c.getPosition());
				localNets.remove(c);
				splitNet(local);
			}
		}
	}

	public void onConnectorLoad(BlockPos pos, IImmersiveConnectable iic)
	{
		//TODO this doesn't belong here... There will be parallel conns every time a chunk cycles...
		for(Connection c : iic.getInternalConnections())
		{
			Preconditions.checkArgument(c.isInternal(), "Internal connection for "+iic+"was not marked as internal!");
			addConnection(c);
		}
		Set<LocalWireNetwork> added = new HashSet<>();
		for(ConnectionPoint connectionPoint : iic.getConnectionPoints())
		{
			LocalWireNetwork local = getLocalNet(connectionPoint);
			if(added.add(local))
				local.loadConnector(pos, iic);
		}
	}

	public void onConnectorUnload(BlockPos pos, IImmersiveConnectable iic)
	{
		Set<LocalWireNetwork> added = new HashSet<>();
		for(ConnectionPoint connectionPoint : iic.getConnectionPoints())
		{
			LocalWireNetwork local = getLocalNet(connectionPoint);
			if(added.add(local))
				local.unloadConnector(pos, iic);
		}
	}
}
