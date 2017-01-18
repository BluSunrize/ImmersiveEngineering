package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.IESaveData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static blusunrize.immersiveengineering.api.ApiUtils.*;
import static java.util.Collections.newSetFromMap;

public class ImmersiveNetHandler
{
	public static final ImmersiveNetHandler INSTANCE = new ImmersiveNetHandler();
	public Map<Integer, ConcurrentHashMap<BlockPos, Set<Connection>>> directConnections = new ConcurrentHashMap<>();
	public Map<BlockPos, Set<AbstractConnection>> indirectConnections = new ConcurrentHashMap<>();
	public Map<Integer, HashMap<Connection, Integer>> transferPerTick = new HashMap<>();
	public Map<DimensionBlockPos, IICProxy> proxies = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<BlockPos, Set<Connection>> getMultimap(int dimension)
	{
		if (directConnections.get(dimension) == null)
		{
			ConcurrentHashMap<BlockPos, Set<Connection>> mm = new ConcurrentHashMap<BlockPos, Set<Connection>>();
			directConnections.put(dimension, mm);
		}
		return directConnections.get(dimension);
	}
	public HashMap<Connection, Integer> getTransferedRates(int dimension)
	{
		if (!transferPerTick.containsKey(dimension))
			transferPerTick.put(dimension, new HashMap<Connection,Integer>());
		return transferPerTick.get(dimension);
	}

	public void addConnection(World world, BlockPos node, BlockPos connection, int distance, WireType cableType)
	{
		if(!getMultimap(world.provider.getDimension()).containsKey(node))
			getMultimap(world.provider.getDimension()).put(node, newSetFromMap(new ConcurrentHashMap<Connection, Boolean>()));
		getMultimap(world.provider.getDimension()).get(node).add(new Connection(node, connection, cableType, distance));
		if(!getMultimap(world.provider.getDimension()).containsKey(connection))
			getMultimap(world.provider.getDimension()).put(connection, newSetFromMap(new ConcurrentHashMap<Connection, Boolean>()));
		getMultimap(world.provider.getDimension()).get(connection).add(new Connection(connection, node, cableType, distance));
		resetCachedIndirectConnections();
		if(world.isBlockLoaded(node))
			world.addBlockEvent(node, world.getBlockState(node).getBlock(),-1,0);
		if(world.isBlockLoaded(connection))
			world.addBlockEvent(connection, world.getBlockState(connection).getBlock(),-1,0);
		IESaveData.setDirty(world.provider.getDimension());
	}
	public void addConnection(World world, BlockPos node, Connection con)
	{
		if(!getMultimap(world.provider.getDimension()).containsKey(node))
			getMultimap(world.provider.getDimension()).put(node, newSetFromMap(new ConcurrentHashMap<Connection, Boolean>()));
		getMultimap(world.provider.getDimension()).get(node).add(con);
		resetCachedIndirectConnections();
		IESaveData.setDirty(world.provider.getDimension());
	}
	public void addConnection(int world, BlockPos node, Connection con)
	{
		if(!getMultimap(world).containsKey(node))
			getMultimap(world).put(node, newSetFromMap(new ConcurrentHashMap<Connection, Boolean>()));
		getMultimap(world).get(node).add(con);
		resetCachedIndirectConnections();
		IESaveData.setDirty(world);
	}

