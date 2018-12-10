/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.api.ApiUtils.*;
import static java.util.Collections.newSetFromMap;

public class ImmersiveNetHandler
{
	public static final ImmersiveNetHandler INSTANCE = new ImmersiveNetHandler();
	public final BlockPlaceListener LISTENER = new BlockPlaceListener();
	public Map<Integer, ConcurrentHashMap<BlockPos, Set<Connection>>> directConnections = new ConcurrentHashMap<>();
	public TIntObjectMap<Map<BlockPos, Set<AbstractConnection>>> indirectConnections
			= new TIntObjectHashMap<>();
	public TIntObjectMap<Map<BlockPos, Set<AbstractConnection>>> indirectConnectionsIgnoreOut
			= new TIntObjectHashMap<>();
	public Map<Integer, HashMap<Connection, Integer>> transferPerTick = new HashMap<>();
	public Map<DimensionBlockPos, IICProxy> proxies = new ConcurrentHashMap<>();

	public IntHashMap<Map<BlockPos, BlockWireInfo>> blockWireMap = new IntHashMap<>();

	private ConcurrentHashMap<BlockPos, Set<Connection>> getMultimap(int dimension)
	{
		if(directConnections.get(dimension)==null)
		{
			ConcurrentHashMap<BlockPos, Set<Connection>> mm = new ConcurrentHashMap<BlockPos, Set<Connection>>();
			directConnections.put(dimension, mm);
		}
		return directConnections.get(dimension);
	}

	public HashMap<Connection, Integer> getTransferedRates(int dimension)
	{
		if(!transferPerTick.containsKey(dimension))
			transferPerTick.put(dimension, new HashMap<Connection, Integer>());
		return transferPerTick.get(dimension);
	}

	public void addConnection(World world, BlockPos node, BlockPos connection, int distance, WireType cableType)
	{
		addAndGetConnection(world, node, connection, distance, cableType);
	}

	public Connection addAndGetConnection(World world, BlockPos node, BlockPos connection, int distance, WireType cableType)
	{
		Connection conn = new Connection(node, connection, cableType, distance);
		addConnection(world, node, conn);
		addConnection(world, connection, new Connection(connection, node, cableType, distance));
		if(world.isBlockLoaded(node))
			world.addBlockEvent(node, world.getBlockState(node).getBlock(), -1, 0);
		if(world.isBlockLoaded(connection))
			world.addBlockEvent(connection, world.getBlockState(connection).getBlock(), -1, 0);
		return conn;
	}

	public void addConnection(World world, BlockPos node, Connection con)
	{
		addConnection(world.provider.getDimension(), node, con);
		resetCachedIndirectConnections(world, node);
	}

	public void addConnection(int world, BlockPos node, Connection con)
	{
		if(!getMultimap(world).containsKey(node))
			getMultimap(world).put(node, newSetFromMap(new ConcurrentHashMap<>()));
		getMultimap(world).get(node).add(con);
		IESaveData.setDirty(world);
	}

	public <T extends TileEntity & IImmersiveConnectable> void onTEValidated(T te)
	{
		Set<Connection> conns = getConnections(te.getWorld(), te.getPos());
		if(conns!=null)
			for(Connection con : conns)
				addBlockData(te.getWorld(), con);
		resetCachedIndirectConnections(te.getWorld(), te.getPos());
		setProxy(new DimensionBlockPos(te), null);
	}

	public void addBlockData(World world, Connection con)
	{
		int dimId = world.provider.getDimension();
		if(!blockWireMap.containsItem(dimId))
			blockWireMap.addKey(dimId, new ConcurrentHashMap<>());
		Map<BlockPos, BlockWireInfo> mapForDim = blockWireMap.lookup(dimId);
		if(mapForDim==null||!world.isBlockLoaded(con.end))
			return;
		raytraceAlongCatenary(con, world, (p) ->
		{
			if(!mapForDim.containsKey(p.getLeft()))
				mapForDim.put(p.getLeft(), new BlockWireInfo());
			if(mapForDim.get(p.getLeft()).in.stream().noneMatch((c) -> c.getLeft().hasSameConnectors(con)))
				mapForDim.get(p.getLeft()).in.add(new ImmutableTriple<>(con, p.getMiddle(), p.getRight()));
			return false;
		}, (p) ->
		{
			if(!mapForDim.containsKey(p.getLeft()))
				mapForDim.put(p.getLeft(), new BlockWireInfo());
			if(mapForDim.get(p.getLeft()).near.stream().noneMatch((c) -> c.getLeft().hasSameConnectors(con)))
				mapForDim.get(p.getLeft()).near.add(new ImmutableTriple<>(con, p.getMiddle(), p.getRight()));
		});
	}


