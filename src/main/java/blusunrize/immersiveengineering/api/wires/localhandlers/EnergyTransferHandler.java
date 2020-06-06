/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.localhandlers;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.utils.BinaryHeap;
import blusunrize.immersiveengineering.api.wires.utils.BinaryHeap.HeapEntry;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class EnergyTransferHandler extends LocalNetworkHandler implements IWorldTickable
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "energy_transfer");

	private final Table<ConnectionPoint, ConnectionPoint, Path> energyPaths = HashBasedTable.create();
	private Object2DoubleMap<Connection> transferredNextTick = new Object2DoubleOpenHashMap<>();
	private Object2DoubleMap<Connection> transferredLastTick = new Object2DoubleOpenHashMap<>();
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
		transferredLastTick = transferredNextTick;
		transferredNextTick = new Object2DoubleOpenHashMap<>();
	}

	/**
	 * @return the transfer map for the next transfert tick. Modify to include transfers made outside of the usual
	 * transfer code in wire burn calculations, CT measurements etc.
	 */
	public Object2DoubleMap<Connection> getTransferredNextTick()
	{
		return transferredNextTick;
	}

	/**
	 * @return the amount of energy transferred by each connection in the last tick. Must not be modified.
	 */
	public Object2DoubleMap<Connection> getTransferredLastTick()
	{
		return Object2DoubleMaps.unmodifiable(transferredLastTick);
	}

	private void reset()
	{
		energyPaths.clear();
		transferredNextTick.clear();
		transferredLastTick.clear();
		sinks.clear();
		sources.clear();
		uninitialised = true;
	}

	public Map<ConnectionPoint, EnergyConnector> getSources()
	{
		if(uninitialised)
			calcPaths();
		return sources;
	}

	@Nullable
	public Path getPath(ConnectionPoint source, ConnectionPoint sink)
	{
		if(uninitialised)
			calcPaths();
		if(sources.containsKey(source)&&sinks.containsKey(sink))
			return energyPaths.get(source, sink);
		else
		{
			final Path[] ret = {null};
			runDijkstraWithSource(source, p -> {
				if(p.end.equals(sink))
				{
					ret[0] = p;
					return true;
				}
				else
					return false;
			});
			return ret[0];
		}
	}

	private void calcPaths()
	{
		uninitialised = false;
		energyPaths.clear();
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
			runDijkstraWithSource(source, p -> {
				energyPaths.put(source, p.end, p);
				return false;
			});
	}

	private void runDijkstraWithSource(ConnectionPoint source, Predicate<Path> stopAfter)
	{
		Map<ConnectionPoint, Path> shortestKnown = new HashMap<>();
		BinaryHeap<ConnectionPoint> heap = new BinaryHeap<>(
				Comparator.comparingDouble(end -> shortestKnown.get(end).loss));
		Map<ConnectionPoint, HeapEntry<ConnectionPoint>> entryMap = new HashMap<>();
		shortestKnown.put(source, new Path(source));
		entryMap.put(source, heap.insert(source));
		while(!heap.empty())
		{
			ConnectionPoint endPoint = heap.extractMin();
			entryMap.remove(endPoint);
			Path shortest = shortestKnown.get(endPoint);
			if(stopAfter.test(shortest))
				return;
			//Loss of 1 means no energy will be transferred, so the paths are irrelevant
			if(shortest.loss >= 1)
				break;
			for(Connection next : net.getConnections(endPoint))
			{
				Path alternative = shortest.append(next, sinks.containsKey(next.getOtherEnd(shortest.end)));
				if(!shortestKnown.containsKey(alternative.end))
				{
					shortestKnown.put(alternative.end, alternative);
					entryMap.put(alternative.end, heap.insert(alternative.end));
				}
				else
				{
					Path oldPath = shortestKnown.get(alternative.end);
					if(alternative.loss < oldPath.loss)
					{
						shortestKnown.put(alternative.end, alternative);
						heap.decreaseKey(entryMap.get(alternative.end));
					}
				}
			}
		}
	}

	private void transferPower()
	{
		for(ConnectionPoint sourceCp : energyPaths.rowKeySet())
		{
			EnergyConnector source = sources.get(sourceCp);
			int available = source.getAvailableEnergy();
			if(available <= 0)
				continue;
			double maxSum = 0;
			Object2DoubleMap<Path> maxOut = new Object2DoubleOpenHashMap<>();
			for(Path p : energyPaths.row(sourceCp).values())
				if(p.isPathToSink)
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
					currentLoss += getBasicLoss(c);
					double availableAtPoint = atSource*(1-currentLoss);
					double transferred = transferredNextTick.getDouble(c);
					transferredNextTick.put(c, transferred+availableAtPoint);
					if(!currentPoint.equals(p.end))
					{
						IImmersiveConnectable iic = net.getConnector(currentPoint);
						if(iic instanceof EnergyConnector)
							((EnergyConnector)iic).onEnergyPassedThrough(availableAtPoint);
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
		List<Pair<Connection, Double>> toBurn = new ArrayList<>();
		for(Connection c : transferredNextTick.keySet())
		{
			double transferred = transferredNextTick.getDouble(c);
			if(c.type instanceof IEnergyWire&&((IEnergyWire)c.type).shouldBurn(c, transferred))
				toBurn.add(new ImmutablePair<>(c, transferred));
		}
		for(Pair<Connection, Double> c : toBurn)
			((IEnergyWire)c.getLeft().type).burn(c.getLeft(), c.getRight(), net.getGlobal(), world);
	}

	private static double getBasicLoss(Connection c)
	{
		if(c.isInternal())
			return 0;
		else if(c.type instanceof IEnergyWire)
			return ((IEnergyWire)c.type).getBasicLossRate(c);
		else
			return Double.POSITIVE_INFINITY;
	}

	public static class Path
	{
		public final Connection[] conns;
		public final ConnectionPoint start;
		public final ConnectionPoint end;
		public final double loss;
		public final boolean isPathToSink;

		private Path(Connection[] conns, ConnectionPoint start, ConnectionPoint end, double loss, boolean isPathToSink)
		{
			this.conns = conns;
			this.start = start;
			this.end = end;
			this.loss = loss;
			this.isPathToSink = isPathToSink;
		}

		public Path(ConnectionPoint point)
		{
			this(new Connection[0], point, point, 0, false);
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			Path path = (Path)o;
			return Arrays.equals(conns, path.conns);
		}

		@Override
		public int hashCode()
		{
			return Arrays.hashCode(conns);
		}

		public Path append(Connection next, boolean isPathToSink)
		{
			ConnectionPoint newEnd = next.getOtherEnd(end);
			double newLoss = loss+getBasicLoss(next);
			Connection[] newPath = Arrays.copyOf(conns, conns.length+1);
			newPath[newPath.length-1] = next;
			return new Path(newPath, start, newEnd, newLoss, isPathToSink);
		}
	}

	public interface IEnergyWire
	{
		int getTransferRate();

		double getBasicLossRate(Connection c);

		double getLossRate(Connection c, int transferred);

		default boolean shouldBurn(Connection c, double power)
		{
			return power > getTransferRate();
		}

		default void burn(Connection c, double power, GlobalWireNetwork net, World w)
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