	public void removeConnection(World world, Connection con)
	{
		if(con==null||world==null)
			return;
		for (Set<Connection> conl : getMultimap(world.provider.getDimension()).values())
		{
			Iterator<Connection> it = conl.iterator();
			while(it.hasNext())
			{
				Connection itCon = it.next();
				if(con.hasSameConnectors(itCon))
				{
					it.remove();
					IImmersiveConnectable iic = toIIC(itCon.end, world);
					if(iic!=null)
						iic.removeCable(itCon);
					iic = toIIC(itCon.start, world);
					if(iic!=null)
						iic.removeCable(itCon);

					if(world.isBlockLoaded(itCon.start))
						world.addBlockEvent(itCon.start, world.getBlockState(itCon.start).getBlock(),-1,0);
					if(world.isBlockLoaded(itCon.end))
						world.addBlockEvent(itCon.end, world.getBlockState(itCon.end).getBlock(),-1,0);
				}
			}
		}
		resetCachedIndirectConnections();
		IESaveData.setDirty(world.provider.getDimension());
	}
	public Set<Integer> getRelevantDimensions()
	{
		return directConnections.keySet();
	}
	public Collection<Connection> getAllConnections(int dimensionId)
	{
		Set<Connection> ret = newSetFromMap(new ConcurrentHashMap<Connection, Boolean>());
		for (Set<Connection> conlist : getMultimap(dimensionId).values())
			ret.addAll(conlist);
		return ret;
	}
	public Collection<Connection> getAllConnections(World world)
	{
		return getAllConnections(world.provider.getDimension());
	}
	public synchronized Set<Connection> getConnections(World world, BlockPos node)
	{
		if(world!=null && world.provider!=null)
		{
			ConcurrentHashMap<BlockPos, Set<Connection>> map = getMultimap(world.provider.getDimension());
			return map.get(node);
		}
		return null;
	}
	public void clearAllConnections(World world)
	{
		getMultimap(world.provider.getDimension()).clear();
	}
	public void clearAllConnections(int world)
	{
		getMultimap(world).clear();
	}
	public void clearConnectionsOriginatingFrom(BlockPos node, World world)
	{
		if(getMultimap(world.provider.getDimension()).containsKey(node))
			getMultimap(world.provider.getDimension()).get(node).clear();
		resetCachedIndirectConnections();
	}

