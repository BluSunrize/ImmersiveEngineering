/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerProvider;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

public class LocalWireNetwork implements IWorldTickable
{
	//TODO do we need this?
	private final GlobalWireNetwork globalNet;
	private final Map<ConnectionPoint, Collection<Connection>> connections = new HashMap<>();
	private final Map<BlockPos, IImmersiveConnectable> connectors = new HashMap<>();
	private final Map<ResourceLocation, LocalNetworkHandler> handlers = new HashMap<>();
	/*private */final Object2IntMap<ResourceLocation> handlerUserCount = new Object2IntOpenHashMap<>();

	public LocalWireNetwork(CompoundNBT subnet, GlobalWireNetwork globalNet)
	{
		this(globalNet);
		ListNBT proxies = subnet.getList("proxies", NBT.TAG_COMPOUND);
		for(INBT b : proxies)
		{
			IICProxy proxy = IICProxy.readFromNBT(((CompoundNBT)b).getCompound("proxy"));
			for(INBT p : ((CompoundNBT)b).getList("points", NBT.TAG_COMPOUND))
			{
				ConnectionPoint point = new ConnectionPoint((CompoundNBT)p);
				loadConnector(point, proxy);
			}
		}
		ListNBT wires = subnet.getList("wires", NBT.TAG_COMPOUND);
		for(INBT b : wires)
		{
			Connection wire = new Connection((CompoundNBT)b);
			if(connectors.containsKey(wire.getEndA().getPosition())&&connectors.containsKey(wire.getEndB().getPosition()))
				addConnection(wire);
			else
				IELogger.logger.error("Wire from {} to {}, but connector points are {}", wire.getEndA(), wire.getEndB(), connectors);
		}
	}

	public LocalWireNetwork(GlobalWireNetwork globalNet)
	{
		this.globalNet = globalNet;
		handlerUserCount.defaultReturnValue(0);
	}

	public CompoundNBT writeToNBT()
	{
		ListNBT wires = new ListNBT();
		for(ConnectionPoint p : connections.keySet())
			for(Connection conn : connections.get(p))
				if(conn.isPositiveEnd(p))
					wires.add(conn.toNBT());
		CompoundNBT ret = new CompoundNBT();
		ret.put("wires", wires);
		Multimap<BlockPos, ConnectionPoint> connsByBlock = HashMultimap.create();
		for(ConnectionPoint cp : connections.keySet())
			connsByBlock.put(cp.getPosition(), cp);
		ListNBT proxies = new ListNBT();
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
				CompoundNBT complete = new CompoundNBT();
				complete.put("proxy", proxy.writeToNBT());
				ListNBT cps = new ListNBT();
				for(ConnectionPoint cp : connsByBlock.get(p))
					cps.add(cp.createTag());
				complete.put("points", cps);
				proxies.add(complete);
			}
		}
		ret.put("proxies", proxies);
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

	public Collection<Connection> getConnections(BlockPos at)
	{
		return getConnections(new ConnectionPoint(at, 0));
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
		addRequestedHandlers(iic);
		if(!connections.containsKey(p))
			connections.put(p, new ArrayList<>());
		for(LocalNetworkHandler h : handlers.values())
			h.onConnectorLoaded(p, iic);
	}

	private void addRequestedHandlers(ILocalHandlerProvider provider)
	{
		for(ResourceLocation loc : provider.getRequestedHandlers())
		{
			handlerUserCount.put(loc, handlerUserCount.getInt(loc)+1);
			if(!handlers.containsKey(loc))
				handlers.put(loc, LocalNetworkHandler.createHandler(loc, this));
			IELogger.logger.info("Increasing {} to {}", loc, handlerUserCount.getInt(loc));
		}
	}

	void unloadConnector(BlockPos p, IImmersiveConnectable iic)
	{
		for(LocalNetworkHandler h : handlers.values())
			h.onConnectorUnloaded(p, iic);
		removeHandlersFor(iic);
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
		result.handlerUserCount.putAll(other.handlerUserCount);
		for(Entry<ResourceLocation, LocalNetworkHandler> loc : handlers.entrySet())
		{
			result.handlers.merge(loc.getKey(), loc.getValue(), LocalNetworkHandler::merge);
			result.handlerUserCount.merge(loc.getKey(), handlerUserCount.getInt(loc.getKey()), (a, b) -> a+b);
			IELogger.logger.info("Merged {} to {}", loc.getKey(), result.handlers.get(loc.getKey()));
		}
		for(Entry<ResourceLocation, LocalNetworkHandler> loc : result.handlers.entrySet())
			loc.getValue().setLocalNet(result);
		return result;
	}

	void removeConnection(Connection c)
	{
		boolean successA = false, successB = false;
		Collection<Connection> connsA = connections.get(c.getEndA());
		if(connsA!=null)
			successA = connsA.removeIf(c::hasSameConnectors);
		Collection<Connection> connsB = connections.get(c.getEndB());
		if(connsB!=null)
			successB = connsB.removeIf(c::hasSameConnectors);
		if(!successA)
			IELogger.logger.info("Failed to remove {} from {} (A)", c, c.getEndA());
		if(!successB)
			IELogger.logger.info("Failed to remove {} from {} (B)", c, c.getEndB());
		for(LocalNetworkHandler h : handlers.values())
			h.onConnectionRemoved(c);
		removeHandlersFor(c.type);
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
		for(LocalNetworkHandler h : handlers.values())
			h.onConnectorRemoved(p, iic);
		removeHandlersFor(iic);
	}

	void addConnection(Connection conn)
	{
		IImmersiveConnectable connA = connectors.get(conn.getEndA().getPosition());
		if(connA==null)
			throw new AssertionError(conn.getEndA().getPosition());
		IImmersiveConnectable connB = connectors.get(conn.getEndB().getPosition());
		if(connB==null)
			throw new AssertionError(conn.getEndB().getPosition());
		connections.get(conn.getEndA()).add(conn);
		connections.get(conn.getEndB()).add(conn);
		for(LocalNetworkHandler h : handlers.values())
			h.onConnectionAdded(conn);
		addRequestedHandlers(conn.type);
		if(!(connA instanceof IICProxy)&&!(connB instanceof IICProxy))
			globalNet.getCollisionData().addConnection(conn);
	}

	private void removeHandlersFor(ILocalHandlerProvider iic)
	{
		for(ResourceLocation loc : iic.getRequestedHandlers())
		{
			if(!handlers.containsKey(loc))
				continue;//TODO throw new AssertionError("Expected to find handler for "+loc+" but didn't!");
			int remaining = handlerUserCount.get(loc)-1;
			handlerUserCount.put(loc, remaining);
			IELogger.logger.info("Decreasing {} to {}", loc, remaining);
			if(remaining <= 0)
			{
				IELogger.logger.info("Removing: {}", loc);
				handlers.remove(loc);
				handlerUserCount.remove(loc);
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
		for(LocalNetworkHandler handler : handlers.values())
			if(handler instanceof IWorldTickable)
				((IWorldTickable)handler).update(w);
	}

	@Nullable
	public <T extends LocalNetworkHandler> T getHandler(ResourceLocation name, Class<T> type)
	{
		LocalNetworkHandler p = handlers.get(name);
		if(p==null)
			return null;
		else
		{
			if(type.isInstance(p))
				return (T)p;
			else
				return null;
		}
	}

	public Collection<LocalNetworkHandler> getAllHandlers()
	{
		return handlers.values();
	}
}
