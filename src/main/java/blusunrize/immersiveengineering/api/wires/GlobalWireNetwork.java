/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerProvider;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import blusunrize.immersiveengineering.api.wires.proxy.IICProxyProvider;
import blusunrize.immersiveengineering.common.IEConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static blusunrize.immersiveengineering.common.util.SafeChunkUtils.getSafeTE;
import static blusunrize.immersiveengineering.common.util.SafeChunkUtils.isChunkSafe;

@EventBusSubscriber(modid = Lib.MODID)
public class GlobalWireNetwork implements IWorldTickable
{
	private static World lastServerWorld = null;
	private static GlobalWireNetwork lastServerNet = null;

	private final Map<ConnectionPoint, LocalWireNetwork> localNets = new HashMap<>();
	private final WireCollisionData collisionData;
	private final IICProxyProvider proxyProvider;
	private final IWireSyncManager syncManager;

	@Nonnull
	public static GlobalWireNetwork getNetwork(World w)
	{
		// This and onWorldUnload should only ever be called with non-remote worlds from the server thread, so this
		// does not need any synchronization
		if(!w.isRemote&&w==lastServerWorld)
			return lastServerNet;
		LazyOptional<GlobalWireNetwork> netOptional = w.getCapability(NetHandlerCapability.NET_CAPABILITY);
		if(!netOptional.isPresent())
			throw new RuntimeException("No net handler found for dimension "+w.getDimension().getType().getRegistryName()+", remote: "+w.isRemote);
		GlobalWireNetwork ret = netOptional.orElseThrow(RuntimeException::new);
		if(!w.isRemote)
		{
			lastServerWorld = w;
			lastServerNet = ret;
		}
		return ret;
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload ev)
	{
		if(ev.getWorld()==lastServerWorld)
		{
			lastServerNet = null;
			lastServerWorld = null;
		}
	}

	public GlobalWireNetwork(boolean remote, IICProxyProvider proxyProvider, IWireSyncManager syncManager)
	{
		this.proxyProvider = proxyProvider;
		collisionData = new WireCollisionData(this, remote);
		this.syncManager = syncManager;
	}

	public void addConnection(Connection conn)
	{
		ConnectionPoint posA = conn.getEndA();
		ConnectionPoint posB = conn.getEndB();
		LocalWireNetwork netA = getLocalNet(posA);
		LocalWireNetwork netB = getLocalNet(posB);
		LocalWireNetwork joined;
		if(netA!=netB)
		{
			WireLogger.logger.info("non-non-different: {} and {}", netA, netB);
			joined = netA.merge(netB, () -> new LocalWireNetwork(this));
			for(ConnectionPoint p : joined.getConnectionPoints())
				putLocalNet(p, joined);
		}
		else
		{
			WireLogger.logger.info("non-non-same");
			joined = netA;
		}
		WireLogger.logger.info("Result: {}", joined);
		joined.addConnection(conn, this);
		syncManager.onConnectionAdded(conn);
		IImmersiveConnectable connA = joined.getConnector(posA);
		IImmersiveConnectable connB = joined.getConnector(posB);
		if(connA!=null&&connB!=null&&!connA.isProxy()&&!connB.isProxy())
			collisionData.addConnection(conn);
		validateNextTick = true;
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
		validateNextTick = true;
	}

	public void removeConnection(Connection c)
	{
		collisionData.removeConnection(c);
		LocalWireNetwork oldNet = getNullableLocalNet(c.getEndA());
		if(oldNet==null)
		{
			Preconditions.checkState(getNullableLocalNet(c.getEndB())==null);
			return;
		}
		Preconditions.checkNotNull(oldNet.getConnector(c.getEndB()));
		oldNet.removeConnection(c);
		splitNet(oldNet);
		syncManager.onConnectionRemoved(c);
	}