	public void resetCachedIndirectConnections()
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
		else
			ImmersiveEngineering.proxy.clearConnectionModelCache();
	}

	/**
	 * Clears all connections to and from this node.
	 */
	public void clearAllConnectionsFor(BlockPos node, World world, boolean doDrops)
	{
		if(getMultimap(world.provider.getDimension()).containsKey(node))
			getMultimap(world.provider.getDimension()).get(node).clear();
		IImmersiveConnectable iic = toIIC(node, world);
		if(iic!=null)
			iic.removeCable(null);
		//		ConcurrentSkipListSet<Connection> itlist = new ConcurrentSkipListSet<Connection>();
		//		for (ConcurrentSkipListSet<Connection> conl : getMultimap(world.provider.getDimension()).values())
		//			itlist.addAll(conl);
		//		Iterator<Connection> it = itlist.iterator();

		for (Set<Connection> conl : getMultimap(world.provider.getDimension()).values())
		{
			Iterator<Connection> it = conl.iterator();

			while(it.hasNext())
			{
				Connection con = it.next();
				if(node.equals(con.start) || node.equals(con.end))
				{
					it.remove();
					IImmersiveConnectable other;
					if (node.equals(con.start))
						other = toIIC(con.end, world);
					else
						other = toIIC(con.start, world);
					if (iic!=null)
						iic.removeCable(con);
					if (other!=null)
						other.removeCable(con);

					if(node.equals(con.end))
					{
						double dx = node.getX()+.5+Math.signum(con.start.getX()-con.end.getX());
						double dy = node.getY()+.5+Math.signum(con.start.getY()-con.end.getY());
						double dz = node.getZ()+.5+Math.signum(con.start.getZ()-con.end.getZ());
						if(doDrops&&world.getGameRules().getBoolean("doTileDrops"))
							world.spawnEntityInWorld(new EntityItem(world, dx,dy,dz, con.cableType.getWireCoil(con)));
						if(world.isBlockLoaded(con.start))
							world.addBlockEvent(con.start, world.getBlockState(con.start).getBlock(),-1,0);
					}
					else
						if(world.isBlockLoaded(con.end))
							world.addBlockEvent(con.end, world.getBlockState(con.end).getBlock(),-1,0);
				}
			}
		}
		if(world.isBlockLoaded(node))
			world.addBlockEvent(node, world.getBlockState(node).getBlock(),-1,0);
		IESaveData.setDirty(world.provider.getDimension());
		resetCachedIndirectConnections();
	}
	public void setProxy(DimensionBlockPos pos, IICProxy p)
	{
		if (p==null)
			proxies.remove(pos);
		else
			proxies.put(pos, p);
	}
	public void addProxy(IICProxy p)
	{
		if (p==null)
			return;
		setProxy(new DimensionBlockPos(p.getPos(), p.getDimension()), p);
	}

	/**
	 * Clears all connections to and from this node.
	 * The TargetingInfo must not be null!
	 */
	public boolean clearAllConnectionsFor(BlockPos node, World world, TargetingInfo target)
	{
		IImmersiveConnectable iic = toIIC(node, world);
		WireType type = target==null?null : iic.getCableLimiter(target);
		if(type==null)
			return false;
		boolean ret = false;
		for(Set<Connection> conl : getMultimap(world.provider.getDimension()).values())
		{
			Iterator<Connection> it = conl.iterator();
			while(it.hasNext())
			{
				Connection con = it.next();
				if(con.cableType==type)
					if(node.equals(con.start) || node.equals(con.end))
					{
						it.remove();
						IImmersiveConnectable other;
						if (node.equals(con.start))
							other = toIIC(con.end, world);
						else
							other = toIIC(con.start, world);
						if(iic!=null)
							iic.removeCable(con);
						if(other!=null)
							other.removeCable(con);
						if(node.equals(con.end))
						{
							double dx = node.getX()+.5+Math.signum(con.start.getX()-con.end.getX());
							double dy = node.getY()+.5+Math.signum(con.start.getY()-con.end.getY());
							double dz = node.getZ()+.5+Math.signum(con.start.getZ()-con.end.getZ());
							if(world.getGameRules().getBoolean("doTileDrops"))
								world.spawnEntityInWorld(new EntityItem(world, dx,dy,dz, con.cableType.getWireCoil(con)));
							if(world.isBlockLoaded(con.start))
								world.addBlockEvent(con.start, world.getBlockState(con.start).getBlock(),-1,0);
						}
						else
							if(world.isBlockLoaded(con.end))
								world.addBlockEvent(con.end, world.getBlockState(con.end).getBlock(),-1,0);
						ret = true;
					}
			}
		}
		if(world.isBlockLoaded(node))
			world.addBlockEvent(node, world.getBlockState(node).getBlock(),-1,0);

		IESaveData.setDirty(world.provider.getDimension());
		resetCachedIndirectConnections();
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
	/**
	 * return values are cached if and only if ignoreIsEnergyOutput is false
	 */
	public Set<AbstractConnection> getIndirectEnergyConnections(BlockPos node, World world, boolean ignoreIsEnergyOutput)
	{
		if(!ignoreIsEnergyOutput&&indirectConnections.containsKey(node))
			return indirectConnections.get(node);

		List<IImmersiveConnectable> openList = new ArrayList<IImmersiveConnectable>();
		Set<AbstractConnection> closedList = newSetFromMap(new ConcurrentHashMap<AbstractConnection, Boolean>());
		List<BlockPos> checked = new ArrayList<BlockPos>();
		HashMap<BlockPos,BlockPos> backtracker = new HashMap<BlockPos,BlockPos>();

		checked.add(node);
		Set<Connection> conL = getConnections(world, node);
		if(conL!=null)
			for(Connection con : conL)
			{
				IImmersiveConnectable end = toIIC(con.end, world);
				if(end!=null)
				{
					openList.add(end);
					backtracker.put(con.end, node);
				}
			}

		IImmersiveConnectable next = null;
		final int closedListMax = 1200;

		while(closedList.size()<closedListMax && !openList.isEmpty())
		{
			next = openList.get(0);
			if(!checked.contains(toBlockPos(next)))
			{
				if(ignoreIsEnergyOutput||next.isEnergyOutput())
				{
					BlockPos last = toBlockPos(next);
					WireType averageType = null;
					int distance = 0;
					List<Connection> connectionParts = new ArrayList<Connection>();
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
										if(averageType==null || conB.cableType.getTransferRate()<averageType.getTransferRate())
											averageType = conB.cableType;
										break;
									}
						}
					}
					closedList.add(new AbstractConnection(toBlockPos(node), toBlockPos(next), averageType, distance, connectionParts.toArray(new Connection[connectionParts.size()])));
				}

				Set<Connection> conLN = getConnections(world, toBlockPos(next));
				if(conLN!=null)
					for(Connection con : conLN)
						if(next.allowEnergyToPass(con))
						{
							IImmersiveConnectable end = toIIC(con.end, world);
							if(end!=null && !checked.contains(con.end) && !openList.contains(end))
							{
								openList.add(end);
								backtracker.put(con.end, toBlockPos(next));
							}
						}
				checked.add(toBlockPos(next));
			}
			openList.remove(0);
		}
		if(!ignoreIsEnergyOutput&&FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			if(!indirectConnections.containsKey(node))
				indirectConnections.put(node, newSetFromMap(new ConcurrentHashMap<AbstractConnection, Boolean>()));
			indirectConnections.get(node).addAll(closedList);
		}
		return closedList;
	}

	public static class Connection implements Comparable<Connection>
	{
		public BlockPos start;
		public BlockPos end;
		public WireType cableType;
		public int length;
		public Vec3d[] catenaryVertices;
		public static final int vertices = 17;
		
		public Connection(BlockPos start, BlockPos end, WireType cableType, int length)
		{
			this.start=start;
			this.end=end;
			this.cableType=cableType;
			this.length=length;
		}

		public boolean hasSameConnectors(Connection o) {
			if(!(o instanceof Connection))
				return false;
			Connection con = o;
			boolean n0 = start.equals(con.start)&&end.equals(con.end);
			boolean n1  =start.equals(con.end)&&end.equals(con.start);
			return n0||n1;
		}

		public Vec3d[] getSubVertices(World world)
		{
			if(catenaryVertices==null)
			{
				Vec3d vStart = new Vec3d(start.getX(),start.getY(),start.getZ());
				Vec3d vEnd = new Vec3d(end.getX(), end.getY(), end.getZ());
				IImmersiveConnectable iicStart = toIIC(start, world);
				IImmersiveConnectable iicEnd = toIIC(end, world);
				if(iicStart!=null)
					vStart = addVectors(vStart, iicStart.getConnectionOffset(this));
				if(iicEnd!=null)
					vEnd = addVectors(vEnd, iicEnd.getConnectionOffset(this));
				catenaryVertices = getConnectionCatenary(this, vStart, vEnd);
			}
			return catenaryVertices;
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			if(start!=null)
				tag.setIntArray("start", new int[]{start.getX(),start.getY(),start.getZ()});
			if(end!=null)
				tag.setIntArray("end", new int[]{end.getX(),end.getY(),end.getZ()});
			tag.setString("cableType", cableType.getUniqueName());
			tag.setInteger("length", length);
			return tag;
		}
		public static Connection readFromNBT(NBTTagCompound tag)
		{
			if(tag==null)
				return null;
			int[] iStart = tag.getIntArray("start");
			BlockPos start = new BlockPos(iStart[0],iStart[1],iStart[2]);

			int[] iEnd = tag.getIntArray("end");
			BlockPos end = new BlockPos(iEnd[0],iEnd[1],iEnd[2]);

			WireType type = ApiUtils.getWireTypeFromNBT(tag, "cableType");

			if(start!=null && end!=null && type!=null)
				return new Connection(start,end, type, tag.getInteger("length"));
			return null;
		}

		@Override
		public int compareTo(Connection o)
		{
			if(this==o)
				return 0;
			int distComp = Integer.compare(length, o.length);
			int cableComp = -1*Integer.compare(cableType.getTransferRate(), o.cableType.getTransferRate());
			if(cableComp!=0)
				return cableComp;
			if (distComp!=0)
				return distComp;
			if (start.getX()!=o.start.getX())
				return start.getX()>o.start.getX()?1:-1;
				if (start.getY()!=o.start.getY())
					return start.getY()>o.start.getY()?1:-1;
					if (start.getZ()!=o.start.getZ())
						return start.getZ()>o.start.getZ()?1:-1;
						if (end.getX()!=o.end.getX())
							return end.getX()>o.end.getX()?1:-1;
							if (end.getY()!=o.end.getY())
								return end.getY()>o.end.getY()?1:-1;
								if (end.getZ()!=o.end.getZ())
									return end.getZ()>o.end.getZ()?1:-1;
									return 0;
		}
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Connection))
				return false;
			return compareTo((Connection)obj)==0;
		}
	}

	public static class AbstractConnection extends Connection
	{
		public Connection[] subConnections;
		public AbstractConnection(BlockPos start, BlockPos end, WireType cableType, int length, Connection... subConnections)
		{
			super(start,end,cableType,length);
			this.subConnections=subConnections;
		}

		public float getPreciseLossRate(int energyInput, int connectorMaxInput)
		{
			float f = 0;
			for(Connection c : subConnections)
			{
				float length = c.length/(float)c.cableType.getMaxLength();
				float baseLoss = (float)c.cableType.getLossRatio();
				float mod = (((connectorMaxInput-energyInput)/(float)connectorMaxInput)/.25f)*.1f;
				f += length*(baseLoss+baseLoss*mod);
			}
			return Math.min(f,1);
		}

		public float getAverageLossRate()
		{
			float f = 0;
			for(Connection c : subConnections)
			{
				float length = c.length/(float)c.cableType.getMaxLength();
				float baseLoss = (float)c.cableType.getLossRatio();
				f += length*baseLoss;
			}
			return Math.min(f,1);
		}
	}
}