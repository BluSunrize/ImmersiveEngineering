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
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerProvider;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import blusunrize.immersiveengineering.api.wires.proxy.IICProxyProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import static blusunrize.immersiveengineering.api.utils.SafeChunkUtils.getSafeTE;
import static blusunrize.immersiveengineering.api.utils.SafeChunkUtils.isChunkSafe;

@EventBusSubscriber(modid = Lib.MODID)
public class GlobalWireNetwork implements IWorldTickable
{
	public static final SetRestrictedField<BooleanSupplier> SANITIZE_CONNECTIONS = SetRestrictedField.common();
	public static final SetRestrictedField<BooleanSupplier> VALIDATE_CONNECTIONS = SetRestrictedField.common();

	private static Level lastServerWorld = null;
	private static GlobalWireNetwork lastServerNet = null;

	private static Level lastClientWorld;
	private static GlobalWireNetwork lastClientNet;

	private final Map<ConnectionPoint, LocalWireNetwork> localNetsByPos = new HashMap<>();
	private final Set<LocalWireNetwork> localNetSet = new ReferenceOpenHashSet<>();
	private final WireCollisionData collisionData;
	private final IICProxyProvider proxyProvider;
	private final IWireSyncManager syncManager;

	private Map<Pair<BlockPos, Level>, IImmersiveConnectable> queuedLoads = new LinkedHashMap<>();

