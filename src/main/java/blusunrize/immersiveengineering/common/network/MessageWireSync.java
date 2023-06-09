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
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.Set;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.network.MessageWireSync.Operation.ADD;
import static blusunrize.immersiveengineering.common.network.MessageWireSync.Operation.REMOVE;

public class MessageWireSync implements IMessage
{
	private final ConnectionPoint start;
	private final ConnectionPoint end;
	private final WireType type;
	private final Operation operation;
	private final Vec3 offsetStart;
	private final Vec3 offsetEnd;

	public MessageWireSync(Connection conn, Operation operation)
	{
		this.start = conn.getEndA();
		this.end = conn.getEndB();
		this.type = conn.type;
		this.operation = operation;
		this.offsetStart = conn.getEndAOffset();
		this.offsetEnd = conn.getEndBOffset();
	}

	public MessageWireSync(FriendlyByteBuf buf)
	{
		operation = Operation.VALUES[buf.readByte()];
		start = readConnPoint(buf);
		end = readConnPoint(buf);
		type = WireType.getValue(buf.readUtf(128));
		offsetStart = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		offsetEnd = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	private ConnectionPoint readConnPoint(FriendlyByteBuf buf)
	{
		return new ConnectionPoint(buf.readBlockPos(), buf.readInt());
	}

	private void writeConnPoint(ConnectionPoint cp, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(cp.position());
		buf.writeInt(cp.index());
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeByte(operation.ordinal());
		writeConnPoint(start, buf);
		writeConnPoint(end, buf);
		buf.writeUtf(type.getUniqueName());
		buf.writeDouble(offsetStart.x).writeDouble(offsetStart.y).writeDouble(offsetStart.z);
		buf.writeDouble(offsetEnd.x).writeDouble(offsetEnd.y).writeDouble(offsetEnd.z);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			WireLogger.logger.debug(
					"Processing sync for connection from {} to {}, type {}, op {}",
					start, end, type, operation.name()
			);
			Player player = ImmersiveEngineering.proxy.getClientPlayer();
			Level w = player.level();

			GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(w);
			Connection connection = new Connection(type, start, end, offsetStart, offsetEnd);
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
		context.get().setPacketHandled(true);
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
}