	public void removeAndDropConnection(Connection c, BlockPos dropAt, World world)
	{
		removeConnection(c);
		double dx = dropAt.getX()+.5;
		double dy = dropAt.getY()+.5;
		double dz = dropAt.getZ()+.5;
		if(world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
			world.addEntity(new ItemEntity(world, dx, dy, dz, c.type.getWireCoil(c)));
	}

	private void splitNet(LocalWireNetwork oldNet)
	{
		Collection<LocalWireNetwork> newNets = oldNet.split(this);
		for(LocalWireNetwork net : newNets)
			for(ConnectionPoint p : net.getConnectionPoints())
				putLocalNet(p, net);
	}

	public void readFromNBT(CompoundNBT nbt)
	{
		localNets.values().forEach(LocalWireNetwork::setInvalid);
		localNets.clear();
		ListNBT locals = nbt.getList("locals", NBT.TAG_COMPOUND);
		for(INBT b : locals)
		{
			CompoundNBT subnet = (CompoundNBT)b;
			LocalWireNetwork localNet = new LocalWireNetwork(subnet, this);
			WireLogger.logger.info("Loading net {}", localNet);
			for(ConnectionPoint p : localNet.getConnectionPoints())
				putLocalNet(p, localNet);
		}
	}

	public CompoundNBT writeToNBT()
	{
		CompoundNBT ret = new CompoundNBT();
		ListNBT locals = new ListNBT();
		Set<LocalWireNetwork> savedNets = Collections.newSetFromMap(new IdentityHashMap<>());
		for(LocalWireNetwork local : localNets.values())
			if(savedNets.add(local))
				locals.add(local.writeToNBT());
		ret.put("locals", locals);
		return ret;
	}

	public LocalWireNetwork getLocalNet(BlockPos pos)
	{
		return getLocalNet(new ConnectionPoint(pos, 0));
	}

	public LocalWireNetwork getLocalNet(ConnectionPoint pos)
	{
		LocalWireNetwork ret = localNets.computeIfAbsent(pos, p -> {
			LocalWireNetwork newNet = new LocalWireNetwork(this);
			IImmersiveConnectable proxy = proxyProvider.create(
					pos.getPosition(),
					ImmutableList.of(),
					ImmutableList.of()
			);
			newNet.addConnector(pos, proxy, this);
			return newNet;
		});
		Preconditions.checkState(ret.isValid(pos), "%s is not a valid net", ret);
		return ret;
	}

	public LocalWireNetwork getNullableLocalNet(BlockPos pos)
	{
		return localNets.get(new ConnectionPoint(pos, 0));
	}

	public LocalWireNetwork getNullableLocalNet(ConnectionPoint pos)
	{
		return localNets.get(pos);
	}

	public void removeConnector(IImmersiveConnectable iic)
	{
		WireLogger.logger.info("Removing {}", iic);
		Set<LocalWireNetwork> netsToRemoveFrom = new ObjectArraySet<>();
		BlockPos iicPos = null;
		for(ConnectionPoint c : iic.getConnectionPoints())
		{
			WireLogger.logger.info("Sub-point {}", c);
			LocalWireNetwork local = getNullableLocalNet(c);
			if(local!=null)
			{
				WireLogger.logger.info("Removing");
				putLocalNet(c, null);
				netsToRemoveFrom.add(local);
				if(iicPos!=null)
					Preconditions.checkState(iicPos.equals(c.getPosition()));
				else
					iicPos = c.getPosition();
			}
		}
		for(LocalWireNetwork net : netsToRemoveFrom)
		{
			net.removeConnector(Preconditions.checkNotNull(iicPos));
			splitNet(net);
		}
		validateNextTick = true;
	}

	public void onConnectorLoad(IImmersiveConnectable iic, World w)
	{
		if(validating)
			WireLogger.logger.error("Adding a connector during validation!");
		boolean isNew = false;
		Set<LocalWireNetwork> loadedInNets = new HashSet<>();
		for(ConnectionPoint cp : iic.getConnectionPoints())
		{
			if(getNullableLocalNet(cp)==null)
				isNew = true;
			LocalWireNetwork local = getLocalNet(cp);
			if(loadedInNets.add(local))
				local.loadConnector(cp.getPosition(), iic, false, this);
		}
		if(isNew&&!w.isRemote)
			for(Connection c : iic.getInternalConnections())
			{
				Preconditions.checkArgument(c.isInternal(), "Internal connection for "+iic+"was not marked as internal!");
				addConnection(c);
			}
		ApiUtils.addFutureServerTask(w, () -> {
			for(ConnectionPoint cp : iic.getConnectionPoints())
				for(Connection c : getLocalNet(cp).getConnections(cp))
				{
					ConnectionPoint otherEnd = c.getOtherEnd(cp);
					LocalWireNetwork otherLocal = getNullableLocalNet(otherEnd);
					if(otherLocal!=null)
					{
						IImmersiveConnectable iicEnd = otherLocal.getConnector(otherEnd);
						if(!iicEnd.isProxy())
						{
							c.generateCatenaryData(w);
							if(!w.isRemote)
							{
								WireLogger.logger.info("Here: {}, other end: {}", iic, iicEnd);
								collisionData.addConnection(c);
							}
						}
					}
				}
		}, true);
		validateNextTick = true;
		//TODO this really should be somewhere else...
		if(w.isRemote)
		{
			for(ConnectionPoint cp : iic.getConnectionPoints())
			{
				LocalWireNetwork localNet = getLocalNet(cp);
				for(Connection c : getLocalNet(cp).getConnections(cp))
				{
					ConnectionPoint otherEnd = c.getOtherEnd(cp);
					IImmersiveConnectable otherIIC = localNet.getConnector(otherEnd);
					if(otherIIC instanceof TileEntity)
						((TileEntity)otherIIC).requestModelDataUpdate();
					BlockState state = w.getBlockState(otherEnd.getPosition());
					w.notifyBlockUpdate(otherEnd.getPosition(), state, state, 3);
				}
				BlockState state = w.getBlockState(cp.getPosition());
				w.notifyBlockUpdate(cp.getPosition(), state, state, 3);
			}
			if(iic instanceof TileEntity)
				((TileEntity)iic).requestModelDataUpdate();
		}
	}

	public void onConnectorUnload(BlockPos pos, IImmersiveConnectable iic)
	{
		Set<LocalWireNetwork> handledNets = new HashSet<>();
		for(ConnectionPoint connectionPoint : iic.getConnectionPoints())
		{
			LocalWireNetwork local = getLocalNet(connectionPoint);
			if(handledNets.add(local))
				local.unloadConnector(pos);
		}
		for(ConnectionPoint cp : iic.getConnectionPoints())
			for(Connection c : getLocalNet(cp).getConnections(cp))
				collisionData.removeConnection(c);
		validateNextTick = true;
	}

	private boolean validateNextTick = false;

	@Override
	public void update(World world)
	{
		if(validateNextTick)
		{
			validate(world);
			validateNextTick = false;
		}
		Set<LocalWireNetwork> ticked = new HashSet<>();
		for(LocalWireNetwork net : localNets.values())
			if(ticked.add(net))
				net.update(world);
		if(IEConfig.WIRES.sanitizeConnections.get())
			NetworkSanitizer.tick(world, this);
	}

	boolean validating = false;

	private void validate(World world)
	{
		if(world.isRemote||!IEConfig.WIRES.validateNet.get())
		{
			WireLogger.logger.info("Skipping validation!");
			return;
		}
		else
			WireLogger.logger.info("Validating wire network...");
		if(validating)
		{
			WireLogger.logger.error("Recursive validation call!");
			Thread.dumpStack();
		}
		validating = true;
		localNets.values().stream().distinct().forEach(
				(local) -> {
					Map<ResourceLocation, Multiset<ILocalHandlerProvider>> handlerUsers = new HashMap<>();
					Function<ResourceLocation, Multiset<ILocalHandlerProvider>> getHandler = rl ->
							handlerUsers.computeIfAbsent(rl, r -> HashMultiset.create());
					for(ConnectionPoint cp : local.getConnectionPoints())
					{
						IImmersiveConnectable iic = local.getConnector(cp);
						if(!iic.getConnectionPoints().contains(cp))
						{
							WireLogger.logger.warn("Connection point {} does not exist on {}", cp, iic);
							continue;
						}
						for(ResourceLocation rl : iic.getRequestedHandlers())
							getHandler.apply(rl).add(iic);
						if(localNets.get(cp)!=local)
							WireLogger.logger.warn("{} has net {}, but is in net {}", cp, localNets.get(cp), local);
						else
							for(Connection c : local.getConnections(cp))
							{
								if(localNets.get(c.getOtherEnd(cp))!=local)
									WireLogger.logger.warn("{} is connected to {}, but nets are {} and {}", cp,
											c.getOtherEnd(cp), localNets.get(c.getOtherEnd(cp)), local);
								else if(!local.getConnections(c.getOtherEnd(cp)).contains(c))
									WireLogger.logger.warn("Connection {} from {} to {} is a diode!", c, cp,
											c.getOtherEnd(cp));
								if(c.isPositiveEnd(cp))
									for(ResourceLocation rl : c.type.getRequestedHandlers())
										getHandler.apply(rl).add(c.type);
							}
					}
					for(ResourceLocation rl : handlerUsers.keySet())
					{
						Multiset<ILocalHandlerProvider> actual = local.handlerUsers.get(rl);
						Multiset<ILocalHandlerProvider> expected = handlerUsers.get(rl);
						if(!actual.equals(expected))
							WireLogger.logger.warn("Expected users for {}: {}, but found {}", rl, expected, actual);
					}
					for(ResourceLocation rl : local.handlerUsers.keySet())
						if(!handlerUsers.containsKey(rl))
							WireLogger.logger.warn("Found no users for {}, but net expects {}", rl, local.handlerUsers.get(rl));
					for(BlockPos p : local.getConnectors())
						if(isChunkSafe(world, p))
						{
							IImmersiveConnectable inNet = local.getConnector(p);
							TileEntity inWorld = getSafeTE(world, p);
							if(inNet!=inWorld)
								WireLogger.logger.warn("Connector at {}: {} in Net, {} in World (Net is {})", p, inNet, inWorld, local);
						}
				}
		);
		WireLogger.logger.info("Validated!");
		validating = false;
	}

	public WireCollisionData getCollisionData()
	{
		return collisionData;
	}

	public Collection<ConnectionPoint> getAllConnectorsIn(ChunkPos pos)
	{
		//TODO better way of finding all connectors in a chunk
		Collection<ConnectionPoint> ret = new ArrayList<>();
		for(ConnectionPoint cp : localNets.keySet())
			if(pos.equals(new ChunkPos(cp.getPosition())))
				ret.add(cp);
		return ret;
	}

	// Internal use only, for network sanitization
	void removeCP(ConnectionPoint cp)
	{
		LocalWireNetwork local = getNullableLocalNet(cp);
		if(local!=null)
			local.removeCP(cp);
	}

	public void removeConnector(BlockPos pos)
	{
		Collection<ConnectionPoint> cpsAtInvalid = new ArrayList<>();
		for(ConnectionPoint cp : localNets.keySet())
			if(cp.getPosition().equals(pos))
				cpsAtInvalid.add(cp);
		for(ConnectionPoint toRemove : cpsAtInvalid)
			removeCP(toRemove);
	}

	public void updateCatenaryData(Connection conn, World world)
	{
		collisionData.removeConnection(conn);
		conn.resetCatenaryData();
		conn.generateCatenaryData(world);
		collisionData.addConnection(conn);
	}

	private void putLocalNet(ConnectionPoint cp, @Nullable LocalWireNetwork net)
	{
		LocalWireNetwork oldNet = localNets.get(cp);
		if(oldNet!=null&&net!=null&&oldNet.isValid(cp))
		{
			WireLogger.logger.info("Marking {} as invalid", oldNet);
			oldNet.setInvalid();
		}
		if(net!=null)
			localNets.put(cp, net);
		else
			localNets.remove(cp);
	}

	public IImmersiveConnectable getConnector(ConnectionPoint cpB)
	{
		LocalWireNetwork local = getNullableLocalNet(cpB);
		return Preconditions.checkNotNull(local).getConnector(cpB);
	}

	public IICProxyProvider getProxyProvider()
	{
		return proxyProvider;
	}
}
