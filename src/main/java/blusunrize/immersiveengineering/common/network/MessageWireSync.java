/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;

import static blusunrize.immersiveengineering.common.network.MessageWireSync.Operation.ADD;
import static blusunrize.immersiveengineering.common.network.MessageWireSync.Operation.REMOVE;

public record MessageWireSync(SyncedConnection connection, Operation operation) implements IMessage
{
	public static final Type<MessageWireSync> ID = IMessage.createType("wire_sync");
	public static final StreamCodec<ByteBuf, MessageWireSync> CODEC = StreamCodec.composite(
			SyncedConnection.CODEC, MessageWireSync::connection,
			ByteBufCodecs.idMapper(i -> Operation.VALUES[i], Operation::ordinal), MessageWireSync::operation,
			MessageWireSync::new
	);

	public MessageWireSync(Connection conn, Operation operation)
	{
		this(new SyncedConnection(conn), operation);
	}

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			ConnectionPoint start = this.connection.start();
			ConnectionPoint end = this.connection.end();
			WireType type = this.connection.type();
			WireLogger.logger.debug(
					"Processing sync for connection from {} to {}, type {}, op {}",
					start, end, type, operation.name()
			);
			Player player = ImmersiveEngineering.proxy.getClientPlayer();
			Level w = player.level();

			GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(w);
			Connection connection = this.connection.toConnection();
			if(operation!=ADD&&globalNet.getNullableLocalNet(start)!=null&&globalNet.getNullableLocalNet(end)!=null)
			{
				globalNet.removeConnection(connection);
				removeProxyIfNoWires(start, globalNet);
				removeProxyIfNoWires(end, globalNet);
			}
			if(operation!=REMOVE)
				globalNet.addConnection(connection);
			Set<SectionPos> sectionsToRerender = new ObjectArraySet<>();
			WireUtils.forEachRenderPoint(connection, ($, $2, section) -> sectionsToRerender.add(section));
			for(SectionPos section : sectionsToRerender)
				Minecraft.getInstance().levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
		});
	}

	private void removeProxyIfNoWires(ConnectionPoint point, GlobalWireNetwork globalNet)
	{
		LocalWireNetwork localNet = globalNet.getLocalNet(point);
		IImmersiveConnectable iic = localNet.getConnector(point);
		if(iic.isProxy()&&!WireUtils.hasAnyConnections(globalNet, iic))
			globalNet.removeConnector(iic);
	}

	public enum Operation
	{
		ADD, REMOVE, UPDATE;

		private static final Operation[] VALUES = values();
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
