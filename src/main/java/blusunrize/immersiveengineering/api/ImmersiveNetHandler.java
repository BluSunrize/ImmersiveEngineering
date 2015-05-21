package blusunrize.immersiveengineering.api;

import static blusunrize.immersiveengineering.api.ApiUtils.toCC;
import static blusunrize.immersiveengineering.api.ApiUtils.toIIC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IESaveData;

import com.google.common.collect.ArrayListMultimap;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class ImmersiveNetHandler
{
	public static ImmersiveNetHandler INSTANCE;
	public HashMap<Integer, ArrayListMultimap<ChunkCoordinates,Connection>> directConnections = new HashMap<Integer, ArrayListMultimap<ChunkCoordinates,Connection>>();
	public ArrayListMultimap<ChunkCoordinates, AbstractConnection> indirectConnections = ArrayListMultimap.create();

	private ArrayListMultimap<ChunkCoordinates,Connection> getMultimap(int dimension)
	{
		if(directConnections.get(dimension)==null)
		{
			ArrayListMultimap<ChunkCoordinates,Connection> mm = ArrayListMultimap.create();
			directConnections.put(dimension, mm);
		}
		return directConnections.get(dimension);
	}

	public void addConnection(World world, ChunkCoordinates node, ChunkCoordinates connection, int distance, WireType cableType)
	{
		getMultimap(world.provider.dimensionId).get(node).add(new Connection(node, connection, cableType, distance));
		getMultimap(world.provider.dimensionId).get(connection).add(new Connection(connection, node, cableType, distance));
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
		IESaveData.setDirty(world.provider.dimensionId);
	}
	public void addConnection(World world, ChunkCoordinates node, Connection con)
	{
		getMultimap(world.provider.dimensionId).put(node, con);
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
		IESaveData.setDirty(world.provider.dimensionId);
	}
	public Set<Integer> getRelevantDimensions()
	{
		return directConnections.keySet();
	}
	public Collection<Connection> getAllConnections(World world)
	{
		return getMultimap(world.provider.dimensionId).values();
	}
	public List<Connection> getConnections(World world, ChunkCoordinates node)
	{
		return getMultimap(world.provider.dimensionId).get(node);
	}
	public void clearAllConnections(World world)
	{
		getMultimap(world.provider.dimensionId).clear();
	}
	public void clearConnectionsOriginatingFrom(ChunkCoordinates node, World world)
	{
		getMultimap(world.provider.dimensionId).removeAll(node);
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.clear();
	}

	/**
	 * Clears all connections to and from this node.
	 */
	public void clearAllConnectionsFor(ChunkCoordinates node, World world)
	{
		getMultimap(world.provider.dimensionId).removeAll(node);
		IImmersiveConnectable iic = toIIC(node, world);
		if(iic!=null)
			iic.removeCable(null);
		Iterator<Connection> it = getMultimap(world.provider.dimensionId).values().iterator();
		while(it.hasNext())
		{
			Connection con = it.next();
			if(node.equals(con.start) || node.equals(con.end))
			{
				it.remove();
				//if(node.equals(con.start) && toIIC(con.end, world)!=null && getConnections(world,con.end).isEmpty())
				iic = toIIC(con.end, world);
				if(iic!=null)
					iic.removeCable(con.cableType);
				//if(node.equals(con.end) && toIIC(con.start, world)!=null && getConnections(world,con.start).isEmpty())
				iic = toIIC(con.start, world);
				if(iic!=null)
					iic.removeCable(con.cableType);

				if(node.equals(con.end))
				{
					double dx = node.posX+.5+Math.signum(con.start.posX-con.end.posX);
					double dy = node.posY+.5+Math.signum(con.start.posY-con.end.posY);
					double dz = node.posZ+.5+Math.signum(con.start.posZ-con.end.posZ);
					world.spawnEntityInWorld(new EntityItem(world, dx,dy,dz, new ItemStack(IEContent.itemWireCoil,1,con.cableType.ordinal())));
				}
			}
		}
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
		Iterator<Connection> it = getMultimap(world.provider.dimensionId).values().iterator();
		while(it.hasNext())
		{
			Connection con = it.next();
			if(con.cableType==type)
				if(node.equals(con.start) || node.equals(con.end))
				{
					it.remove();
					toIIC(con.end, world).removeCable(con.cableType);
					toIIC(con.start, world).removeCable(con.cableType);

					if(node.equals(con.end))
					{
						double dx = node.posX+.5+Math.signum(con.start.posX-con.end.posX);
						double dy = node.posY+.5+Math.signum(con.start.posY-con.end.posY);
						double dz = node.posZ+.5+Math.signum(con.start.posZ-con.end.posZ);
						world.spawnEntityInWorld(new EntityItem(world, dx,dy,dz, new ItemStack(IEContent.itemWireCoil,1,con.cableType.ordinal())));
					}
				}
		}
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
	public List<AbstractConnection> getIndirectEnergyConnections(ChunkCoordinates node, World world)
	{
		if(indirectConnections.containsKey(node))
			return indirectConnections.get(node);

		List<IImmersiveConnectable> openList = new ArrayList<IImmersiveConnectable>();
		List<AbstractConnection> closedList = new ArrayList<AbstractConnection>();
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
								if(conB.end.equals(last))
								{
									connectionParts.add(conB);
									distance += conB.length;
									if(averageType==null || averageType.ordinal()>conB.cableType.ordinal())
										averageType = conB.cableType;
									break;
								}
						}
					}
					closedList.add(new AbstractConnection(toCC(node), toCC(next), averageType, distance, connectionParts.toArray(new Connection[0])));
				}

				for(Connection con : getConnections(world, toCC(next)))
					if(toIIC(con.end, world)!=null && !checked.contains(con.end) && !openList.contains(toIIC(con.end, world)))
					{
						openList.add(toIIC(con.end, world));
						backtracker.put(con.end, toCC(next));
					}
				checked.add(toCC(next));
			}
			openList.remove(0);
		}
		Collections.sort(closedList);
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			indirectConnections.putAll(node, closedList);
		return closedList;
	}

	public static class Connection
	{
		public ChunkCoordinates start;
		public ChunkCoordinates end;
		public WireType cableType;
		public int length;

		public Connection(ChunkCoordinates start, ChunkCoordinates end, WireType cableType, int length)
		{
			this.start=start;
			this.end=end;
			this.cableType=cableType;
			this.length=length;
		}

		@Override
		public boolean equals(Object o)
		{
			if(!(o instanceof Connection))
				return false;
			Connection con = (Connection)o;
			boolean n0 = end.equals(con.start)||start.equals(con.end);
			boolean n1 = end.equals(con.start)||start.equals(con.end);
			return n0&&n1;
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			if(start!=null)
				tag.setIntArray("start", new int[]{start.posX,start.posY,start.posZ});
			if(end!=null)
				tag.setIntArray("end", new int[]{end.posX,end.posY,end.posZ});
			tag.setInteger("cableType", cableType.ordinal());
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

			if(start!=null && end!=null)
				return new Connection(start,end, WireType.getValue(tag.getInteger("cableType")), tag.getInteger("length"));
			return null;
		}
	}

	public static class AbstractConnection extends Connection implements Comparable<AbstractConnection>
	{
		public Connection[] subConnections;
		public AbstractConnection(ChunkCoordinates start, ChunkCoordinates end, WireType cableType, int length, Connection... subConnections)
		{
			super(start,end,cableType,length);
			this.subConnections=subConnections;
		}
		@Override
		public int compareTo(AbstractConnection con)
		{
			int distComp = Integer.compare(length, con.length);
			int cableComp = -1*Integer.compare(cableType.ordinal(), con.cableType.ordinal());
			if(distComp==0)
				return cableComp;
			return distComp;
		}

		public float getAverageLossRate()
		{
			float f = 0;
			for(Connection c : subConnections)
				f += (c.length/(float)c.cableType.getMaxLength())*c.cableType.getLossRatio();
			return f;
		}
	}
}