/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires.localhandlers;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.wires.*;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import it.unimi.dsi.fastutil.objects.Object2DoubleAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

public class EnergyTransferHandler extends LocalNetworkHandler implements IWorldTickable
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "energy_transfer");

	private final Multimap<ConnectionPoint, Path> energyPaths = MultimapBuilder.hashKeys().arrayListValues().build();
	private final Object2IntMap<Connection> transferredInTick = new Object2IntOpenHashMap<>();
	private final Map<ConnectionPoint, EnergyConnector> sources = new HashMap<>();
	private final Map<ConnectionPoint, EnergyConnector> sinks = new HashMap<>();
	private boolean uninitialised = true;

	public EnergyTransferHandler(LocalWireNetwork net)
	{
		super(net);
	}

	@Override
	public LocalNetworkHandler merge(LocalNetworkHandler other)
	{
		reset();
		return this;
	}

	@Override
	public void onConnectorLoaded(ConnectionPoint p, IImmersiveConnectable iic)
	{
		reset();//TODO slightly more intelligent behavior
	}

	@Override
	public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic)
	{
		reset();//TODO slightly more intelligent behavior
	}

	@Override
	public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic)
	{
		reset();
	}

	@Override
	public void onConnectionAdded(Connection c)
	{
		reset();
	}

	@Override
	public void onConnectionRemoved(Connection c)
	{
		reset();
	}

	@Override
	public void update(World w)
	{
		if(uninitialised)
			calcPaths();
		transferPower();
		burnOverloaded(w);
	}

	private void reset()
	{
		energyPaths.clear();
		transferredInTick.clear();
		sinks.clear();
		sources.clear();
		uninitialised = true;
	}

	private void calcPaths()
	{
		uninitialised = false;
		energyPaths.clear();
		//TODO Dijkstra, using a proper binary heap since PriorityQueue does not have decreaseKey. This is just DFS
		for(ConnectionPoint cp : net.getConnectionPoints())
		{
			IImmersiveConnectable iic = net.getConnector(cp);
			if(iic instanceof EnergyConnector)
			{
				EnergyConnector energyIIC = (EnergyConnector)iic;
				if(energyIIC.isSink(cp))
					sinks.put(cp, energyIIC);
				if(energyIIC.isSource(cp))
					sources.put(cp, energyIIC);
			}
		}
		if(sources.isEmpty()||sinks.isEmpty())
			return;
		for(ConnectionPoint source : sources.keySet())
		{
			Deque<ConnectionPoint> stack = new ArrayDeque<>();
			Set<ConnectionPoint> visited = new HashSet<>();
			List<Path> paths = new ArrayList<>();
			visited.add(source);
			stack.add(source);
			List<Connection> path = new ArrayList<>();
			double lossSum = 0;
			while(!stack.isEmpty())
			{
				ConnectionPoint current = stack.peek();
				boolean foundNext = false;
				for(Connection c : net.getConnections(current))
				{
					double loss = getBasicLoss(c);
					if(Double.isFinite(loss))
					{
						ConnectionPoint end = c.getOtherEnd(current);
						if(!visited.contains(end))
						{
							stack.push(end);
							visited.add(end);
							lossSum += loss;
							foundNext = true;
							break;
						}
					}
				}
				if(!foundNext)
				{
					if(!current.equals(source)&&sinks.containsKey(current)&&lossSum < 1)
						paths.add(new Path(path.toArray(new Connection[0]), source, current, lossSum));
					stack.pop();
					if(!path.isEmpty())
					{
						Connection removed = path.remove(path.size()-1);
						lossSum -= ((EnergyWiretype)removed.type).getBasicLossRate(removed);
					}
				}
			}
			energyPaths.putAll(source, paths);
		}
	}

	private void transferPower()
	{
		for(ConnectionPoint sourceCp : energyPaths.keySet())
		{
			EnergyConnector source = sources.get(sourceCp);
			int available = source.getAvailableEnergy();
			if(available <= 0)
				continue;
			double maxSum = 0;
			Object2DoubleMap<Path> maxOut = new Object2DoubleAVLTreeMap<>();
			for(Path p : energyPaths.get(sourceCp))
			{
				EnergyConnector end = sinks.get(p.end);
				int requested = end.getRequestedEnergy();
				if(requested <= 0)
					continue;
				double requiredAtSource = Math.min(requested/(1-p.loss), available);
				maxOut.put(p, requiredAtSource);
				maxSum += requiredAtSource;
			}
			double allowedFactor = Math.min(1, available/maxSum);
			for(Path p : maxOut.keySet())
			{
				double atSource = allowedFactor*maxOut.getDouble(p);
				double currentLoss = 0;
				ConnectionPoint currentPoint = sourceCp;
				for(Connection c : p.conns)
				{
					currentPoint = c.getOtherEnd(currentPoint);
					//TODO use Blu's loss formula
					currentLoss += ((EnergyWiretype)c.type).getBasicLossRate(c);
					if(!currentPoint.equals(p.end))
					{
						IImmersiveConnectable iic = net.getConnector(currentPoint);
						if(iic instanceof EnergyConnector)
						{
							double availableAtPoint = atSource*(1-currentLoss);
							((EnergyConnector)iic).onEnergyPassedThrough(availableAtPoint);
						}
					}
				}
				EnergyConnector sink = sinks.get(p.end);
				sink.insertEnergy((int)(atSource*(1-currentLoss)));
			}
			if(allowedFactor < 1)
				source.extractEnergy(available);
			else
				source.extractEnergy(MathHelper.ceil(maxSum));
		}
	}

	private void burnOverloaded(World world)
	{
		List<Pair<Connection, Integer>> toBurn = new ArrayList<>();
		for(Connection c : transferredInTick.keySet())
		{
			int transferred = transferredInTick.get(c);
			if(c.type instanceof EnergyWiretype&&((EnergyWiretype)c.type).shouldBurn(c, transferred))
			{
				toBurn.add(new ImmutablePair<>(c, transferred));
			}
		}
		for(Pair<Connection, Integer> c : toBurn)
		{
			((EnergyWiretype)c.getLeft().type).burn(c.getLeft(), c.getRight(), net.getGlobal(), world);
		}
	}

	private double getBasicLoss(Connection c)
	{
		if(c.isInternal())
			return 0;
		else if(c.type instanceof EnergyWiretype)
			return ((EnergyWiretype)c.type).getBasicLossRate(c);
		else
			return Double.POSITIVE_INFINITY;
	}

	private class Path implements Comparable<Path>
	{
		final Connection[] conns;
		final ConnectionPoint start;
		final ConnectionPoint end;
		final double loss;

		private Path(Connection[] conns, ConnectionPoint start, ConnectionPoint end, double loss)
		{
			this.conns = conns;
			this.start = start;
			this.end = end;
			this.loss = loss;
		}

		@Override
		public int compareTo(@Nonnull Path o)
		{
			return Double.compare(loss, o.loss);
		}
	}

	public interface EnergyWiretype
	{
		int getTransferRate();

		double getBasicLossRate(Connection c);

		double getLossRate(Connection c, int transferred);

		default boolean shouldBurn(Connection c, int power)
		{
			return power > getTransferRate();
		}

		default void burn(Connection c, int power, GlobalWireNetwork net, World w)
		{
			net.removeConnection(c);
			//TODO
		}
	}

	public interface EnergyConnector extends IImmersiveConnectable
	{
		boolean isSource(ConnectionPoint cp);

		boolean isSink(ConnectionPoint cp);

		default int getAvailableEnergy()
		{
			return 0;
		}

		default int getRequestedEnergy()
		{
			return 0;
		}

		default void insertEnergy(int amount)
		{
		}

		default void extractEnergy(int amount)
		{
		}

		default void onEnergyPassedThrough(double amount)
		{
		}
	}
}