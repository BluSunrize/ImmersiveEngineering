package blusunrize.immersiveengineering.api.energy;

import static blusunrize.immersiveengineering.api.ApiUtils.addVectors;
import static blusunrize.immersiveengineering.api.ApiUtils.getConnectionCatenary;
import static blusunrize.immersiveengineering.api.ApiUtils.toCC;
import static blusunrize.immersiveengineering.api.ApiUtils.toIIC;
import static java.util.Collections.newSetFromMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.IESaveData;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class ImmersiveNetHandler
{
	public static ImmersiveNetHandler INSTANCE;
	public ConcurrentHashMap<Integer, ConcurrentHashMap<ChunkCoordinates, Set<Connection>>> directConnections = new ConcurrentHashMap<Integer, ConcurrentHashMap<ChunkCoordinates, Set<Connection>>>();
	public ConcurrentHashMap<ChunkCoordinates, Set<AbstractConnection>> indirectConnections = new ConcurrentHashMap<ChunkCoordinates, Set<AbstractConnection>>();
	public HashMap<Integer, HashMap<Connection, Integer>> transferPerTick = new HashMap<Integer, HashMap<Connection,Integer>>();

	private ConcurrentHashMap<ChunkCoordinates, Set<Connection>> getMultimap(int dimension)
	{
		if (directConnections.get(dimension) == null)
		{
			ConcurrentHashMap<ChunkCoordinates, Set<Connection>> mm = new ConcurrentHashMap<ChunkCoordinates, Set<Connection>>();
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

	public void addConnection(World world, ChunkCoordinates node, ChunkCoordinates connection, int distance, WireType cableType)
	{
		if(!getMultimap(world.provider.dimensionId).containsKey(node))
			getMultimap(world.provider.dimensionId).put(node, newSetFromMap(new ConcurrentHashMap<Connection, Boolean>()));
		getMultimap(world.provider.dimensionId).get(node).add(new Connection(node, connection, cableType, distance));
		if(!getMultimap(world.provider.dimensionId).containsKey(connection))
			getMultimap(world.provider.dimensionId).put(connection, newSetFromMap(new ConcurrentHashMap<Connection, Boolean>()));
		getMultimap(world.provider.dimensionId).get(connection).add(new Connection(connection, node, cableType, distance));
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
		if(world.blockExists(node.posX,node.posY,node.posZ))
			world.addBlockEvent(node.posX, node.posY, node.posZ, world.getBlock(node.posX,node.posY,node.posZ),-1,0);
		if(world.blockExists(connection.posX,connection.posY,connection.posZ))
			world.addBlockEvent(connection.posX, connection.posY, connection.posZ, world.getBlock(connection.posX,connection.posY,connection.posZ),-1,0);
		IESaveData.setDirty(world.provider.dimensionId);
	}
	public void addConnection(World world, ChunkCoordinates node, Connection con)
	{
		if(!getMultimap(world.provider.dimensionId).containsKey(node))
			getMultimap(world.provider.dimensionId).put(node, newSetFromMap(new ConcurrentHashMap<Connection, Boolean>()));
		getMultimap(world.provider.dimensionId).get(node).add(con);
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
		IESaveData.setDirty(world.provider.dimensionId);
	}
	public void addConnection(int world, ChunkCoordinates node, Connection con)
	{
		if(!getMultimap(world).containsKey(node))
			getMultimap(world).put(node, newSetFromMap(new ConcurrentHashMap<Connection, Boolean>()));
		getMultimap(world).get(node).add(con);
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
		IESaveData.setDirty(world);
	}
	
	public void removeConnection(World world, Connection con)
	{
		if(con==null||world==null)
			return;
		for (Set<Connection> conl : getMultimap(world.provider.dimensionId).values())
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

					if(world.blockExists(itCon.start.posX,itCon.start.posY,itCon.start.posZ))
						world.addBlockEvent(itCon.start.posX, itCon.start.posY, itCon.start.posZ, world.getBlock(itCon.start.posX,itCon.start.posY,itCon.start.posZ),-1,0);
					if(world.blockExists(itCon.end.posX,itCon.end.posY,itCon.end.posZ))
						world.addBlockEvent(itCon.end.posX, itCon.end.posY, itCon.end.posZ, world.getBlock(itCon.end.posX,itCon.end.posY,itCon.end.posZ),-1,0);
				}
			}
		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
		IESaveData.setDirty(world.provider.dimensionId);
	}
	public Set<Integer> getRelevantDimensions()
	{
		return directConnections.keySet();//TODO This will break on Java8. Remember to ask cobra >_>
	}
	public Collection<Connection> getAllConnections(World world)
	{
		Set<Connection> ret = newSetFromMap(new ConcurrentHashMap<Connection, Boolean>());
		for (Set<Connection> conlist : getMultimap(world.provider.dimensionId).values())
			ret.addAll(conlist);
		return ret;
	}
	public synchronized Set<Connection> getConnections(World world, ChunkCoordinates node)
	{
		ConcurrentHashMap<ChunkCoordinates, Set<Connection>> map = getMultimap(world.provider.dimensionId);
		if(map.containsKey(node))
			return map.get(node);
		return null;
	}
	public void clearAllConnections(World world)
	{
		getMultimap(world.provider.dimensionId).clear();
	}
	public void clearAllConnections(int world)
	{
		getMultimap(world).clear();
	}
	public void clearConnectionsOriginatingFrom(ChunkCoordinates node, World world)
	{
		if(getMultimap(world.provider.dimensionId).containsKey(node))
			getMultimap(world.provider.dimensionId).get(node).clear();
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
	}

	public void resetCachedIndirectConnections()
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
	}

	/**
	 * Clears all connections to and from this node.
	 */
	public void clearAllConnectionsFor(ChunkCoordinates node, World world)
	{
		if(getMultimap(world.provider.dimensionId).containsKey(node))
			getMultimap(world.provider.dimensionId).get(node).clear();
		IImmersiveConnectable iic = toIIC(node, world);
		if(iic!=null)
			iic.removeCable(null);
		//		ConcurrentSkipListSet<Connection> itlist = new ConcurrentSkipListSet<Connection>();
		//		for (ConcurrentSkipListSet<Connection> conl : getMultimap(world.provider.dimensionId).values())
		//			itlist.addAll(conl);
		//		Iterator<Connection> it = itlist.iterator();

		for (Set<Connection> conl : getMultimap(world.provider.dimensionId).values())
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
						double dx = node.posX+.5+Math.signum(con.start.posX-con.end.posX);
						double dy = node.posY+.5+Math.signum(con.start.posY-con.end.posY);
						double dz = node.posZ+.5+Math.signum(con.start.posZ-con.end.posZ);
						if(world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
							world.spawnEntityInWorld(new EntityItem(world, dx,dy,dz, con.cableType.getWireCoil()));
						if(world.blockExists(con.start.posX,con.start.posY,con.start.posZ))
							world.addBlockEvent(con.start.posX, con.start.posY, con.start.posZ, world.getBlock(con.start.posX,con.start.posY,con.start.posZ),-1,0);
					}
					else
						if(world.blockExists(con.end.posX,con.end.posY,con.end.posZ))
							world.addBlockEvent(con.end.posX, con.end.posY, con.end.posZ, world.getBlock(con.end.posX,con.end.posY,con.end.posZ),-1,0);
				}
			}
		}
		if(world.blockExists(node.posX,node.posY,node.posZ))
			world.addBlockEvent(node.posX, node.posY, node.posZ, world.getBlock(node.posX,node.posY,node.posZ),-1,0);
		IESaveData.setDirty(world.provider.dimensionId);
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
	}

	/**
	 * Clears all connections to and from this node.
	 * The TargetingInfo must not be null!
	 */
	public void clearAllConnectionsFor(ChunkCoordinates node, World world, TargetingInfo target)
	{
		IImmersiveConnectable iic = toIIC(node, world);
		WireType type = target==null?null : iic.getCableLimiter(target);
		if(type==null)
			return;
		for (Set<Connection> conl : getMultimap(world.provider.dimensionId).values())
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
						if (iic!=null)
							iic.removeCable(con);
						if (other!=null)
							other.removeCable(con);
						if(node.equals(con.end))
						{
							double dx = node.posX+.5+Math.signum(con.start.posX-con.end.posX);
							double dy = node.posY+.5+Math.signum(con.start.posY-con.end.posY);
							double dz = node.posZ+.5+Math.signum(con.start.posZ-con.end.posZ);
							if(world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
								world.spawnEntityInWorld(new EntityItem(world, dx,dy,dz, con.cableType.getWireCoil()));
							if(world.blockExists(con.start.posX,con.start.posY,con.start.posZ))
								world.addBlockEvent(con.start.posX, con.start.posY, con.start.posZ, world.getBlock(con.start.posX,con.start.posY,con.start.posZ),-1,0);
						}
						else
							if(world.blockExists(con.end.posX,con.end.posY,con.end.posZ))
									world.addBlockEvent(con.end.posX, con.end.posY, con.end.posZ, world.getBlock(con.end.posX,con.end.posY,con.end.posZ),-1,0);
					}
			}
		}
		if(world.blockExists(node.posX,node.posY,node.posZ))
			world.addBlockEvent(node.posX, node.posY, node.posZ, world.getBlock(node.posX,node.posY,node.posZ),-1,0);

		IESaveData.setDirty(world.provider.dimensionId);
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
	}

	/*
	public static List<IImmersiveConnectable> getValidEnergyOutputs(ChunkCoordinates node, World world)
	{
		List<IImmersiveConnectable> openList = new ArrayList<IImmersiveConnectable>();
		List<IImmersiveConnectable> closedList = new ArrayList<IImmersiveConnectable>();
		List<ChunkCoordinates> checked = new ArrayList<ChunkCoordinates>();
		HashMap<ChunkCoordinates,ChunkCoordinates> backtracker = new HashMap<ChunkCoordinates,ChunkCoordinates>();

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
					ChunkCoordinates last = toCC(next);
					WireType averageType = null;
					int distance = 0;
					List<Connection> connectionParts = new ArrayList<Connection>();
					while(last!=null)
					{
						ChunkCoordinates prev = last;
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
	public Set<AbstractConnection> getIndirectEnergyConnections(ChunkCoordinates node, World world)
	{
		if(indirectConnections.containsKey(node))
			return indirectConnections.get(node);

		List<IImmersiveConnectable> openList = new ArrayList<IImmersiveConnectable>();
		Set<AbstractConnection> closedList = newSetFromMap(new ConcurrentHashMap<AbstractConnection, Boolean>());
		List<ChunkCoordinates> checked = new ArrayList<ChunkCoordinates>();
		HashMap<ChunkCoordinates,ChunkCoordinates> backtracker = new HashMap<ChunkCoordinates,ChunkCoordinates>();

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
			if(!checked.contains(toCC(next)))
			{
				if(next.isEnergyOutput())
				{
					ChunkCoordinates last = toCC(next);
					WireType averageType = null;
					int distance = 0;
					List<Connection> connectionParts = new ArrayList<Connection>();
					while(last!=null)
					{
						ChunkCoordinates prev = last;
						last = backtracker.get(last);
						if(last!=null)
						{

							Set<Connection> conLB = getConnections(world, prev);
							if(conLB!=null)
								for(Connection conB : conLB)
									if(conB.end.equals(last))
									{
										connectionParts.add(conB);
										distance += conB.length;
										if(averageType==null || conB.cableType.getTransferRate()<averageType.getTransferRate())
											averageType = conB.cableType;
										break;
									}
						}
					}
					closedList.add(new AbstractConnection(toCC(node), toCC(next), averageType, distance, connectionParts.toArray(new Connection[connectionParts.size()])));
				}

				Set<Connection> conLN = getConnections(world, toCC(next));
				if(conLN!=null)
					for(Connection con : conLN)
						if(next.allowEnergyToPass(con))
						{
							IImmersiveConnectable end = toIIC(con.end, world);
							if(end!=null && !checked.contains(con.end) && !openList.contains(end))
							{
								openList.add(end);
								backtracker.put(con.end, toCC(next));
							}
						}
				checked.add(toCC(next));
			}
			openList.remove(0);
		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			if(!indirectConnections.containsKey(node))
				indirectConnections.put(node, newSetFromMap(new ConcurrentHashMap<AbstractConnection, Boolean>()));
			indirectConnections.get(node).addAll(closedList);
		}
		return closedList;
	}

	public static class Connection implements Comparable<Connection>
	{
		public ChunkCoordinates start;
		public ChunkCoordinates end;
		public WireType cableType;
		public int length;
		public Vec3[] catenaryVertices;

		public Connection(ChunkCoordinates start, ChunkCoordinates end, WireType cableType, int length)
		{
			this.start=start;
			this.end=end;
			this.cableType=cableType;
			this.length=length;
		}

		public boolean hasSameConnectors(Connection o) {
			if(!(o instanceof Connection))
				return false;
			Connection con = (Connection)o;
			boolean n0 = start.equals(con.start)&&end.equals(con.end);
			boolean n1  =start.equals(con.end)&&end.equals(con.start);
			return n0||n1;
		}

		public Vec3[] getSubVertices(World world)
		{
			if(catenaryVertices==null)
			{
				Vec3 vStart = Vec3.createVectorHelper(start.posX,start.posY,start.posZ);
				Vec3 vEnd = Vec3.createVectorHelper(end.posX, end.posY, end.posZ);
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
				tag.setIntArray("start", new int[]{start.posX,start.posY,start.posZ});
			if(end!=null)
				tag.setIntArray("end", new int[]{end.posX,end.posY,end.posZ});
			tag.setString("cableType", cableType.getUniqueName());
			tag.setInteger("length", length);
			return tag;
		}
		public static Connection readFromNBT(NBTTagCompound tag)
		{
			if(tag==null)
				return null;
			int[] iStart = tag.getIntArray("start");
			ChunkCoordinates start = new ChunkCoordinates(iStart[0],iStart[1],iStart[2]);

			int[] iEnd = tag.getIntArray("end");
			ChunkCoordinates end = new ChunkCoordinates(iEnd[0],iEnd[1],iEnd[2]);

			WireType type = ApiUtils.getWireTypeFromNBT(tag, "cableType");

			if(start!=null && end!=null && type!=null)
				return new Connection(start,end, type, tag.getInteger("length"));
			return null;
		}

		@Override
		public int compareTo(Connection o)
		{
			if (equals(o))
				return 0;
			int distComp = Integer.compare(length, o.length);
			int cableComp = -1*Integer.compare(cableType.getTransferRate(), o.cableType.getTransferRate());
			if(cableComp!=0)
				return cableComp;
			if (distComp!=0)
				return distComp;
			if (start.posX!=o.start.posX)
				return start.posX>o.start.posX?1:-1;
			if (start.posY!=o.start.posY)
				return start.posY>o.start.posY?1:-1;
			if (start.posZ!=o.start.posZ)
				return start.posZ>o.start.posZ?1:-1;
			if (end.posX!=o.end.posX)
				return end.posX>o.end.posX?1:-1;
			if (end.posY!=o.end.posY)
				return end.posY>o.end.posY?1:-1;
			if (end.posZ!=o.end.posZ)
				return end.posZ>o.end.posZ?1:-1;
			return 0;
		}
	}

	public static class AbstractConnection extends Connection
	{
		public Connection[] subConnections;
		public AbstractConnection(ChunkCoordinates start, ChunkCoordinates end, WireType cableType, int length, Connection... subConnections)
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