	@Nonnull
	public static GlobalWireNetwork getNetwork(Level w)
	{
		// This and onWorldUnload should only ever be called with non-remote worlds from the server thread, so this
		// does not need any synchronization
		if(!w.isClientSide&&w==lastServerWorld)
			return lastServerNet;
		if(w.isClientSide&&w==lastClientWorld)
			return lastClientNet;
		LazyOptional<GlobalWireNetwork> netOptional = w.getCapability(NetHandlerCapability.NET_CAPABILITY);
		if(!netOptional.isPresent())
			throw new RuntimeException("No net handler found for dimension "+w.dimension().location()+", client: "+w.isClientSide);
		GlobalWireNetwork ret = netOptional.orElseThrow(RuntimeException::new);
		if(!w.isClientSide)
		{
			lastServerWorld = w;
			lastServerNet = ret;
		}
		else
		{
			lastClientWorld = w;
			lastClientNet = ret;
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

	public GlobalWireNetwork(boolean isClientSide, IICProxyProvider proxyProvider, IWireSyncManager syncManager)
	{
		this.proxyProvider = proxyProvider;
		collisionData = new WireCollisionData(this, isClientSide);
		this.syncManager = syncManager;
	}

	public void addConnection(Connection conn)
	{
		processQueuedLoads();
		ConnectionPoint posA = conn.getEndA();
		ConnectionPoint posB = conn.getEndB();
		LocalWireNetwork netA = getLocalNet(posA);
		LocalWireNetwork netB = getLocalNet(posB);
		LocalWireNetwork joined;
		if(netA!=netB)
		{
			joined = netA.merge(netB, () -> new LocalWireNetwork(this));
			for(ConnectionPoint p : joined.getConnectionPoints())
				putLocalNet(p, joined);
		}
		else
			joined = netA;
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
		processQueuedLoads();
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
		processQueuedLoads();
		collisionData.removeConnection(c);
		LocalWireNetwork oldNet = getNullableLocalNet(c.getEndA());
		if(oldNet==null)
		{
			Preconditions.checkState(
					getNullableLocalNet(c.getEndB())==null,
					"Found net at %s but not at %s while removing connection %s",
					c.getEndB(),
					c.getEndA(),
					c
			);
			return;
		}
		Preconditions.checkNotNull(
				oldNet.getConnector(c.getEndB()),
				"Removing connection %s from net %s, but does not have connector for %s",
				c, oldNet, c.getEndB()
		);
		oldNet.removeConnection(c);
		splitNet(oldNet);
		syncManager.onConnectionRemoved(c);
	}

	public void removeAndDropConnection(Connection c, BlockPos dropAt, Level world)
	{
		removeConnection(c);
		double dx = dropAt.getX()+.5;
		double dy = dropAt.getY()+.5;
		double dz = dropAt.getZ()+.5;
		if(world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
			world.addFreshEntity(new ItemEntity(world, dx, dy, dz, c.type.getWireCoil(c)));
	}

	private void splitNet(LocalWireNetwork oldNet)
	{
		Collection<LocalWireNetwork> newNets = oldNet.split(this);
		for(LocalWireNetwork net : newNets)
			for(ConnectionPoint p : net.getConnectionPoints())
				putLocalNet(p, net);
	}

	public void readFromNBT(CompoundTag nbt)
	{
		localNetSet.forEach(LocalWireNetwork::setInvalid);
		localNetSet.clear();
		localNetsByPos.clear();
		ListTag locals = nbt.getList("locals", NBT.TAG_COMPOUND);
		for(Tag b : locals)
		{
			CompoundTag subnet = (CompoundTag)b;
			LocalWireNetwork localNet = new LocalWireNetwork(subnet, this);
			WireLogger.logger.info("Loading net {}", localNet);
			for(ConnectionPoint p : localNet.getConnectionPoints())
				putLocalNet(p, localNet);
		}
		queuedLoads.clear();
	}

	public CompoundTag writeToNBT()
	{
		CompoundTag ret = new CompoundTag();
		ListTag locals = new ListTag();
		for(LocalWireNetwork local : localNetSet)
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
		processQueuedLoads();
		LocalWireNetwork ret = localNetsByPos.computeIfAbsent(pos, p -> {
			LocalWireNetwork newNet = new LocalWireNetwork(this);
			IImmersiveConnectable proxy = proxyProvider.create(
					pos.getPosition(),
					ImmutableList.of(),
					ImmutableList.of()
			);
			newNet.addConnector(pos, proxy, this);
			localNetSet.add(newNet);
			return newNet;
		});
		Preconditions.checkState(ret.isValid(pos), "%s is not a valid net", ret);
		return ret;
	}

	public LocalWireNetwork getNullableLocalNet(BlockPos pos)
	{
		return getNullableLocalNet(new ConnectionPoint(pos, 0));
	}

	public LocalWireNetwork getNullableLocalNet(ConnectionPoint pos)
	{
		processQueuedLoads();
		LocalWireNetwork ret = localNetsByPos.get(pos);
		if(ret!=null)
			Preconditions.checkState(ret.isValid(pos), "%s is not valid for position %s", ret, pos);
		return ret;
	}

	public void removeConnector(IImmersiveConnectable iic)
	{
		processQueuedLoads();
		WireLogger.logger.info("Removing connector {} at {}", iic, iic.getPosition());
		Set<LocalWireNetwork> netsToRemoveFrom = new ObjectArraySet<>();
		final BlockPos iicPos = iic.getPosition();
		for(ConnectionPoint c : iic.getConnectionPoints())
		{
			LocalWireNetwork local = getNullableLocalNet(c);
			if(local!=null)
			{
				putLocalNet(c, null);
				netsToRemoveFrom.add(local);
			}
		}
		for(LocalWireNetwork net : netsToRemoveFrom)
		{
			net.removeConnector(iicPos);
			if (net.getConnectionPoints().isEmpty())
				localNetSet.remove(net);
			else
				splitNet(net);
		}
		validateNextTick = true;
	}

	@VisibleForTesting
	public void onConnectorLoad(IImmersiveConnectable iic, boolean remote)
	{
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
		if(isNew&&!remote)
			for(Connection c : iic.getInternalConnections())
			{
				Preconditions.checkArgument(c.isInternal(), "Internal connection for "+iic+"was not marked as internal!");
				addConnection(c);
			}
	}

	public void onConnectorLoad(IImmersiveConnectable iic, Level world)
	{
		queuedLoads.put(Pair.of(iic.getPosition(), world), iic);
	}

	private boolean processingLoadQueue = false;

	private void processQueuedLoads()
	{
		if(queuedLoads.isEmpty()||processingLoadQueue)
			return;
		processingLoadQueue = true;
		try
		{
			processQueuedLoadsInner();
		} finally
		{
			processingLoadQueue = false;
		}
	}

	private void processQueuedLoadsInner()
	{
		Map<Pair<BlockPos, Level>, IImmersiveConnectable> toProcess = queuedLoads;
		queuedLoads = new LinkedHashMap<>();
		for(Entry<Pair<BlockPos, Level>, IImmersiveConnectable> load : toProcess.entrySet())
		{
			IImmersiveConnectable iic = load.getValue();
			Level level = load.getKey().getSecond();
			if(isChunkSafe(level, load.getKey().getFirst()))
			{
				WireLogger.logger.info("Loading connector {} at {}", iic, iic.getPosition());
				if(validating)
					WireLogger.logger.error("Adding a connector during validation!");
				onConnectorLoad(iic, level.isClientSide);
				ApiUtils.addFutureServerTask(level, () -> initializeConnectionsOn(iic, level), true);
				validateNextTick = true;
				if(level.isClientSide)
					updateModelData(iic, level);
			}
			else
				queuedLoads.put(load.getKey(), load.getValue());
		}
	}

	private void updateModelData(IImmersiveConnectable iic, Level world)
	{
		for(ConnectionPoint cp : iic.getConnectionPoints())
		{
			LocalWireNetwork localNet = getLocalNet(cp);
			for(Connection c : getLocalNet(cp).getConnections(cp))
			{
				ConnectionPoint otherEnd = c.getOtherEnd(cp);
				IImmersiveConnectable otherIIC = localNet.getConnector(otherEnd);
				if(otherIIC instanceof BlockEntity)
					((BlockEntity)otherIIC).requestModelDataUpdate();
				BlockState state = world.getBlockState(otherEnd.getPosition());
				world.sendBlockUpdated(otherEnd.getPosition(), state, state, 3);
			}
			BlockState state = world.getBlockState(cp.getPosition());
			world.sendBlockUpdated(cp.getPosition(), state, state, 3);
		}
		if(iic instanceof BlockEntity)
			((BlockEntity)iic).requestModelDataUpdate();
	}

	private void initializeConnectionsOn(IImmersiveConnectable iic, Level world)
	{
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
						c.generateCatenaryData(world);
						if(!world.isClientSide)
						{
							WireLogger.logger.info("Here: {}, other end: {}", iic, iicEnd);
							collisionData.addConnection(c);
						}
					}
				}
			}
	}

