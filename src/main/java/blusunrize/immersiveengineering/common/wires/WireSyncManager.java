/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.network.MessageWireSync;
import blusunrize.immersiveengineering.common.network.MessageWireSync.Operation;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID)
public class WireSyncManager implements IWireSyncManager
{
	private static final SetMultimap<UUID, ChunkPos> wireWatchedChunksByPlayer = HashMultimap.create();

	private static void sendMessagesForChunk(Level w, ChunkPos pos, ServerPlayer player, boolean add)
	{
		GlobalWireNetwork net = GlobalWireNetwork.getNetwork(w);
		Collection<ConnectionPoint> connsInChunk = net.getAllConnectorsIn(pos);
		final Operation operation = add?Operation.ADD: Operation.REMOVE;
		for(ConnectionPoint cp : connsInChunk)
			for(Connection conn : net.getLocalNet(cp).getConnections(cp))
				if(shouldSendConnection(conn, pos, player, add, cp))
				{
					WireLogger.logger.info("Sending connection {} ({}) for chunk change at {}", conn, add, pos);
					ImmersiveEngineering.packetHandler.send(
							PacketDistributor.PLAYER.with(() -> player), new MessageWireSync(conn, operation)
					);
				}
	}

	private static boolean shouldSendConnection(Connection conn, ChunkPos pos, ServerPlayer player, boolean add,
												ConnectionPoint currEnd)
	{
		if(conn.isInternal())
			return false;
		ConnectionPoint other = conn.getOtherEnd(currEnd);
		ChunkPos otherChunk = new ChunkPos(other.position());
		if(otherChunk.equals(pos))
			return conn.isPositiveEnd(currEnd);
		else
			return wireWatchedChunksByPlayer.containsEntry(player.getUUID(), otherChunk);
	}

	private static void addPlayersTrackingPoint(Set<ServerPlayer> receivers, int x, int z, ServerLevel world)
	{
		ServerChunkCache chunkProvider = world.getChunkSource();
		for(ServerPlayer e : chunkProvider.chunkMap.getPlayers(new ChunkPos(x >> 4, z >> 4), false))
		{
			WireLogger.logger.debug("Watching player for {}, {}: {}", x, z, e);
			receivers.add(e);
		}
	}

	private static void sendToPlayersForConnection(Operation operation, Level level, Connection connection)
	{
		if(connection.isInternal()||!(level instanceof ServerLevel serverLevel))
			return;
		ApiUtils.addFutureServerTask(serverLevel, () -> {
			Set<ServerPlayer> targets = new HashSet<>();
			addPlayersTrackingPoint(targets, connection.getEndA().getX(), connection.getEndA().getZ(), serverLevel);
			addPlayersTrackingPoint(targets, connection.getEndB().getX(), connection.getEndB().getZ(), serverLevel);
			MessageWireSync msg = new MessageWireSync(connection, operation);
			for(ServerPlayer p : targets)
				ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> p), msg);
		}, true);
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch ev)
	{
		ApiUtils.addFutureServerTask(ev.getLevel(),
				() -> {
					if(wireWatchedChunksByPlayer.put(ev.getPlayer().getUUID(), ev.getPos()))
						sendMessagesForChunk(ev.getLevel(), ev.getPos(), ev.getPlayer(), true);
				}, true);
	}

	@SubscribeEvent
	public static void onChunkUnWatch(ChunkWatchEvent.UnWatch ev)
	{
		ApiUtils.addFutureServerTask(ev.getLevel(),
				() -> {
					if(wireWatchedChunksByPlayer.remove(ev.getPlayer().getUUID(), ev.getPos()))
						sendMessagesForChunk(ev.getLevel(), ev.getPos(), ev.getPlayer(), false);
				}, true);
	}

	private final Level world;

	public WireSyncManager(Level world)
	{
		this.world = world;
	}

	public void onConnectionAdded(Connection c)
	{
		sendToPlayersForConnection(Operation.ADD, world, c);
	}

	public void onConnectionRemoved(Connection c)
	{
		sendToPlayersForConnection(Operation.REMOVE, world, c);
	}

	@Override
	public void onConnectionEndpointsChanged(Connection c)
	{
		sendToPlayersForConnection(Operation.UPDATE, world, c);
	}
}
