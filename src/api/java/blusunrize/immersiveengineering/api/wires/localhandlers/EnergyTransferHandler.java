/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.localhandlers;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.utils.BinaryHeap;
import blusunrize.immersiveengineering.api.wires.utils.BinaryHeap.HeapEntry;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class EnergyTransferHandler extends LocalNetworkHandler implements IWorldTickable
{
	public static final ResourceLocation ID = new ResourceLocation(Lib.MODID, "energy_transfer");

	private final Map<ConnectionPoint, Map<ConnectionPoint, Path>> energyPaths = new HashMap<>();
	private Object2DoubleOpenHashMap<Connection> transferredNextTick = new Object2DoubleOpenHashMap<>();
	private Object2DoubleMap<Connection> transferredLastTick = new Object2DoubleOpenHashMap<>();
	private final Map<ConnectionPoint, EnergyConnector> sources = new HashMap<>();
	private final Map<ConnectionPoint, EnergyConnector> sinks = new HashMap<>();
	private final List<SinkPathsFromSource> transferPaths = new ArrayList<>();
	private boolean sourceSinkMapInitialized = true;
	HashMap<Connection, List<Double>> limits = new HashMap<>();

	public EnergyTransferHandler(LocalWireNetwork net, GlobalWireNetwork global)
	{
		super(net, global);
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
	public void update(Level w)
	{
		transferPower();
		transferredLastTick = transferredNextTick;
		transferredNextTick = new Object2DoubleOpenHashMap<>();
		burnOverloaded(w);
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
		transferPaths.clear();
		sourceSinkMapInitialized = false;
		limits.clear();;
	}

	public Map<ConnectionPoint, EnergyConnector> getSources()
	{
		updateSourcesAndSinks();
		return sources;
	}

	/**
	 * @return shortest (w.r.t. base loss) path from source to sink. null if there is no path with base loss <1
	 */
	@Nullable
	public Path getPath(ConnectionPoint source, ConnectionPoint sink)
	{
		return getPathsFromSource(source).get(sink);
	}

	public Map<ConnectionPoint, Path> getPathsFromSource(ConnectionPoint source)
	{
		Map<ConnectionPoint, Path> mutableResult = energyPaths.get(source);
		if(mutableResult==null)
		{
			mutableResult = new HashMap<>();
			Map<ConnectionPoint, Path> finalMutableResult = mutableResult;
			runDijkstraWithSource(source, p -> finalMutableResult.put(p.end, p));
			energyPaths.put(source, mutableResult);
		}
		return Collections.unmodifiableMap(mutableResult);
	}

	private void updateSourcesAndSinks()
	{
		if(sourceSinkMapInitialized)
			return;
		sourceSinkMapInitialized = true;
		for(ConnectionPoint cp : localNet.getConnectionPoints())
		{
			IImmersiveConnectable iic = localNet.getConnector(cp);
			if(iic instanceof EnergyConnector energyIIC)
			{
				if(energyIIC.isSink(cp))
					sinks.put(cp, energyIIC);
				if(energyIIC.isSource(cp))
					sources.put(cp, energyIIC);
				if(energyIIC instanceof LimitingEnergyConnector limiting)
					for(Connection c : localNet.getConnections(cp))
						limits.put(c, Arrays.asList(limiting.getPowerLimit(), limiting.getPowerLimit()));
			}
		}
		for(Entry<ConnectionPoint, EnergyConnector> source : sources.entrySet())
		{
			Map<ConnectionPoint, Path> paths = getPathsFromSource(source.getKey());
			List<SinkPath> sinkPaths = new ArrayList<>();
			for(Entry<ConnectionPoint, EnergyConnector> sink : sinks.entrySet())
			{
				Path pathTo = paths.get(sink.getKey());
				if(pathTo!=null)
					sinkPaths.add(new SinkPath(sink.getKey(), sink.getValue(), pathTo));
			}
			transferPaths.add(new SinkPathsFromSource(source.getKey(), source.getValue(), sinkPaths));
		}
	}

	private void runDijkstraWithSource(ConnectionPoint source, Consumer<Path> output)
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
			output.accept(shortest);
			//Loss of 1 means no energy will be transferred, so the paths are irrelevant
			if(shortest.loss >= 1)
				break;
			for(Connection next : localNet.getConnections(endPoint))
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
		updateSourcesAndSinks();
		resetLimits();
		for(SinkPathsFromSource sourceData : transferPaths)
		{
			ConnectionPoint sourceCp = sourceData.sourceCP();
			EnergyConnector source = sourceData.sourceConnector();
			int available = source.getAvailableEnergy();
			if(available <= 0)
				continue;
			double maxSum = 0;
			record OutputData(double amount, Path path, EnergyConnector output)
			{
			}
			List<OutputData> maxOut = new ArrayList<>(sourceData.paths().size());
			for(SinkPath sinkEntry : sourceData.paths())
			{
				double limit = Double.MAX_VALUE;
				Connection conn = null;
				for(Connection c : sinkEntry.pathTo().conns)
					if (limits.containsKey(c))
					{
						limit = limits.get(c).get(1);
						conn = c;
					}
				EnergyConnector sink = sinkEntry.sinkConnector();
				int requested = (int)Math.min(sink.getRequestedEnergy(), limit*(1-sinkEntry.pathTo().loss));
				if(requested <= 0)
					continue;
				double requiredAtSource = Math.min(requested/(1-sinkEntry.pathTo().loss), available);
				if(conn!=null)
					limits.put(conn, Arrays.asList(limits.get(conn).get(0), limit-requiredAtSource));
				maxOut.add(new OutputData(requiredAtSource, sinkEntry.pathTo(), sink));
				maxSum += requiredAtSource;
			}
			if(maxSum==0)
				continue;
			double allowedFactor = Math.min(1, available/maxSum);
			for(OutputData entry : maxOut)
			{
				Path path = entry.path();
				double atSource = allowedFactor*entry.amount();
				double availableFactor = 1;
				ConnectionPoint currentPoint = sourceCp;
				double availableRecent = atSource;
				for(Connection c : path.conns)
				{
					currentPoint = c.getOtherEnd(currentPoint);
					// We use exponential loss here so there is still some power at arbitrarily far distances
					availableRecent*=(1-getBasicLoss(c));
					IImmersiveConnectable iic = localNet.getConnector(currentPoint);
					if(!currentPoint.equals(path.end) && iic instanceof EnergyConnector connector)
						connector.onEnergyPassedThrough(availableRecent);
					transferredNextTick.addTo(c, availableRecent);
				}
				entry.output.insertEnergy(ceilIfClose(atSource*availableFactor));
			}
			if(allowedFactor < 1)
				source.extractEnergy(available);
			else
				source.extractEnergy(Mth.ceil(maxSum));
		}
	}

	private int ceilIfClose(double in)
	{
		return (int)(in+0.01);
	}

	private void burnOverloaded(Level world)
	{
		Preconditions.checkNotNull(globalNet);
		List<Pair<Connection, Double>> toBurn = new ArrayList<>();
		for(Object2DoubleMap.Entry<Connection> entry : transferredLastTick.object2DoubleEntrySet())
		{
			Connection c = entry.getKey();
			double transferred = entry.getDoubleValue();
			if(c.type instanceof IEnergyWire&&((IEnergyWire)c.type).shouldBurn(c, transferred))
				toBurn.add(Pair.of(c, transferred));
		}
		for(Pair<Connection, Double> c : toBurn)
			((IEnergyWire)c.getFirst().type).burn(c.getFirst(), c.getSecond(), globalNet, world);
	}

	private static double getBasicLoss(Connection c)
	{
		if(c.isInternal())
			return 0;
		else if(c.type instanceof IEnergyWire energyWire)
			return energyWire.getBasicLossRate(c);
		else
			return Double.POSITIVE_INFINITY;
	}

	private void resetLimits()
	{
		if (limits.isEmpty()) return;
		limits.replaceAll((p, v) -> Arrays.asList(limits.get(p).get(0), limits.get(p).get(0)));
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
			double newLoss = loss+(1-loss)*getBasicLoss(next);
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

		default void burn(Connection c, double power, GlobalWireNetwork net, Level w)
		{
			net.removeConnection(c);
			if(w instanceof ServerLevel)
			{
				final int numPoints = 16;
				final Vec3 offset = Vec3.atLowerCornerOf(c.getEndA().position());
				for(int i = 1; i < numPoints; ++i)
				{
					final double posOnWire = i/(double)numPoints;
					final Vec3 pos = c.getPoint(posOnWire, c.getEndA()).add(offset);
					((ServerLevel)w).sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0, 0, 0, 0, 1);
				}
			}
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

	public interface LimitingEnergyConnector extends EnergyConnector
	{
		double getPowerLimit();
	}

	private record SinkPath(ConnectionPoint sinkCP, EnergyConnector sinkConnector, Path pathTo)
	{
	}

	private record SinkPathsFromSource(
			ConnectionPoint sourceCP, EnergyConnector sourceConnector, List<SinkPath> paths
	)
	{
	}
}