	public void onConnectorUnload(IImmersiveConnectable iic)
	{
		BlockPos pos = iic.getPosition();
		processQueuedLoads();
		WireLogger.logger.info("Unloading connector {} at {}", iic, iic.getPosition());
		Map<LocalWireNetwork, Boolean> handledNets = new HashMap<>();
		for(ConnectionPoint connectionPoint : iic.getConnectionPoints())
		{
			LocalWireNetwork local = getLocalNet(connectionPoint);
			Boolean actuallyRemoved = handledNets.get(local);
			if(actuallyRemoved==null)
			{
				actuallyRemoved = local.unloadConnector(pos, iic);
				handledNets.put(local, actuallyRemoved);
			}
			if(actuallyRemoved)
				for(Connection c : local.getConnections(connectionPoint))
					collisionData.removeConnection(c);
		}
		validateNextTick = true;
	}

	private boolean validateNextTick = false;

	@Override
	public void update(Level world)
	{
		if(validateNextTick)
		{
			validate(world);
			validateNextTick = false;
		}
		processQueuedLoads();
		for(LocalWireNetwork net : localNetSet)
			net.update(world);
		if(SANITIZE_CONNECTIONS.getValue().getAsBoolean())
			NetworkSanitizer.tick(world, this);
	}

	boolean validating = false;

	private void validate(Level world)
	{
		if(world.isClientSide||!VALIDATE_CONNECTIONS.getValue().getAsBoolean())
			return;
		else
			WireLogger.logger.info("Validating wire network...");
		if(validating)
		{
			WireLogger.logger.error("Recursive validation call!");
			Thread.dumpStack();
		}
		validating = true;
		localNetsByPos.values().stream().distinct().forEach(
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
						if(localNetsByPos.get(cp)!=local)
							WireLogger.logger.warn("{} has net {}, but is in net {}", cp, localNetsByPos.get(cp), local);
						else
							for(Connection c : local.getConnections(cp))
							{
								if(localNetsByPos.get(c.getOtherEnd(cp))!=local)
									WireLogger.logger.warn("{} is connected to {}, but nets are {} and {}", cp,
											c.getOtherEnd(cp), localNetsByPos.get(c.getOtherEnd(cp)), local);
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
							BlockEntity inWorld = getSafeTE(world, p);
							if(inNet!=inWorld)
								WireLogger.logger.warn("Connector at {}: {} in Net, {} in World (Net is {})", p, inNet, inWorld, local);
						}
				}
		);
		Set<LocalWireNetwork> actualLocalSet = new ReferenceOpenHashSet<>(localNetsByPos.values());
		actualLocalSet.removeIf(lwn -> !lwn.isValid());
		if (!localNetSet.equals(actualLocalSet))
		{
			WireLogger.logger.warn("Local net set does not match value set of local nets by position");
			WireLogger.logger.warn("Actual set, but not in stored set: {}", new HashSet<>(Sets.difference(actualLocalSet, localNetSet)));
			WireLogger.logger.warn("Stored set, but not in actual set: {}", new HashSet<>(Sets.difference(localNetSet, actualLocalSet)));
		}
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
		for(ConnectionPoint cp : localNetsByPos.keySet())
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

	void removeConnector(BlockPos pos)
	{
		Collection<ConnectionPoint> cpsAtInvalid = new ArrayList<>();
		for(ConnectionPoint cp : localNetsByPos.keySet())
			if(cp.getPosition().equals(pos))
				cpsAtInvalid.add(cp);
		for(ConnectionPoint toRemove : cpsAtInvalid)
			removeCP(toRemove);
	}

	public void updateCatenaryData(Connection conn, Level world)
	{
		processQueuedLoads();
		collisionData.removeConnection(conn);
		conn.resetCatenaryData();
		conn.generateCatenaryData(world);
		collisionData.addConnection(conn);
	}

	private void putLocalNet(ConnectionPoint cp, @Nullable LocalWireNetwork net)
	{
		LocalWireNetwork oldNet = localNetsByPos.get(cp);
		if(oldNet!=null&&net!=null&&oldNet.isValid(cp))
		{
			WireLogger.logger.info("Marking {} as invalid", oldNet);
			oldNet.setInvalid();
			localNetSet.remove(oldNet);
		}
		if(net!=null)
		{
			localNetsByPos.put(cp, net);
			localNetSet.add(net);
		}
		else
			localNetsByPos.remove(cp);
	}

	public IImmersiveConnectable getExistingConnector(ConnectionPoint cp)
	{
		LocalWireNetwork local = getNullableLocalNet(cp);
		return Preconditions.checkNotNull(local, "No local net at %s", cp)
				.getConnector(cp);
	}

	public IICProxyProvider getProxyProvider()
	{
		return proxyProvider;
	}
}
