/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;


import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties.ConnectionModelData;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.client.utils.CombinedModelData;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public abstract class ImmersiveConnectableTileEntity extends IEBaseTileEntity implements IImmersiveConnectable
{
	protected GlobalWireNetwork globalNet;
	private static Map<BlockPos, Deque<EventQueueEntry>> queuedEvents = new HashMap<>();

	public ImmersiveConnectableTileEntity(TileEntityType<? extends ImmersiveConnectableTileEntity> type)
	{
		super(type);
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return getPos();
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		this.markDirty();
		if(this.world!=null)
		{
			BlockState state = this.world.getBlockState(pos);
			this.world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	@Override
	public void setWorldAndPos(World worldIn, BlockPos pos)
	{
		super.setWorldAndPos(worldIn, pos);
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==-1||id==255)
		{
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		else if(id==254)
		{
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset)
	{
		return new ConnectionPoint(pos, 0);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
	}

	public ConnectionModelData genConnBlockstate()
	{
		Set<Connection> ret = new HashSet<>();
		for(ConnectionPoint cp : getConnectionPoints())
		{
			LocalWireNetwork local = globalNet.getLocalNet(cp);
			Collection<Connection> conns = local.getConnections(cp);
			if(conns==null)
			{
				WireLogger.logger.warn("Aborting and returning empty data: null connections at {}", cp);
				return new ConnectionModelData(ImmutableSet.of(), pos);
			}
			//TODO change model data to only include catenary (a, oX, oY) and number of vertices to render
			for(Connection c : conns)
			{
				ConnectionPoint other = c.getOtherEnd(cp);
				if(!c.isInternal())
				{
					IImmersiveConnectable otherConnector = globalNet.getLocalNet(other).getConnector(other);
					if(otherConnector!=null&&!otherConnector.isProxy())
					{
						// generate subvertices
						c.generateCatenaryData(world);
						ret.add(c);
					}
				}
			}
		}
		return new ConnectionModelData(ret, pos);
	}

	@Nonnull
	@Override
	public IModelData getModelData()
	{
		return new CombinedModelData(new SinglePropertyModelData<>(genConnBlockstate(), Model.CONNECTIONS),
				super.getModelData());
	}

	@Override
	public void onChunkUnloaded()
	{
		super.onChunkUnloaded();
		queueEvent(LoadUnloadEvent.UNLOAD);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		queueEvent(LoadUnloadEvent.LOAD);
	}

	@Override
	public void remove()
	{
		super.remove();
		queueEvent(LoadUnloadEvent.REMOVE);
	}

	private void queueEvent(LoadUnloadEvent ev)
	{
		if(world!=null)
		{
			if(!getWorldNonnull().isRemote)
				ev.run(this);
			else
			{
				Deque<EventQueueEntry> queue = queuedEvents.get(pos);
				if(queue==null)
				{
					queue = new ArrayDeque<>();
					queuedEvents.put(pos, queue);
				}
				if(queue.isEmpty()||queue.getLast().tick!=world.getGameTime())
					ApiUtils.addFutureServerTask(getWorldNonnull(), () -> processEvents(pos), true);
				queue.add(new EventQueueEntry(ev, this, getWorldNonnull().getGameTime()));
				WireLogger.logger.info("Queuing {} at {} (tile {})", ev, getPos(), this);
			}
		}
	}

	// Loading, unloading and removing is strange on the client, so we need to make sure we don't remove IICs from the
	// net that shouldn't be. This fixes #4152
	// In 1.16 this is even worse than in previous versions, with unload happening 1 tick after load when a TE is
	// replaced.
	private static void processEvents(BlockPos pos)
	{
		ImmersiveConnectableTileEntity toUnload = null;
		List<ImmersiveConnectableTileEntity> loadedInTick = new ArrayList<>();
		Queue<EventQueueEntry> events = queuedEvents.get(pos);
		for(EventQueueEntry e : events)
		{
			switch(e.type)
			{
				case LOAD:
					loadedInTick.add(e.tile);
					break;
				case UNLOAD:
				case REMOVE:
					boolean wasNew = loadedInTick.remove(e.tile);
					if(!wasNew)
					{
						Preconditions.checkState(
								toUnload==null,
								"Unloading two IICs at %s in the same tick: %s and %s, events %s",
								e.tile, toUnload, events
						);
						toUnload = e.tile;
					}
					break;
			}
		}
		Preconditions.checkState(
				loadedInTick.size() < 2,
				"Too many IICs loaded at %s in one tick: %s, queue is %s",
				pos, loadedInTick, events
		);
		if(toUnload!=null)
		{
			if(!loadedInTick.isEmpty())
				LoadUnloadEvent.UNLOAD.run(toUnload);
			else
				LoadUnloadEvent.REMOVE.run(toUnload);
		}
		if(!loadedInTick.isEmpty())
		{
			ImmersiveConnectableTileEntity loaded = loadedInTick.get(0);
			ConnectionPoint testPoint = loaded.getConnectionPoints().iterator().next();
			IImmersiveConnectable currentLoaded = loaded.globalNet.getLocalNet(testPoint).getConnector(testPoint);
			if(currentLoaded==null||currentLoaded.isProxy())
			{
				LoadUnloadEvent.LOAD.run(loaded);
				queuedEvents.remove(pos);
			}
			else
			{
				events.clear();
				events.add(new EventQueueEntry(LoadUnloadEvent.LOAD, loaded, loaded.getWorldNonnull().getGameTime()-1));
			}
		}
		else
			queuedEvents.remove(pos);
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, 0));
	}

	private final Int2ObjectMap<LocalWireNetwork> cachedLocalNets = new Int2ObjectArrayMap<>();

	protected LocalWireNetwork getLocalNet(int cpIndex)
	{
		LocalWireNetwork ret = cachedLocalNets.get(cpIndex);
		ConnectionPoint cp = new ConnectionPoint(getPos(), cpIndex);
		if(ret==null||!ret.isValid(cp))
		{
			ret = globalNet.getLocalNet(cp);
			cachedLocalNets.put(cpIndex, ret);
		}
		return ret;
	}

	@Override
	public BlockPos getPosition()
	{
		return pos;
	}

	private enum LoadUnloadEvent
	{
		LOAD(te -> te.globalNet.onConnectorLoad(te, te.getWorld())),
		UNLOAD(te -> te.globalNet.onConnectorUnload(te.getPos(), te)),
		REMOVE(te -> te.globalNet.removeConnector(te));

		private final Consumer<ImmersiveConnectableTileEntity> run;

		LoadUnloadEvent(Consumer<ImmersiveConnectableTileEntity> run)
		{
			this.run = run;
		}

		public void run(ImmersiveConnectableTileEntity tile)
		{
			WireLogger.logger.info("Running {} at {} (tile {})", name(), tile.getPos(), tile);
			run.accept(tile);
		}

		@Override
		public String toString()
		{
			return name();
		}
	}

	private static class EventQueueEntry
	{
		private final LoadUnloadEvent type;
		private final ImmersiveConnectableTileEntity tile;
		private final long tick;

		private EventQueueEntry(LoadUnloadEvent type, ImmersiveConnectableTileEntity tile, long tick)
		{
			this.type = type;
			this.tile = tile;
			this.tick = tick;
		}

		@Override
		public String toString()
		{
			return new StringJoiner(", ", EventQueueEntry.class.getSimpleName()+"[", "]")
					.add("type="+type)
					.add("tile="+tile)
					.add("tick="+tick)
					.toString();
		}
	}
}