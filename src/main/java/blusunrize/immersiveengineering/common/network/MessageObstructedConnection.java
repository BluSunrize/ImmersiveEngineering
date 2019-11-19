/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.ClientEventHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MessageObstructedConnection implements IMessage
{
	private Vec3d start, end;
	private BlockPos startB, endB, blocking;
	private WireType wireType;

	public MessageObstructedConnection(Connection conn, BlockPos blocking, World w)
	{
		this.blocking = blocking;
		start = conn.getPoint(0, conn.getEndA());
		end = conn.getPoint(1, conn.getEndA());
		startB = conn.getEndA().getPosition();
		endB = conn.getEndB().getPosition();
		wireType = conn.type;
	}

	public MessageObstructedConnection(PacketBuffer buf)
	{
		start = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		end = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		startB = buf.readBlockPos();
		endB = buf.readBlockPos();
		blocking = buf.readBlockPos();
		wireType = WireType.getValue(buf.readString(100));
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeDouble(start.x).writeDouble(start.y).writeDouble(start.z);
		buf.writeDouble(end.x).writeDouble(end.y).writeDouble(end.z);
		buf.writeBlockPos(startB);
		buf.writeBlockPos(endB);
		buf.writeBlockPos(blocking);
		buf.writeString(wireType.getUniqueName());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			Connection conn = new Connection(wireType, new ConnectionPoint(startB, 0),
					new ConnectionPoint(endB, 0));
			conn.generateCatenaryData(start, end);
			ClientEventHandler.FAILED_CONNECTIONS.put(conn,
					new ImmutablePair<>(blocking, new AtomicInteger(200)));
		});
	}
}