	public void removeConnection(World world, Connection con)
	{
		removeConnection(world, con, getVecForIICAt(world, con.start, con), getVecForIICAt(world, con.end, con));
	}

	public void removeConnection(World world, Connection con, Vec3d vecStart, Vec3d vecEnd)
	{
		if(con==null||world==null)
			return;
		int dim = world.provider.getDimension();
		resetCachedIndirectConnections(world, con.start);
		Map<BlockPos, Set<Connection>> connsInDim = getMultimap(world.provider.getDimension());
		Set<Connection> reverseConns = connsInDim.get(con.end);
		Set<Connection> forwardConns = connsInDim.get(con.start);
		Optional<Connection> back = reverseConns.stream().filter(con::hasSameConnectors).findAny();
		reverseConns.removeIf(con::hasSameConnectors);
		forwardConns.removeIf(con::hasSameConnectors);
		Map<BlockPos, BlockWireInfo> mapForDim = blockWireMap.lookup(world.provider.getDimension());
		BiConsumer<BlockPos, Map<BlockPos, BlockWireInfo>> handle = (p, map) -> {
			if(mapForDim!=null)
			{
				BlockWireInfo info = map.get(p);
				if(info!=null)
				{
					for(int i = 0; i < 2; i++)
					{
						Set<Triple<Connection, Vec3d, Vec3d>> s = i==0?info.in: info.near;
						s.removeIf((t) -> t.getLeft().hasSameConnectors(con));
						if(s.isEmpty())
							map.remove(p);
					}
					if(info.near.isEmpty()&&info.in.isEmpty())
						map.remove(p);
				}
			}
		};
		raytraceAlongCatenaryRelative(con, (p) -> {
			handle.accept(p.getLeft(), mapForDim);
			return false;
		}, (p) -> handle.accept(p.getLeft(), mapForDim), vecStart, vecEnd);

		IImmersiveConnectable iic = toIIC(con.end, world);
		if(iic!=null)
		{
			iic.removeCable(con);
			back.ifPresent(iic::removeCable);
		}
		iic = toIIC(con.start, world);
		if(iic!=null)
		{
			iic.removeCable(con);
			back.ifPresent(iic::removeCable);
		}

		if(world.isBlockLoaded(con.start))
			world.addBlockEvent(con.start, world.getBlockState(con.start).getBlock(), -1, 0);
		if(world.isBlockLoaded(con.end))
			world.addBlockEvent(con.end, world.getBlockState(con.end).getBlock(), -1, 0);

		IESaveData.setDirty(dim);
	}

	public Set<Integer> getRelevantDimensions()
	{
		return directConnections.keySet();
	}

	public Collection<Connection> getAllConnections(int dimensionId)
	{
		Set<Connection> ret = newSetFromMap(new ConcurrentHashMap<Connection, Boolean>());
		for(Set<Connection> conlist : getMultimap(dimensionId).values())
			ret.addAll(conlist);
		return ret;
	}

	public Collection<Connection> getAllConnections(World world)
	{
		return getAllConnections(world.provider.getDimension());
	}

	@Nullable
	public synchronized Set<Connection> getConnections(World world, BlockPos node)
	{
		if(world!=null&&world.provider!=null)
		{
			return getConnections(world.provider.getDimension(), node);
		}
		return null;
	}

	@Nullable
	public synchronized Set<Connection> getConnections(int world, BlockPos node)
	{
		ConcurrentHashMap<BlockPos, Set<Connection>> map = getMultimap(world);
		return map.get(node);
	}

	public void clearAllConnections(World world)
	{
		clearAllConnections(world.provider.getDimension());
	}

	public void clearAllConnections(int world)
	{
		getMultimap(world).clear();
		blockWireMap.removeObject(world);
		proxies.clear();
	}

