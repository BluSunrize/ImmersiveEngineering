/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.energy.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalWireNetwork
{
	//TODO do we need this?
	private final GlobalWireNetwork globalNet;
	private final Multimap<ConnectionPoint, Connection> connections = HashMultimap.create();
	private final Map<BlockPos, IImmersiveConnectable> connectors = new HashMap<>();
	private final Map<ResourceLocation, Pair<AtomicInteger, LocalNetworkHandler>> handlers = new HashMap<>();

	public LocalWireNetwork(NBTTagCompound subnet, GlobalWireNetwork globalNet)
	{
		this.globalNet = globalNet;
		NBTTagList proxies = subnet.getTagList("proxies", NBT.TAG_COMPOUND);
		for(NBTBase b : proxies)
		{
			IICProxy p = IICProxy.readFromNBT((NBTTagCompound)b);
			connectors.put(p.getPos(), p);
		}
		NBTTagList wires = subnet.getTagList("wires", NBT.TAG_COMPOUND);
		for(NBTBase b : wires)
		{
			Connection wire = new Connection((NBTTagCompound)b);
			if(connectors.containsKey(wire.getEndA().getPosition())&&connectors.containsKey(wire.getEndB().getPosition()))
				addConnection(wire);
			else
				IELogger.logger.error("Wire from {} to {}, but connector points are {}", wire.getEndA(), wire.getEndB(), connectors);
		}
	}

	public LocalWireNetwork(GlobalWireNetwork globalNet)
	{
		this.globalNet = globalNet;
	}

	public NBTTagCompound writeToNBT()
	{
		NBTTagList wires = new NBTTagList();
		for(ConnectionPoint p : connections.keySet())
			for(Connection conn : connections.get(p))
				if(conn.isPositiveEnd(p))
					wires.appendTag(conn.toNBT());
		NBTTagCompound ret = new NBTTagCompound();
		ret.setTag("wires", wires);
		NBTTagList proxies = new NBTTagList();
		for(BlockPos p : connectors.keySet())
		{
			IImmersiveConnectable iic = connectors.get(p);
			IICProxy proxy = null;
			if(iic instanceof IICProxy)
				proxy = (IICProxy)iic;
			else if(iic instanceof TileEntity)
				proxy = new IICProxy((TileEntity)iic);
			if(proxy!=null)
				proxies.appendTag(proxy.writeToNBT());
		}
		ret.setTag("proxies", proxies);
		return ret;
	}

	//GETTERS
	/*
	 * Returns all nodes in the local network. Do not modify the result!
	 */
	public Collection<BlockPos> getConnectors()
	{
		return Collections.unmodifiableCollection(connectors.keySet());
	}

	public IImmersiveConnectable getConnector(BlockPos pos)
	{
		assert connectors.containsKey(pos);
		return connectors.get(pos);
	}

	/*
	 * Returns all connections at the given connector. Do not modify the result!
	 */
	public Collection<Connection> getConnections(ConnectionPoint at)
	{
		return Collections.unmodifiableCollection(connections.get(at));
	}

	//LOADING/UNLOADING
	void loadConnector(BlockPos p, IImmersiveConnectable iic)
	{
		connectors.put(p, iic);
		for(ResourceLocation loc : iic.getRequestedHandlers())
		{
			if(handlers.containsKey(loc))
				handlers.get(loc).getLeft().incrementAndGet();
			else
				handlers.put(loc, new ImmutablePair<>(new AtomicInteger(1),
						LocalNetworkHandler.createHandler(loc, this)));
		}
		for(Pair<AtomicInteger, LocalNetworkHandler> h : handlers.values())
			h.getRight().onConnectorLoaded(p, iic);
	}

	void unloadConnector(BlockPos p, IImmersiveConnectable iic)
	{
		for(Pair<AtomicInteger, LocalNetworkHandler> h : handlers.values())
			h.getRight().onConnectorUnloaded(p, iic);
		removeConnectorHandlers(iic);
		connectors.put(p, new IICProxy((TileEntity)iic));
	}

	//INTERNAL USE ONLY!
	LocalWireNetwork merge(LocalWireNetwork other)
	{
		LocalWireNetwork result = new LocalWireNetwork(globalNet);
		result.connectors.putAll(connectors);
		result.connectors.putAll(other.connectors);
		result.connections.putAll(connections);
		result.connections.putAll(other.connections);
		for(Entry<ResourceLocation, Pair<AtomicInteger, LocalNetworkHandler>> loc : handlers.entrySet())
			result.handlers.merge(loc.getKey(), loc.getValue(), (p1, p2) -> new MutablePair<>(
					new AtomicInteger(p1.getKey().intValue()+p2.getKey().get()), p1.getValue().merge(p2.getValue())
			));
		return result;
	}

	void removeConnection(Connection c)
	{
		connections.remove(c.getEndA(), c);
		connections.remove(c.getEndB(), c);
		for(Pair<AtomicInteger, LocalNetworkHandler> h : handlers.values())
			h.getValue().onConnectionRemoved(c);
	}

	void removeConnector(BlockPos p)
	{
		IImmersiveConnectable iic = connectors.get(p);
		for(ConnectionPoint point : iic.getConnectionPoints())
		{
			for(Connection c : getConnections(point))
			{
				ConnectionPoint other = c.getOtherEnd(point);
				connections.remove(other, c);
			}
			connections.removeAll(point);
		}
		connectors.remove(p);
		for(Pair<AtomicInteger, LocalNetworkHandler> h : handlers.values())
			h.getValue().onConnectorRemoved(p, iic);
		removeConnectorHandlers(iic);
	}

	void addConnection(Connection conn)
	{
		if(!connectors.containsKey(conn.getEndA().getPosition()))
			throw new AssertionError(conn.getEndA().getPosition());
		if(!connectors.containsKey(conn.getEndB().getPosition()))
			throw new AssertionError(conn.getEndB().getPosition());
		connections.put(conn.getEndA(), conn);
		connections.put(conn.getEndB(), conn);
		for(Pair<AtomicInteger, LocalNetworkHandler> h : handlers.values())
			h.getValue().onConnectionAdded(conn);
	}

	private void removeConnectorHandlers(IImmersiveConnectable iic)
	{
		for(ResourceLocation loc : iic.getRequestedHandlers())
		{
			assert handlers.containsKey(loc);
			int remaining = handlers.get(loc).getLeft().decrementAndGet();
			if(remaining <= 0)
				handlers.remove(loc);
		}
	}

	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return connections.keySet();
	}

	public Collection<LocalWireNetwork> split()
	{
		Collection<ConnectionPoint> toVisit = new HashSet<>(getConnectionPoints());
		Collection<LocalWireNetwork> ret = new ArrayList<>();
		while(!toVisit.isEmpty())
		{
			Deque<ConnectionPoint> open = new ArrayDeque<>();
			List<ConnectionPoint> inComponent = new ArrayList<>();
			{
				Iterator<ConnectionPoint> tmpIt = toVisit.iterator();
				open.add(tmpIt.next());
				tmpIt.remove();
			}
			while(!open.isEmpty())
			{
				ConnectionPoint curr = open.pop();
				inComponent.add(curr);
				for(Connection c : getConnections(curr))
				{
					ConnectionPoint otherEnd = c.getOtherEnd(curr);
					if(toVisit.contains(otherEnd))
					{
						toVisit.remove(otherEnd);
						open.push(otherEnd);
					}
				}
			}
			if(ret.isEmpty()&&toVisit.isEmpty())
			{
				//Still connected
				ret.add(this);
				break;
			}
			LocalWireNetwork newNet = new LocalWireNetwork(globalNet);
			for(ConnectionPoint p : inComponent)
				newNet.loadConnector(p.getPosition(), connectors.get(p.getPosition()));
			for(ConnectionPoint p : inComponent)
			{
				for(Connection c : getConnections(p))
					if(c.isPositiveEnd(p))
						newNet.addConnection(c);
			}
			ret.add(newNet);
		}
		return ret;
	}

	@Override
	public String toString()
	{
		return "Connectors: "+connectors+", connections: "+connections;
	}
}
