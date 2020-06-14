/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.wires.WireSyncManager;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.common.util.SafeChunkUtils.getSafeTE;
import static blusunrize.immersiveengineering.common.util.SafeChunkUtils.isChunkSafe;

public class GlobalWireNetwork implements ITickableTileEntity
{
	private final Map<ConnectionPoint, LocalWireNetwork> localNets = new HashMap<>();
	private final WireCollisionData collisionData = new WireCollisionData(this);
	private final World world;

	@Nonnull
	public static GlobalWireNetwork getNetwork(World w)
	{
		if(!w.getCapability(NetHandlerCapability.NET_CAPABILITY).isPresent())
			throw new RuntimeException("No net handler found for dimension "+w.getDimension().getType().getRegistryName()+", remote: "+w.isRemote);
		return Objects.requireNonNull(w.getCapability(NetHandlerCapability.NET_CAPABILITY).orElse(null));
	}

	public GlobalWireNetwork(World w)
	{
		world = w;
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
			WireLogger.logger.info("null-null");
			joined = new LocalWireNetwork(this);
			toSet.add(posA);
			toSet.add(posB);
			joined.loadConnector(posA, iicA);
			joined.loadConnector(posB, iicB);
		}
		else if(netA==null)
		{
			WireLogger.logger.info("null-non");
			toSet.add(posA);
			joined = netB;
			joined.loadConnector(posA, iicA);
		}
		else if(netB==null)
		{
			WireLogger.logger.info("non-null");
			toSet.add(posB);
			joined = netA;
			joined.loadConnector(posB, iicB);
		}
		else if(netA!=netB)
		{
			WireLogger.logger.info("non-non-different: {} and {}", netA, netB);
			joined = netA.merge(netB);
			toSet = joined.getConnectionPoints();
		}
		else
		{
			WireLogger.logger.info("non-non-same");
			joined = netA;
		}
		WireLogger.logger.info("Result: {}, to set: {}", joined, toSet);
		for(ConnectionPoint p : toSet)
			putLocalNet(p, joined);
		joined.addConnection(conn);
		WireSyncManager.onConnectionAdded(conn, world);
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
		if(!world.isRemote)
			collisionData.removeConnection(c);
		LocalWireNetwork oldNet = localNets.get(c.getEndA());
		if(oldNet==null||oldNet.getConnector(c.getEndB())==null)
			return;
		oldNet.removeConnection(c);
		splitNet(oldNet);
		WireSyncManager.onConnectionRemoved(c, world);
	}

	public void removeAndDropConnection(Connection c, BlockPos dropAt)
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
		Collection<LocalWireNetwork> newNets = oldNet.split();
		for(LocalWireNetwork net : newNets)
			if(net!=oldNet)
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
		return localNets.computeIfAbsent(pos, p -> {
			LocalWireNetwork ret = new LocalWireNetwork(this);
			ret.loadConnector(pos, new IICProxy(world.dimension.getType(), pos.getPosition()));
			return ret;
		});
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
		for(ConnectionPoint cp : iic.getConnectionPoints())
		{
			if(getNullableLocalNet(cp)==null)
				isNew = true;
			LocalWireNetwork local = getLocalNet(cp);
			local.loadConnector(cp, iic);
		}
		if(isNew&&!world.isRemote)
		{
			for(Connection c : iic.getInternalConnections())
			{
				Preconditions.checkArgument(c.isInternal(), "Internal connection for "+iic+"was not marked as internal!");
				addConnection(c);
			}
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
						if(!(iicEnd instanceof IICProxy))
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
		if(world.isRemote)
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
					BlockState state = world.getBlockState(otherEnd.getPosition());
					world.notifyBlockUpdate(otherEnd.getPosition(), state, state, 3);
				}
				BlockState state = world.getBlockState(cp.getPosition());
				world.notifyBlockUpdate(cp.getPosition(), state, state, 3);
			}
			if(iic instanceof TileEntity)
				((TileEntity)iic).requestModelDataUpdate();
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
		if(!world.isRemote)
			for(ConnectionPoint cp : iic.getConnectionPoints())
				for(Connection c : getLocalNet(cp).getConnections(cp))
					collisionData.removeConnection(c);
		validateNextTick = true;
	}

	private boolean validateNextTick = false;

	@Override
	public void tick()
	{
		if(validateNextTick)
		{
			validate();
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

	private void validate()
	{
		if(world.isRemote||!IEConfig.WIRES.enableWireLogger.get())
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
					Object2IntMap<ResourceLocation> handlers = new Object2IntOpenHashMap<>();
					for(ConnectionPoint cp : local.getConnectionPoints())
					{
						IImmersiveConnectable iic = local.getConnector(cp);
						if(!iic.getConnectionPoints().contains(cp))
						{
							WireLogger.logger.warn("Connection point {} does not exist on {}", cp, iic);
							continue;
						}
						for(ResourceLocation rl : iic.getRequestedHandlers())
							handlers.put(rl, handlers.getInt(rl)+1);
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
										handlers.put(rl, handlers.getInt(rl)+1);
							}
					}
					for(ResourceLocation rl : handlers.keySet())
					{
						int countInNet = local.handlerUserCount.getInt(rl);
						int actualCount = handlers.getInt(rl);
						if(countInNet!=actualCount)
							WireLogger.logger.warn("Expected to find {} users of {}, but found {}", countInNet, rl, actualCount);
					}
					for(ResourceLocation rl : local.handlerUserCount.keySet())
						if(!handlers.containsKey(rl))
							WireLogger.logger.warn("Found no users for {}, but net expects {}", rl, local.handlerUserCount.getInt(rl));
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

	private void putLocalNet(ConnectionPoint cp, @Nullable LocalWireNetwork net)
	{
		LocalWireNetwork oldNet = localNets.get(cp);
		if(oldNet!=null)
			oldNet.setInvalid();
		if(net!=null)
			localNets.put(cp, net);
		else
			localNets.remove(cp);
	}
}
