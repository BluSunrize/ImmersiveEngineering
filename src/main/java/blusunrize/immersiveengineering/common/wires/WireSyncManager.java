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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
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
		for(ConnectionPoint cp : connsInChunk)
			for(Connection conn : net.getLocalNet(cp).getConnections(cp))
				if(shouldSendConnection(conn, pos, player, add, cp))
				{
					WireLogger.logger.info("Sending connection {} ({}) for chunk change at {}", conn, add, pos);
					ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> player), new MessageWireSync(conn, add));
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

	private static <T> void sendToPlayersForConnection(T msg, ServerLevel world, Connection c)
	{
		ApiUtils.addFutureServerTask(world, () -> {
			Set<ServerPlayer> targets = new HashSet<>();
			addPlayersTrackingPoint(targets, c.getEndA().getX(), c.getEndA().getZ(), world);
			addPlayersTrackingPoint(targets, c.getEndB().getX(), c.getEndB().getZ(), world);
			for(ServerPlayer p : targets)
				ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> p), msg);
		}, true);
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch ev)
	{
		ApiUtils.addFutureServerTask(ev.getWorld(),
				() -> {
					if(wireWatchedChunksByPlayer.put(ev.getPlayer().getUUID(), ev.getPos()))
						sendMessagesForChunk(ev.getWorld(), ev.getPos(), ev.getPlayer(), true);
				}, true);
	}

	@SubscribeEvent
	public static void onChunkUnWatch(ChunkWatchEvent.UnWatch ev)
	{
		ApiUtils.addFutureServerTask(ev.getWorld(),
				() -> {
					if(wireWatchedChunksByPlayer.remove(ev.getPlayer().getUUID(), ev.getPos()))
						sendMessagesForChunk(ev.getWorld(), ev.getPos(), ev.getPlayer(), false);
				}, true);
	}

	private final Level world;

	public WireSyncManager(Level world)
	{
		this.world = world;
	}

	public void onConnectionAdded(Connection c)
	{
		if(!c.isInternal()&&!world.isClientSide&&world instanceof ServerLevel)
			sendToPlayersForConnection(new MessageWireSync(c, true), (ServerLevel)world, c);
	}

	public void onConnectionRemoved(Connection c)
	{
		if(!c.isInternal()&&!world.isClientSide&&world instanceof ServerLevel)
			sendToPlayersForConnection(new MessageWireSync(c, false), (ServerLevel)world, c);
	}
}
