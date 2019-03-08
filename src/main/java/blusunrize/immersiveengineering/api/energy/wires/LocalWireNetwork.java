/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.energy.wires.localhandlers.IWorldTickable;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalWireNetwork implements IWorldTickable
{
	//TODO do we need this?
	private final GlobalWireNetwork globalNet;
	private final Map<ConnectionPoint, Collection<Connection>> connections = new HashMap<>();
	private final Map<BlockPos, IImmersiveConnectable> connectors = new HashMap<>();
	private final Map<ResourceLocation, Pair<AtomicInteger, LocalNetworkHandler>> handlers = new HashMap<>();

	public LocalWireNetwork(NBTTagCompound subnet, GlobalWireNetwork globalNet)
	{
		this.globalNet = globalNet;
		NBTTagList proxies = subnet.getTagList("proxies", NBT.TAG_COMPOUND);
		for(NBTBase b : proxies)
		{
			IICProxy proxy = IICProxy.readFromNBT(((NBTTagCompound)b).getCompoundTag("proxy"));
			for(NBTBase p : ((NBTTagCompound)b).getTagList("points", NBT.TAG_COMPOUND))
			{
				ConnectionPoint point = new ConnectionPoint((NBTTagCompound)p);
				loadConnector(point, proxy);
			}
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
		Multimap<BlockPos, ConnectionPoint> connsByBlock = HashMultimap.create();
		for(ConnectionPoint cp : connections.keySet())
			connsByBlock.put(cp.getPosition(), cp);
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
			{
				NBTTagCompound complete = new NBTTagCompound();
				complete.setTag("proxy", proxy.writeToNBT());
				NBTTagList cps = new NBTTagList();
				for(ConnectionPoint cp : connsByBlock.get(p))
					cps.appendTag(cp.createTag());
				complete.setTag("points", cps);
				proxies.appendTag(complete);
			}
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
		Collection<Connection> conns = connections.get(at);
		if(conns!=null)
			return Collections.unmodifiableCollection(conns);
		else
			return ImmutableSet.of();
	}

	//LOADING/UNLOADING
	void loadConnector(ConnectionPoint p, IImmersiveConnectable iic)
	{
		connectors.put(p.getPosition(), iic);
		for(ResourceLocation loc : iic.getRequestedHandlers())
		{
			if(handlers.containsKey(loc))
				handlers.get(loc).getLeft().incrementAndGet();
			else
				handlers.put(loc, new ImmutablePair<>(new AtomicInteger(1),
						LocalNetworkHandler.createHandler(loc, this)));
			IELogger.logger.info("Increasing {} to {}", loc, handlers.get(loc).getLeft());
		}
		if(!connections.containsKey(p))
			connections.put(p, new HashSet<>());
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
		result.handlers.putAll(other.handlers);
		for(Entry<ResourceLocation, Pair<AtomicInteger, LocalNetworkHandler>> loc : handlers.entrySet())
		{
			//TODO call merge or soemthing when only one of the original nets had the handler!
			result.handlers.merge(loc.getKey(), loc.getValue(), (p1, p2) -> {
				LocalNetworkHandler mergedHandler = p1.getValue().merge(p2.getValue());
				return new MutablePair<>(
						new AtomicInteger(p1.getKey().intValue()+p2.getKey().get()),
						mergedHandler);
			});
			IELogger.logger.info("Merged {} to {}", loc.getKey(), result.handlers.get(loc.getKey()).getLeft());
		}
		for(Entry<ResourceLocation, Pair<AtomicInteger, LocalNetworkHandler>> loc : result.handlers.entrySet())
			loc.getValue().getRight().setLocalNet(result);
		return result;
	}

	void removeConnection(Connection c)
	{
		boolean successA = false, successB = false;
		Collection<Connection> connsA = connections.get(c.getEndA());
		if(connsA!=null)
			successA = connsA.remove(c);
		Collection<Connection> connsB = connections.get(c.getEndB());
		if(connsB!=null)
			successB = connsB.remove(c);
		if(!successA)
			IELogger.logger.info("Failed to remove {} from {} (A)", c, c.getEndA());
		if(!successB)
			IELogger.logger.info("Failed to remove {} from {} (B)", c, c.getEndB());
		for(Pair<AtomicInteger, LocalNetworkHandler> h : handlers.values())
			h.getValue().onConnectionRemoved(c);
	}

	void removeConnector(BlockPos p)
	{
		IImmersiveConnectable iic = connectors.get(p);
		if(iic==null)
		{
			for(ConnectionPoint point : getConnectionPoints())
				if(point.getPosition().equals(p))
					IELogger.logger.info("Cancelling, but connections {} at {} still exist!", connections.get(point),
							point);
			IELogger.logger.info("Cancelled");
			return;
		}
		for(ConnectionPoint point : iic.getConnectionPoints())
		{
			for(Connection c : getConnections(point))
			{
				ConnectionPoint other = c.getOtherEnd(point);
				Collection<Connection> connsOther = connections.get(other);
				if(connsOther!=null)
					connsOther.remove(c);
			}
			connections.remove(point);
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
		connections.get(conn.getEndA()).add(conn);
		connections.get(conn.getEndB()).add(conn);
		for(Pair<AtomicInteger, LocalNetworkHandler> h : handlers.values())
			h.getValue().onConnectionAdded(conn);
	}

	private void removeConnectorHandlers(IImmersiveConnectable iic)
	{
		for(ResourceLocation loc : iic.getRequestedHandlers())
		{
			if(!handlers.containsKey(loc)) throw new AssertionError();
			int remaining = handlers.get(loc).getLeft().decrementAndGet();
			IELogger.logger.info("Decreasing {} to {}", loc, remaining);
			if(remaining <= 0)
			{
				IELogger.logger.info("Removing: {}", loc);
				handlers.remove(loc);
			}
		}
	}

	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return connections.keySet();
	}

	public Collection<LocalWireNetwork> split()
	{
		//TODO handlers?
		Set<ConnectionPoint> toVisit = new HashSet<>(getConnectionPoints());
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
				newNet.loadConnector(p, connectors.get(p.getPosition()));
			for(ConnectionPoint p : inComponent)
			{
				for(Connection c : getConnections(p))
					if(c.isPositiveEnd(p))
						newNet.addConnection(c);
			}
			ret.add(newNet);
		}
		IELogger.info("Split net! Now {} nets: {}", ret.size(), ret);
		return ret;
	}

	@Override
	public String toString()
	{
		return "Connectors: "+connectors+", connections: "+connections;
	}

	public IImmersiveConnectable getConnector(ConnectionPoint cp)
	{
		return getConnector(cp.getPosition());
	}

	public GlobalWireNetwork getGlobal()
	{
		return globalNet;
	}

	@Override
	public void update(World w)
	{
		for(Pair<AtomicInteger, LocalNetworkHandler> handler : handlers.values())
			if(handler.getRight() instanceof IWorldTickable)
				((IWorldTickable)handler.getRight()).update(w);
	}

	public <T extends LocalNetworkHandler> T getHandler(ResourceLocation name, Class<T> type)
	{
		Pair<AtomicInteger, LocalNetworkHandler> p = handlers.get(name);
		if(p==null)
			return null;
		else
		{
			LocalNetworkHandler ret = p.getRight();
			if(type.isInstance(ret))
				return (T)ret;
			else
				return null;
		}
	}
}