	public void clearConnectionsOriginatingFrom(BlockPos node, World world)
	{
		resetCachedIndirectConnections(world, node);
		if(getMultimap(world.provider.getDimension()).containsKey(node))
			getMultimap(world.provider.getDimension()).get(node).clear();
	}

	/**
	 * Reset ALL cached connections, anywhere, in any dimension. Use resetCachedIndirectConnections(int, BlockPos) below
	 */
	@Deprecated
	public void resetCachedIndirectConnections()
	{
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			indirectConnections.clear();
			indirectConnectionsIgnoreOut.clear();
		}
		else
			ImmersiveEngineering.proxy.clearConnectionModelCache();
	}

	/**
	 * Resets the cached energy connections for all connectors connected to an IIC.
	 * Call this when there are "more" wires (before removing and after adding)
	 *
	 * @param w     the world to reset the cache in
	 * @param start the starting block. If this is null, all cached connections for the dimension will be reset
	 */
	public void resetCachedIndirectConnections(World w, @Nullable BlockPos start)
	{
		if(FMLCommonHandler.instance().getEffectiveSide()!=Side.SERVER)
		{
			ImmersiveEngineering.proxy.clearConnectionModelCache();
			return;
		}
		int dimension = w.provider.getDimension();
		if(!directConnections.containsKey(dimension))
			return;
		if(start==null)
		{
			for(BlockPos pos : directConnections.get(dimension).keySet())
			{
				IImmersiveConnectable iic = toIIC(pos, w);
				if(iic!=null)
				{
					iic.onConnectivityUpdate(pos, dimension);
				}
			}
			return;
		}
		Map<BlockPos, Set<Connection>> connsForDim = directConnections.get(dimension);
		Set<BlockPos> open = new HashSet<>();
		open.add(start);
		Set<BlockPos> closed = new HashSet<>();
		while(!open.isEmpty())
		{
			Iterator<BlockPos> it = open.iterator();
			BlockPos next = it.next();
			it.remove();
			closed.add(next);
			IImmersiveConnectable iic = toIIC(next, w);
			if(iic!=null)
				iic.onConnectivityUpdate(next, dimension);
			Set<Connection> connsAtBlock = connsForDim.get(next);
			if(connsAtBlock!=null)
				for(Connection c : connsAtBlock)
					if(!closed.contains(c.end))
						open.add(c.end);
		}
	}

	public void removeConnectionAndDrop(Connection conn, World world, @Nullable BlockPos dropPos)
	{
		removeConnection(world, conn);
		if(dropPos!=null)
		{
			double dx = dropPos.getX()+.5;
			double dy = dropPos.getY()+.5;
			double dz = dropPos.getZ()+.5;
			if(world.getGameRules().getBoolean("doTileDrops"))
				world.spawnEntity(new EntityItem(world, dx, dy, dz, conn.cableType.getWireCoil(conn)));
		}
	}

	public void clearAllConnectionsFor(BlockPos node, World world, boolean doDrops)
	{
		clearAllConnectionsFor(node, world, null, doDrops);
	}

	/**
	 * Clears all connections to and from this node.
	 */
	public void clearAllConnectionsFor(BlockPos node, World world, @Nullable IImmersiveConnectable iic, boolean doDrops)
	{
		if(iic==null)
			iic = toIIC(node, world);
		if(iic!=null)
			iic.removeCable(null);

		if(getMultimap(world.provider.getDimension()).containsKey(node))
		{
			for(Connection con : getMultimap(world.provider.getDimension()).get(node))
			{
				removeConnection(world, con, iic!=null?iic.getConnectionOffset(con): Vec3d.ZERO,
						getVecForIICAt(world, con.end, con));
				double dx = node.getX()+.5+Math.signum(con.end.getX()-con.start.getX());
				double dy = node.getY()+.5+Math.signum(con.end.getY()-con.start.getY());
				double dz = node.getZ()+.5+Math.signum(con.end.getZ()-con.start.getZ());
				if(doDrops&&world.getGameRules().getBoolean("doTileDrops"))
					world.spawnEntity(new EntityItem(world, dx, dy, dz, con.cableType.getWireCoil(con)));
			}
		}
		IESaveData.setDirty(world.provider.getDimension());
	}


	public void setProxy(DimensionBlockPos pos, IICProxy p)
	{
		if(p==null)
			proxies.remove(pos);
		else
			proxies.put(pos, p);
	}

	public void addProxy(IICProxy p)
	{
		if(p==null)
			return;
		setProxy(new DimensionBlockPos(p.getPos(), p.getDimension()), p);
	}

	/**
	 * Clears all connections to and from this node.
	 */
	public boolean clearAllConnectionsFor(BlockPos node, World world, @Nonnull TargetingInfo target)
	{
		IImmersiveConnectable iic = toIIC(node, world);
		WireType type = iic.getCableLimiter(target);
		if(type==null)
			return false;
		int dim = world.provider.getDimension();
		resetCachedIndirectConnections(world, node);
		boolean ret = false;
		for(Connection con : getMultimap(world.provider.getDimension()).get(node))
		{
			if(con.cableType==type)
			{
				removeConnection(world, con);
				double dx = node.getX()+.5+Math.signum(con.end.getX()-con.start.getX());
				double dy = node.getY()+.5+Math.signum(con.end.getY()-con.start.getY());
				double dz = node.getZ()+.5+Math.signum(con.end.getZ()-con.start.getZ());
				if(world.getGameRules().getBoolean("doTileDrops"))
					world.spawnEntity(new EntityItem(world, dx, dy, dz, con.cableType.getWireCoil(con)));
				ret = true;
			}
		}
		if(world.isBlockLoaded(node))
			world.addBlockEvent(node, world.getBlockState(node).getBlock(), -1, 0);

		IESaveData.setDirty(dim);
		return ret;
	}

	/*
	public static List<IImmersiveConnectable> getValidEnergyOutputs(BlockPos node, World world)
	{
		List<IImmersiveConnectable> openList = new ArrayList<IImmersiveConnectable>();
		List<IImmersiveConnectable> closedList = new ArrayList<IImmersiveConnectable>();
		List<BlockPos> checked = new ArrayList<BlockPos>();
		HashMap<BlockPos,BlockPos> backtracker = new HashMap<BlockPos,BlockPos>();

		checked.add(node);
		for(Connection con : getConnections(world, node))
			if(toIIC(con.end, world)!=null)
			{
				openList.add(toIIC(con.end, world));
				backtracker.put(con.end, node);
			}

		IImmersiveConnectable next = null;
		final int closedListMax = 1200;

		while(closedList.size()<closedListMax && !openList.isEmpty())
		{
			next = openList.get(0);
			if(!checked.contains(toCC(next)))
			{
				if(next.isEnergyOutput())
				{
					BlockPos last = toCC(next);
					WireType averageType = null;
					int distance = 0;
					List<Connection> connectionParts = new ArrayList<Connection>();
					while(last!=null)
					{
						BlockPos prev = last;
						last = backtracker.get(last);
						if(last!=null)
						{
							for(Connection conB : getConnections(world, prev))
								if(conB.end.equals(toCC(last)))
								{
									connectionParts.add(conB);
									distance += conB.length;
									if(averageType==null || averageType.ordinal()>conB.cableType.ordinal())
										averageType = conB.cableType;
									break;
								}
						}
					}
					closedList.add(next);
				}

				for(Connection con : getConnections(world, toCC(next)))
					if(toIIC(con.end, world)!=null && !checked.contains(con.end) && !closedList.contains(toIIC(con.end, world)) && !openList.contains(toIIC(con.end, world)))
					{
						openList.add(toIIC(con.end, world));
						backtracker.put(con.end, toCC(next));
					}
				checked.add(toCC(next));
			}
			openList.remove(0);
		}

		return closedList;
	}
	 */

	public Set<AbstractConnection> getIndirectEnergyConnections(BlockPos node, World world)
	{
		return getIndirectEnergyConnections(node, world, false);
	}

	public Set<AbstractConnection> getIndirectEnergyConnections(BlockPos node, World world, boolean ignoreIsEnergyOutput)
	{
		int dimension = world.provider.getDimension();
		if(!ignoreIsEnergyOutput&&indirectConnections.containsKey(dimension)&&
				indirectConnections.get(dimension).containsKey(node))
			return indirectConnections.get(dimension).get(node);
		else if(ignoreIsEnergyOutput&&indirectConnectionsIgnoreOut.containsKey(dimension)&&
				indirectConnectionsIgnoreOut.get(dimension).containsKey(node))
			return indirectConnectionsIgnoreOut.get(dimension).get(node);

		PriorityQueue<Pair<IImmersiveConnectable, Float>> queue = new PriorityQueue<>(Comparator.comparingDouble(Pair::getRight));
		Set<AbstractConnection> closedList = newSetFromMap(new ConcurrentHashMap<AbstractConnection, Boolean>());
		List<BlockPos> checked = new ArrayList<>();
		HashMap<BlockPos, BlockPos> backtracker = new HashMap<>();

		checked.add(node);
		Set<Connection> conL = getConnections(world, node);
		if(conL!=null)
			for(Connection con : conL)
			{
				IImmersiveConnectable end = toIIC(con.end, world);
				if(end!=null)
				{
					queue.add(new ImmutablePair<>(end, con.getBaseLoss()));
					backtracker.put(con.end, node);
				}
			}

		IImmersiveConnectable next;
		final int closedListMax = 1200;

		while(closedList.size() < closedListMax&&!queue.isEmpty())
		{
			Pair<IImmersiveConnectable, Float> pair = queue.poll();
			next = pair.getLeft();
			float loss = pair.getRight();
			BlockPos nextPos = toBlockPos(next);
			if(!checked.contains(nextPos)&&queue.stream().noneMatch((p) -> p.getLeft().equals(nextPos)))
			{
				boolean isOutput = next.isEnergyOutput();
				if(ignoreIsEnergyOutput||isOutput)
				{
					BlockPos last = toBlockPos(next);
					WireType minimumType = null;
					int distance = 0;
					List<Connection> connectionParts = new ArrayList<>();
					while(last!=null)
					{
						BlockPos prev = last;
						last = backtracker.get(last);
						if(last!=null)
						{

							Set<Connection> conLB = getConnections(world, last);
							if(conLB!=null)
								for(Connection conB : conLB)
									if(conB.end.equals(prev))
									{
										connectionParts.add(0, conB);
										distance += conB.length;
										if(minimumType==null||conB.cableType.getTransferRate() < minimumType.getTransferRate())
											minimumType = conB.cableType;
										break;
									}
						}
					}
					closedList.add(new AbstractConnection(toBlockPos(node), toBlockPos(next), minimumType, distance, isOutput, connectionParts.toArray(new Connection[connectionParts.size()])));
				}

				Set<Connection> conLN = getConnections(world, toBlockPos(next));
				if(conLN!=null)
					for(Connection con : conLN)
						if(next.allowEnergyToPass(con))
						{
							IImmersiveConnectable end = toIIC(con.end, world);

							Optional<Pair<IImmersiveConnectable, Float>> existing =
									queue.stream().filter((p) -> p.getLeft()==end).findAny();
							float newLoss = con.getBaseLoss()+loss;
							if(end!=null&&!checked.contains(con.end)&&existing.map(Pair::getRight).orElse(Float.MAX_VALUE) > newLoss)
							{
								existing.ifPresent(p1 -> queue.removeIf((p2) -> p1.getLeft()==p2.getLeft()));
								queue.add(new ImmutablePair<>(end, newLoss));
								backtracker.put(con.end, toBlockPos(next));
							}
						}
				checked.add(toBlockPos(next));
			}
		}
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			if(ignoreIsEnergyOutput)
			{
				if(!indirectConnectionsIgnoreOut.containsKey(dimension))
					indirectConnectionsIgnoreOut.put(dimension, new ConcurrentHashMap<>());
				Map<BlockPos, Set<AbstractConnection>> conns = indirectConnectionsIgnoreOut.get(dimension);
				if(!conns.containsKey(node))
					conns.put(node, newSetFromMap(new ConcurrentHashMap<>()));
				conns.get(node).addAll(closedList);
			}
			else
			{
				if(!indirectConnections.containsKey(dimension))
					indirectConnections.put(dimension, new ConcurrentHashMap<>());
				Map<BlockPos, Set<AbstractConnection>> conns = indirectConnections.get(dimension);
				if(!conns.containsKey(node))
					conns.put(node, newSetFromMap(new ConcurrentHashMap<>()));
				conns.get(node).addAll(closedList);
			}
		}
		return closedList;
	}

	//Called through ASM/coremod
	@SuppressWarnings("unused")
	public static void handleEntityCollision(BlockPos p, Entity e)
	{
		if(!e.world.isRemote&&IEConfig.enableWireDamage&&e instanceof EntityLivingBase&&
				!e.isEntityInvulnerable(IEDamageSources.wireShock)&&
				!(e instanceof EntityPlayer&&((EntityPlayer)e).capabilities.disableDamage))
		{
			Map<BlockPos, BlockWireInfo> mapForDim = INSTANCE.blockWireMap.lookup(e.dimension);
			if(mapForDim!=null)
			{
				BlockWireInfo info = mapForDim.get(p);
				if(info!=null)
				{
					handleMapForDamage(info.in, (EntityLivingBase)e, p);
					handleMapForDamage(info.near, (EntityLivingBase)e, p);
				}
			}
		}
	}

	private static void handleMapForDamage(Set<Triple<Connection, Vec3d, Vec3d>> in, EntityLivingBase e, BlockPos here)
	{
		final double KNOCKBACK_PER_DAMAGE = 10;
		if(!in.isEmpty())
		{
			AxisAlignedBB eAabb = e.getEntityBoundingBox();
			for(Triple<Connection, Vec3d, Vec3d> conn : in)
				if(conn.getLeft().cableType.canCauseDamage())
				{
					double extra = conn.getLeft().cableType.getDamageRadius();
					AxisAlignedBB includingExtra = eAabb.grow(extra).offset(-here.getX(), -here.getY(), -here.getZ());
					boolean endpointsInEntity = includingExtra.contains(conn.getMiddle())||
							includingExtra.contains(conn.getRight());
					RayTraceResult rayRes = endpointsInEntity?null: includingExtra.calculateIntercept(conn.getMiddle(), conn.getRight());
					if(endpointsInEntity||(rayRes!=null&&rayRes.typeOfHit==RayTraceResult.Type.BLOCK))
					{
						IImmersiveConnectable iic = toIIC(conn.getLeft().start, e.world);
						float damage = 0;
						if(iic!=null)
							damage = iic.getDamageAmount(e, conn.getLeft());
						if(damage==0)
						{
							iic = toIIC(conn.getLeft().end, e.world);
							if(iic!=null)
								damage = iic.getDamageAmount(e, conn.getLeft());
						}
						if(damage!=0)
						{
							IEDamageSources.ElectricDamageSource dmg = IEDamageSources.causeWireDamage(damage, conn.getLeft().cableType.getElectricSource());
							if(dmg.apply(e))
							{
								damage = dmg.dmg;
								Vec3d v = e.getLookVec();
								knockbackNoSource(e, damage/KNOCKBACK_PER_DAMAGE, v.x, v.z);
								iic.processDamage(e, damage, conn.getLeft());
							}
						}
					}
				}
		}
	}

	public Connection getReverseConnection(int world, Connection ret)
	{
		return getConnections(world, ret.end).stream().filter(ret::hasSameConnectors).findAny().orElse(null);
	}

	public static class Connection implements Comparable<Connection>
	{
		public BlockPos start;
		public BlockPos end;
		public WireType cableType;
		public int length;
		public Vec3d[] catenaryVertices;
		public static final int vertices = 17;
		public boolean vertical = false;
		/**
		 * Used to calculate the catenary vertices:
		 * Y = a * cosh((( X-offsetX)/a)+offsetY
		 * (Y relative to start. X linear&horizontal, from 0 to horizontal length)
		 * set in getSubVertices
		 */
		public double catOffsetX;
		public double catOffsetY;
		public double catA;
		/*
		Stores the horizontal length for non-vertical connections and the vertical length for vertical connections.
		TODO rename
		 */
		public double horizontalLength;
		public Vec3d across = null;

		public Connection(BlockPos start, BlockPos end, WireType cableType, int length)
		{
			this.start = start;
			this.end = end;
			this.cableType = cableType;
			this.length = length;
		}

		public boolean hasSameConnectors(Connection con)
		{
			boolean n0 = start.equals(con.start)&&end.equals(con.end);
			boolean n1 = start.equals(con.end)&&end.equals(con.start);
			return n0||n1;
		}

		public Vec3d[] getSubVertices(Vec3d start, Vec3d end)
		{
			if(catenaryVertices==null)
			{
				catenaryVertices = getConnectionCatenary(this, start, end);
				vertical = start.x==end.x&&start.z==end.z;
			}
			return catenaryVertices;
		}

		public Vec3d[] getSubVertices(World world)
		{
			return getSubVertices(getVecForIICAt(world, start, this),
					getVecForIICAt(world, end, this));
		}

		@Deprecated
		public Vec3d getVecAt(double pos, Vec3d vStart, Vec3d across, double lengthHor)
		{
			getSubVertices(vStart, vStart.add(across));
			return getVecAt(pos);
		}

		public Vec3d getVecAt(double pos)
		{
			pos = MathHelper.clamp(pos, 0, 1);
			if(vertical)
				return catenaryVertices[0].add(across.scale(pos));
			else
				return catenaryVertices[0].add(pos*across.x,
						catA*Math.cosh((pos*horizontalLength-catOffsetX)/catA)+catOffsetY,
						pos*across.z);
		}

		//Slope per block
		public double getSlopeAt(double pos)
		{
			pos = MathHelper.clamp(pos, 0, 1);
			if(vertical)
				return Double.POSITIVE_INFINITY;
			else
				return Math.sinh((pos*horizontalLength-catOffsetX)/catA);
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			if(start!=null)
				tag.setIntArray("start", new int[]{start.getX(), start.getY(), start.getZ()});
			if(end!=null)
				tag.setIntArray("end", new int[]{end.getX(), end.getY(), end.getZ()});
			tag.setString("cableType", cableType.getUniqueName());
			tag.setInteger("length", length);
			return tag;
		}

		public static Connection readFromNBT(NBTTagCompound tag)
		{
			if(tag==null)
				return null;
			int[] iStart = tag.getIntArray("start");
			BlockPos start = new BlockPos(iStart[0], iStart[1], iStart[2]);

			int[] iEnd = tag.getIntArray("end");
			BlockPos end = new BlockPos(iEnd[0], iEnd[1], iEnd[2]);

			WireType type = ApiUtils.getWireTypeFromNBT(tag, "cableType");

			if(start!=null&&end!=null&&type!=null)
				return new Connection(start, end, type, tag.getInteger("length"));
			return null;
		}

		@Override
		public int compareTo(@Nonnull Connection o)
		{
			if(this==o)
				return 0;
			int distComp = Integer.compare(length, o.length);
			int cableComp = -1*Integer.compare(cableType.getTransferRate(), o.cableType.getTransferRate());
			if(cableComp!=0)
				return cableComp;
			if(distComp!=0)
				return distComp;
			if(start.getX()!=o.start.getX())
				return start.getX() > o.start.getX()?1: -1;
			if(start.getY()!=o.start.getY())
				return start.getY() > o.start.getY()?1: -1;
			if(start.getZ()!=o.start.getZ())
				return start.getZ() > o.start.getZ()?1: -1;
			if(end.getX()!=o.end.getX())
				return end.getX() > o.end.getX()?1: -1;
			if(end.getY()!=o.end.getY())
				return end.getY() > o.end.getY()?1: -1;
			if(end.getZ()!=o.end.getZ())
				return end.getZ() > o.end.getZ()?1: -1;
			return 0;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof Connection))
				return false;
			return compareTo((Connection)obj)==0;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(start, end, cableType);
		}

		public float getBaseLoss()
		{
			return getBaseLoss(0);
		}

		public float getBaseLoss(float mod)
		{
			float lengthRelative = this.length/(float)cableType.getMaxLength();
			return (float)(lengthRelative*cableType.getLossRatio()*(1+mod));
		}
	}

	public static class AbstractConnection extends Connection
	{
		public Connection[] subConnections;
		public boolean isEnergyOutput;
		private float avgLoss = -1;

		public AbstractConnection(BlockPos start, BlockPos end, WireType cableType, int length, Connection... subConnections)
		{
			this(start, end, cableType, length, true, subConnections);
		}

		public AbstractConnection(BlockPos start, BlockPos end, WireType cableType, int length, boolean output, Connection... subConnections)
		{
			super(start, end, cableType, length);
			this.isEnergyOutput = output;
			this.subConnections = subConnections;
		}

		public float getPreciseLossRate(int energyInput, int connectorMaxInput)
		{
			float f = 0;
			for(Connection c : subConnections)
			{
				float mod = (((connectorMaxInput-energyInput)/(float)connectorMaxInput)/.25f)*.1f;
				f += c.getBaseLoss(mod);
			}
			return Math.min(f, 1);
		}

		public float getAverageLossRate()
		{
			if(avgLoss < 0)
			{
				float f = 0;
				for(Connection c : subConnections)
				{
					f += c.getBaseLoss();
				}
				avgLoss = Math.min(f, 1);
			}
			return avgLoss;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			if(!super.equals(o)) return false;
			AbstractConnection that = (AbstractConnection)o;
			return Arrays.equals(subConnections, that.subConnections);
		}

		@Override
		public int hashCode()
		{
			int result = super.hashCode();
			result = 31*result+Arrays.hashCode(subConnections);
			return result;
		}

		@Override
		public int compareTo(@Nonnull Connection other)
		{
			if(!(other instanceof AbstractConnection))
				return -other.compareTo(this);
			AbstractConnection otherAbstract = (AbstractConnection)other;
			if(getAverageLossRate()!=otherAbstract.getAverageLossRate())
				return Double.compare(getAverageLossRate(), otherAbstract.getAverageLossRate());
			return super.compareTo(other);
		}
	}

	public class BlockWireInfo
	{
		@Nonnull
		public final Set<Triple<Connection, Vec3d, Vec3d>> in = Collections.newSetFromMap(new ConcurrentHashMap<>());
		@Nonnull
		public final Set<Triple<Connection, Vec3d, Vec3d>> near = Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	public class BlockPlaceListener implements IWorldEventListener
	{
		@Override
		public void notifyBlockUpdate(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState, int flags)
		{
			Map<BlockPos, BlockWireInfo> worldMap = blockWireMap.lookup(worldIn.provider.getDimension());
			if(worldMap!=null&&!worldIn.isRemote&&(flags&1)!=0&&newState.getBlock().canCollideCheck(newState, false))
			{
				BlockWireInfo info = worldMap.get(pos);
				if(info!=null)
				{
					Set<Triple<Connection, Vec3d, Vec3d>> conns = info.in;
					Set<Pair<Connection, BlockPos>> toBreak = new HashSet<>();
					for(Triple<Connection, Vec3d, Vec3d> conn : conns)
					{
						Vec3d[] verts = conn.getLeft().getSubVertices(worldIn);
						if(!Utils.isVecInBlock(verts[0], pos, conn.getLeft().start)&&
								!Utils.isVecInBlock(verts[verts.length-1], pos, conn.getLeft().start))
						{
							BlockPos dropPos = pos;
							if(ApiUtils.preventsConnection(worldIn, pos, newState, conn.getMiddle(), conn.getRight()))
							{
								for(EnumFacing f : EnumFacing.VALUES)
									if(worldIn.isAirBlock(pos.offset(f)))
									{
										dropPos = dropPos.offset(f);
										break;
									}
								toBreak.add(new ImmutablePair<>(conn.getLeft(), dropPos));
							}
						}
					}
					for(Pair<Connection, BlockPos> b : toBreak)
						removeConnectionAndDrop(b.getLeft(), worldIn, b.getRight());
				}
			}
		}

		@Override
		public void notifyLightSet(@Nonnull BlockPos pos)
		{
		}

		@Override
		public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
		{
		}

		@Override
		public void playSoundToAllNearExcept(EntityPlayer player, @Nonnull SoundEvent soundIn, @Nonnull SoundCategory category, double x, double y, double z, float volume, float pitch)
		{
		}

		@Override
		public void playRecord(@Nonnull SoundEvent soundIn, @Nonnull BlockPos pos)
		{
		}

		@Override
		public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, @Nonnull int... parameters)
		{
		}

		@Override
		public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, @Nonnull int... parameters)
		{
		}

		@Override
		public void onEntityAdded(@Nonnull Entity entityIn)
		{
		}

		@Override
		public void onEntityRemoved(@Nonnull Entity entityIn)
		{
		}

		@Override
		public void broadcastSound(int soundID, @Nonnull BlockPos pos, int data)
		{
		}

		@Override
		public void playEvent(EntityPlayer player, int type, @Nonnull BlockPos blockPosIn, int data)
		{
		}

		@Override
		public void sendBlockBreakProgress(int breakerId, @Nonnull BlockPos pos, int progress)
		{
		}
	}
}