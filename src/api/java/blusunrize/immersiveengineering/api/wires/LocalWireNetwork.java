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
import blusunrize.immersiveengineering.api.wires.proxy.IICProxyProvider;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class LocalWireNetwork implements IWorldTickable
{
	private final IICProxyProvider proxyProvider;
	private final Map<ConnectionPoint, Collection<Connection>> connections = new HashMap<>();
	private final Map<BlockPos, IImmersiveConnectable> connectors = new HashMap<>();
	//This is an array map since it will generally be tiny, and needs to be fast at those sizes
	private final Map<ResourceLocation, LocalNetworkHandler> handlers = new Object2ObjectArrayMap<>();
	//package private to allow GlobalWireNetwork#validate to read this
	//One user is either one ConnectionPoint in the net (NOT a BlockPos) or one connection
	final Map<ResourceLocation, Multiset<ILocalHandlerProvider>> handlerUsers = new HashMap<>();
	private List<Runnable> runNextTick = new ArrayList<>();
	private boolean isValid = true;
	private int version = 0;

	public LocalWireNetwork(CompoundTag subnet, GlobalWireNetwork globalNet)
	{
		this(globalNet);
		ListTag proxies = subnet.getList("proxies", NBT.TAG_COMPOUND);
		for(Tag b : proxies)
		{
			IImmersiveConnectable proxy = proxyProvider.fromNBT(((CompoundTag)b).getCompound("proxy"));
			for(Tag p : ((CompoundTag)b).getList("points", NBT.TAG_COMPOUND))
			{
				ConnectionPoint point = new ConnectionPoint((CompoundTag)p);
				addConnector(point, proxy, globalNet);
			}
		}
		ListTag wires = subnet.getList("wires", NBT.TAG_COMPOUND);
		for(Tag b : wires)
		{
			Connection wire = new Connection((CompoundTag)b);
			if(connectors.containsKey(wire.getEndA().getPosition())&&connectors.containsKey(wire.getEndB().getPosition()))
				addConnection(wire, globalNet);
			else
				WireLogger.logger.error("Wire from {} to {}, but connector points are {}", wire.getEndA(), wire.getEndB(), connectors);
		}
	}

	public LocalWireNetwork(GlobalWireNetwork globalNet)
	{
		this.proxyProvider = globalNet.getProxyProvider();
	}

	public CompoundTag writeToNBT()
	{
		ListTag wires = new ListTag();
		for(ConnectionPoint p : connections.keySet())
			for(Connection conn : connections.get(p))
				if(conn.isPositiveEnd(p))
					wires.add(conn.toNBT());
		CompoundTag ret = new CompoundTag();
		ret.put("wires", wires);
		Multimap<BlockPos, ConnectionPoint> connsByBlock = HashMultimap.create();
		for(ConnectionPoint cp : connections.keySet())
			connsByBlock.put(cp.getPosition(), cp);
		ListTag proxies = new ListTag();
		for(BlockPos p : connectors.keySet())
		{
			IImmersiveConnectable iic = connectors.get(p);
			IImmersiveConnectable proxy = null;
			if(iic.isProxy())
				proxy = iic;
			else
				proxy = proxyProvider.createFor(iic);
			if(proxy!=null)
			{
				CompoundTag complete = new CompoundTag();
				complete.put("proxy", proxyProvider.toNBT(proxy));
				ListTag cps = new ListTag();
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

	void addConnector(ConnectionPoint cp, IImmersiveConnectable iic, GlobalWireNetwork globalNet)
	{
		++version;
		{
			Collection<Connection> existing = connections.get(cp);
			Preconditions.checkState(
					existing==null,
					"Adding connection point %s with connector %s to net %s, but already has %s",
					cp, iic, this, existing
			);
		}
		connections.put(cp, new ArrayList<>());
		if(!connectors.containsKey(cp.getPosition()))
			loadConnector(cp.getPosition(), iic, true, globalNet);
		else
		{
			IImmersiveConnectable existing = getConnector(cp);
			Preconditions.checkState(
					existing==iic,
					"Existing connector in net %s is %s, but expected %s",
					this, existing, iic
			);
			addRequestedHandlers(iic, globalNet);
		}
	}

	void loadConnector(BlockPos p, IImmersiveConnectable iic, boolean adding, GlobalWireNetwork globalNet)
	{
		++version;
		IImmersiveConnectable existingIIC = connectors.get(p);
		if(adding)
			Preconditions.checkState(
					existingIIC==null,
					"Adding connector %s at %s in net %s, but IIC %s already exists there",
					iic, p, this, existingIIC
			);
		else if(existingIIC!=null&&!existingIIC.isProxy())
		{
			// This out-of-order case can happen in 1.16 when TEs are marked for unloading, but onChunkUnload isn't
			// actually called until a player logs back in, at which point the new TE can already be loaded
			WireLogger.logger.info(
					"Loading connector {} at {} in net {}, but current IIC is {}, unloading first",
					iic, p, this, existingIIC
			);
			unloadConnector(p, existingIIC);
		}
		connectors.put(p, iic);
		for(ConnectionPoint cp : iic.getConnectionPoints())
			if(connections.containsKey(cp))
			{
				addRequestedHandlers(iic, globalNet);
				for(LocalNetworkHandler h : handlers.values())
					h.onConnectorLoaded(cp, iic);
			}
	}

	boolean unloadConnector(BlockPos pos, @Nullable IImmersiveConnectable iicToRemove)
	{
		++version;
		IImmersiveConnectable existingIIC = connectors.get(pos);
		if(iicToRemove!=existingIIC)
		{
			// Out of order case, same as in loadConnector
			WireLogger.logger.info(
					"Tried to remove {} at {} from {}, skipping",
					iicToRemove, pos, this
			);
			return false;
		}
		Preconditions.checkState(
				existingIIC!=null,
				"Unloading connector at %s in net %s, but no connector is stored",
				pos, this
		);
		Preconditions.checkState(
				!existingIIC.isProxy(),
				"Unloading connector at %s in %s, but %s is already a proxy",
				pos, this, existingIIC
		);
		connectors.put(pos, proxyProvider.createFor(existingIIC));
		for(LocalNetworkHandler h : handlers.values())
			h.onConnectorUnloaded(pos, existingIIC);
		for(ConnectionPoint cp : existingIIC.getConnectionPoints())
			if(connections.containsKey(cp))
				removeHandlersFor(existingIIC);
		return true;
	}

	LocalWireNetwork merge(LocalWireNetwork other, Supplier<LocalWireNetwork> createNewNet)
	{
		LocalWireNetwork result = createNewNet.get();
		for(LocalWireNetwork net : new LocalWireNetwork[]{this, other})
		{
			result.connectors.putAll(net.connectors);
			result.connections.putAll(net.connections);
		}
		result.handlers.putAll(other.handlers);
		other.handlerUsers.forEach((rl, h) -> result.handlerUsers.put(rl, HashMultiset.create(h)));
		for(Entry<ResourceLocation, LocalNetworkHandler> loc : handlers.entrySet())
		{
			result.handlers.merge(loc.getKey(), loc.getValue(), LocalNetworkHandler::merge);
			result.handlerUsers.merge(loc.getKey(), HashMultiset.create(handlerUsers.get(loc.getKey())), (a, b) -> {
				a.addAll(b);
				return a;
			});
		}
		for(Entry<ResourceLocation, LocalNetworkHandler> loc : result.handlers.entrySet())
			loc.getValue().setLocalNet(result);
		return result;
	}

	void removeConnection(Connection c)
	{
		++version;
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
		++version;
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

	void addConnection(Connection conn, GlobalWireNetwork globalNet)
	{
		++version;
		IImmersiveConnectable connA = connectors.get(conn.getEndA().getPosition());
		Preconditions.checkNotNull(connA, "No connector at %s", conn.getEndA().getPosition());
		IImmersiveConnectable connB = connectors.get(conn.getEndB().getPosition());
		Preconditions.checkNotNull(connB, "No connector at %s", conn.getEndB().getPosition());
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
		addRequestedHandlers(conn.type, globalNet);
	}

	private void removeHandlersFor(ILocalHandlerProvider iic)
	{
		for(ResourceLocation loc : iic.getRequestedHandlers())
		{
			Preconditions.checkState(
					handlers.containsKey(loc),
					"Expected to find handler for %s (provided by %s), only found %s",
					loc, iic, handlers
			);
			Multiset<ILocalHandlerProvider> providers = getProvidersFor(loc);
			Preconditions.checkState(
					providers.contains(iic),
					"Expected to find handler %s for %s. Found: %s",
					iic, loc, providers
			);
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

	private void addRequestedHandlers(ILocalHandlerProvider provider, GlobalWireNetwork global)
	{
		for(ResourceLocation loc : provider.getRequestedHandlers())
		{
			getProvidersFor(loc).add(provider);
			if(!handlers.containsKey(loc))
				handlers.put(loc, LocalNetworkHandler.createHandler(loc, this, global));
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
	Collection<LocalWireNetwork> split(GlobalWireNetwork globalNet)
	{
		++version;
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
				newNet.addConnector(p, connectors.get(p.getPosition()), globalNet);
			for(ConnectionPoint p : inComponent)
				for(Connection c : getConnections(p))
					if(c.isPositiveEnd(p))
						newNet.addConnection(c, globalNet);
			ret.add(newNet);
		}
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

	@Override
	public void update(Level w)
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
		++version;
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
		++version;
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

	/**
	 * Returns the current "version" of the local network. If the version is the same as in a previous call the graph
	 * structure of this component (and its validity) is guaranteed to have stayed the same. The exact value has no
	 * meaning, and spurious changes are allowed.
	 */
	public int getVersion()
	{
		return version;
	}
}
