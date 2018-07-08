/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.client.ClientEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.concurrent.atomic.AtomicInteger;

public class MessageObstructedConnection implements IMessage
{
	private Vec3d start, end;
	private BlockPos startB, endB, blocking;
	private WireType wireType;

	public MessageObstructedConnection(ImmersiveNetHandler.Connection conn, BlockPos blocking, World w)
	{
		this.blocking = blocking;
		Vec3d[] vertices = conn.getSubVertices(w);
		start = vertices[0];
		end = vertices[vertices.length-1];
		startB = conn.start;
		endB = conn.end;
		wireType = conn.cableType;
	}

	public MessageObstructedConnection()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		start = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		end = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		startB = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		endB = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		blocking = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		wireType = WireType.getValue(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeDouble(start.x).writeDouble(start.y).writeDouble(start.z);
		buf.writeDouble(end.x).writeDouble(end.y).writeDouble(end.z);
		buf.writeInt(startB.getX()).writeInt(startB.getY()).writeInt(startB.getZ());
		buf.writeInt(endB.getX()).writeInt(endB.getY()).writeInt(endB.getZ());
		buf.writeInt(blocking.getX()).writeInt(blocking.getY()).writeInt(blocking.getZ());
		ByteBufUtils.writeUTF8String(buf, wireType.getUniqueName());
	}

	public static class Handler implements IMessageHandler<MessageObstructedConnection, IMessage>
	{
		@Override
		public IMessage onMessage(MessageObstructedConnection message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				ImmersiveNetHandler.Connection conn = new ImmersiveNetHandler.Connection(message.startB, message.endB, message.wireType,
						(int)Math.sqrt(message.startB.distanceSq(message.endB)));
				conn.getSubVertices(message.start, message.end);
				ClientEventHandler.FAILED_CONNECTIONS.put(conn,
						new ImmutablePair<>(message.blocking, new AtomicInteger(200)));
			});
			return null;
		}
	}
}