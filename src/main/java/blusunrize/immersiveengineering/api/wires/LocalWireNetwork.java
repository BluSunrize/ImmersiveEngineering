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
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
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
	//This is an array map since it will generally be tiny, and needs to be fast at those sizes
	private final Map<ResourceLocation, LocalNetworkHandler> handlers = new Object2ObjectArrayMap<>();
	//package private to allow GlobalWireNetwork#validate to read this
	//One user is either one ConnectionPoint in the net (NOT a BlockPos) or one connection
	final Map<ResourceLocation, Multiset<ILocalHandlerProvider>> handlerUsers = new HashMap<>();
	private List<Runnable> runNextTick = new ArrayList<>();
	private boolean isValid = true;

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
				addConnector(point, proxy);
			}
		}
		ListNBT wires = subnet.getList("wires", NBT.TAG_COMPOUND);
		for(INBT b : wires)
		{
			Connection wire = new Connection((CompoundNBT)b);
			if(connectors.containsKey(wire.getEndA().getPosition())&&connectors.containsKey(wire.getEndB().getPosition()))
				addConnection(wire);
			else
				WireLogger.logger.error("Wire from {} to {}, but connector points are {}", wire.getEndA(), wire.getEndB(), connectors);
		}
	}

	public LocalWireNetwork(GlobalWireNetwork globalNet)
	{
		this.globalNet = globalNet;
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

	void addConnector(ConnectionPoint p, IImmersiveConnectable iic)
	{
		Preconditions.checkState(!connections.containsKey(p));
		connections.put(p, new ArrayList<>());
		if(!connectors.containsKey(p.getPosition()))
			loadConnector(p.getPosition(), iic, true);
		else
		{
			Preconditions.checkState(getConnector(p)==iic);
			addRequestedHandlers(iic);
		}
	}

	void loadConnector(BlockPos p, IImmersiveConnectable iic, boolean adding)
	{
		IImmersiveConnectable existingIIC = connectors.get(p);
		if(adding)
			Preconditions.checkState(existingIIC==null);
		else
			Preconditions.checkState(existingIIC instanceof IICProxy, "Loading connector with current IIC "+existingIIC);
		connectors.put(p, iic);
		for(ConnectionPoint cp : iic.getConnectionPoints())
			if(connections.containsKey(cp))
			{
				addRequestedHandlers(iic);
				for(LocalNetworkHandler h : handlers.values())
					h.onConnectorLoaded(cp, iic);
			}
	}

	void unloadConnector(BlockPos p)
	{
		IImmersiveConnectable iic = connectors.get(p);
		Preconditions.checkState(iic!=null);
		Preconditions.checkState(!(iic instanceof IICProxy));
		for(LocalNetworkHandler h : handlers.values())
			h.onConnectorUnloaded(p, iic);
		for(ConnectionPoint cp : iic.getConnectionPoints())
			if(connections.containsKey(cp))
				removeHandlersFor(iic);
		connectors.put(p, new IICProxy((TileEntity)iic));
	}

	LocalWireNetwork merge(LocalWireNetwork other)
	{
		LocalWireNetwork result = new LocalWireNetwork(globalNet);
		for(LocalWireNetwork net : new LocalWireNetwork[]{this, other})
		{
			result.connectors.putAll(net.connectors);
			result.connections.putAll(net.connections);
		}
		result.handlers.putAll(other.handlers);
		other.handlerUsers.forEach((rl, h) -> result.handlerUsers.put(rl, HashMultiset.create(h)));
		WireLogger.logger.info("Merging {} and {}", result.handlerUsers, handlerUsers);
		for(Entry<ResourceLocation, LocalNetworkHandler> loc : handlers.entrySet())
		{
			result.handlers.merge(loc.getKey(), loc.getValue(), LocalNetworkHandler::merge);
			result.handlerUsers.merge(loc.getKey(), HashMultiset.create(handlerUsers.get(loc.getKey())), (a, b) -> {
				a.addAll(b);
				return a;
			});
			WireLogger.logger.info("Merged {} to {}", loc.getKey(), result.handlers.get(loc.getKey()));
		}
		WireLogger.logger.info("Result: {}", result.handlerUsers);
		for(Entry<ResourceLocation, LocalNetworkHandler> loc : result.handlers.entrySet())
			loc.getValue().setLocalNet(result);
		return result;
	}

	void removeConnection(Connection c)
	{
		for(ConnectionPoint end : new ConnectionPoint[]{c.getEndA(), c.getEndB()})
		{
			boolean success = false;
			Collection<Connection> conns = connections.get(end);
			if(conns!=null)
				success = conns.remove(c);
			if(!success)
				WireLogger.logger.error("Failed to remove {} from {}", c, c.getEndB());
		}
		for(ConnectionPoint end : new ConnectionPoint[]{c.getEndA(), c.getEndB()})
		{
			IImmersiveConnectable connector = connectors.get(end.getPosition());
			if(connector!=null)
				connector.removeCable(c, end);
		}
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
					WireLogger.logger.info("Cancelling, but connections {} at {} still exist!", connections.get(point),
							point);
			WireLogger.logger.info("Cancelled");
			return;
		}
		for(ConnectionPoint point : iic.getConnectionPoints())
			if(connections.containsKey(point))
			{
				removeHandlersFor(iic);
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
	}

	void addConnection(Connection conn)
	{
		IImmersiveConnectable connA = connectors.get(conn.getEndA().getPosition());
		Preconditions.checkNotNull(connA, conn.getEndA().getPosition());
		IImmersiveConnectable connB = connectors.get(conn.getEndB().getPosition());
		Preconditions.checkNotNull(connB, conn.getEndB().getPosition());
		if(connections.get(conn.getEndA()).stream().anyMatch(c -> c.getOtherEnd(conn.getEndA()).equals(conn.getEndB())))
		{
			WireLogger.logger.error("Tried to add a duplicate connection from {} ({}) to {} ({})",
					conn.getEndA(), connA,
					conn.getEndB(), connB
			);
			return;
		}
		connections.get(conn.getEndA()).add(conn);
		connections.get(conn.getEndB()).add(conn);
		for(LocalNetworkHandler h : handlers.values())
			h.onConnectionAdded(conn);
		addRequestedHandlers(conn.type);
	}

	private void removeHandlersFor(ILocalHandlerProvider iic)
	{
		for(ResourceLocation loc : iic.getRequestedHandlers())
		{
			Preconditions.checkState(handlers.containsKey(loc), "Expected to find handler for "+loc+"(provided by "+iic+")");
			Multiset<ILocalHandlerProvider> providers = getProvidersFor(loc);
			Preconditions.checkState(providers.contains(iic), "Expected to find handler "+iic+" for "+loc
					+". Found: "+providers);
			providers.remove(iic);
			WireLogger.logger.info("Removing {} from handlers for {}. Remaining: {}", iic, loc, providers);
			if(providers.isEmpty())
			{
				WireLogger.logger.info("Removing: {}", loc);
				handlers.remove(loc);
				handlerUsers.remove(loc);
			}
		}
	}

	private void addRequestedHandlers(ILocalHandlerProvider provider)
	{
		for(ResourceLocation loc : provider.getRequestedHandlers())
		{
			getProvidersFor(loc).add(provider);
			if(!handlers.containsKey(loc))
				handlers.put(loc, LocalNetworkHandler.createHandler(loc, this));
			WireLogger.logger.info("Adding handler {} for {}", loc, provider);
		}
	}

	private Multiset<ILocalHandlerProvider> getProvidersFor(ResourceLocation rl)
	{
		return handlerUsers.computeIfAbsent(rl, rl_ -> HashMultiset.create());
	}

	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return connections.keySet();
	}

	// Returns the nets that need to be changed
	Collection<LocalWireNetwork> split()
	{
		Set<ConnectionPoint> toVisit = new HashSet<>(getConnectionPoints());
		Collection<LocalWireNetwork> ret = new ArrayList<>();
		while(!toVisit.isEmpty())
		{
			Collection<ConnectionPoint> inComponent;
			{
				Iterator<ConnectionPoint> tmpIt = toVisit.iterator();
				inComponent = getConnectedComponent(tmpIt.next(), toVisit);
			}
			if(toVisit.size()==0&&ret.size()==0)
				// All still connected => no changed nets
				break;
			LocalWireNetwork newNet = new LocalWireNetwork(globalNet);
			for(ConnectionPoint p : inComponent)
				newNet.addConnector(p, connectors.get(p.getPosition()));
			for(ConnectionPoint p : inComponent)
				for(Connection c : getConnections(p))
					if(c.isPositiveEnd(p))
						newNet.addConnection(c);
			ret.add(newNet);
			WireLogger.logger.info("Subnet after split: {}, users {}", newNet, newNet.handlerUsers);
		}
		WireLogger.logger.info("Split net! Now {} nets: {}", ret.size(), ret);
		return ret;
	}

	private Collection<ConnectionPoint> getConnectedComponent(ConnectionPoint start, Set<ConnectionPoint> unvisited)
	{
		Deque<ConnectionPoint> open = new ArrayDeque<>();
		List<ConnectionPoint> inComponent = new ArrayList<>();
		open.push(start);
		unvisited.remove(start);
		while(!open.isEmpty())
		{
			ConnectionPoint curr = open.pop();
			inComponent.add(curr);
			for(Connection c : getConnections(curr))
			{
				ConnectionPoint otherEnd = c.getOtherEnd(curr);
				if(unvisited.contains(otherEnd))
				{
					unvisited.remove(otherEnd);
					open.push(otherEnd);
				}
			}
		}
		return inComponent;
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
		List<Runnable> toRun = runNextTick;
		runNextTick = new ArrayList<>();
		for(Runnable r : toRun)
			r.run();
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

	public void addAsFutureTask(Runnable r)
	{
		runNextTick.add(r);
	}

	// Internal use only, for network sanitization
	void removeCP(ConnectionPoint cp)
	{
		for(Connection c : getConnections(cp).toArray(new Connection[0]))
			removeConnection(c);
		connections.remove(cp);
		boolean hasMoreAtSameBlock = true;
		for(ConnectionPoint cp2 : connections.keySet())
			if(cp.getPosition().equals(cp2.getPosition()))
			{
				hasMoreAtSameBlock = false;
				break;
			}
		if(hasMoreAtSameBlock)
			removeConnector(cp.getPosition());
	}

	public void setInvalid()
	{
		isValid = false;
	}

	public boolean isValid()
	{
		return isValid;
	}

	public boolean isValid(ConnectionPoint cp)
	{
		return isValid&&connections.containsKey(cp);
	}
}
