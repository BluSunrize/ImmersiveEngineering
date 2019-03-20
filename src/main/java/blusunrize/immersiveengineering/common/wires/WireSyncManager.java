/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.common.util.network.MessageWireSync;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber
public class WireSyncManager
{
	private static void sendMessagesForChunk(Chunk chunk, EntityPlayerMP player, boolean add)
	{
		World w = chunk.getWorld();
		GlobalWireNetwork net = GlobalWireNetwork.getNetwork(w);
		Collection<ConnectionPoint> connsInChunk = net.getAllConnectorsIn(chunk.getPos());
		for(ConnectionPoint cp : connsInChunk)
			for(Connection conn : net.getLocalNet(cp).getConnections(cp))
				if(!conn.isInternal()&&conn.isPositiveEnd(cp))
					ImmersiveEngineering.packetHandler.sendTo(new MessageWireSync(conn, add), player);
	}

	private static void addPlayersTrackingPoint(Set<EntityPlayerMP> receivers, int x, int z, WorldServer world)
	{
		PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(MathHelper.floor(x) >> 4, MathHelper.floor(z) >> 4);
		if(entry==null)
			return;
		receivers.addAll(entry.getWatchingPlayers());
	}

	private static void sendToPlayersForConnection(IMessage msg, WorldServer world, Connection c)
	{
		Set<EntityPlayerMP> targets = new HashSet<>();
		addPlayersTrackingPoint(targets, c.getEndA().getX(), c.getEndA().getZ(), world);
		addPlayersTrackingPoint(targets, c.getEndB().getX(), c.getEndB().getZ(), world);
		for(EntityPlayerMP p : targets)
			ImmersiveEngineering.packetHandler.sendTo(msg, p);
	}

	public static void onConnectionAdded(Connection c, World w)
	{
		if(!w.isRemote&&w instanceof WorldServer)
			sendToPlayersForConnection(new MessageWireSync(c, true), (WorldServer)w, c);
	}

	public static void onConnectionRemoved(Connection c, World w)
	{
		if(!w.isRemote&&w instanceof WorldServer)
			sendToPlayersForConnection(new MessageWireSync(c, false), (WorldServer)w, c);
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch ev)
	{
		Chunk chunk = ev.getChunkInstance();
		if(chunk!=null)
			sendMessagesForChunk(chunk, ev.getPlayer(), true);
	}

	@SubscribeEvent
	public static void onChunkUnWatch(ChunkWatchEvent.UnWatch ev)
	{
		Chunk chunk = ev.getChunkInstance();
		if(chunk!=null)
			sendMessagesForChunk(chunk, ev.getPlayer(), false);
	}
}
