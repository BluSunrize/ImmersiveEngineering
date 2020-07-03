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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MessageObstructedConnection implements IMessage
{
	private Vector3d start, end;
	private BlockPos startB, endB;
	private Collection<BlockPos> blocking;
	private WireType wireType;

	public MessageObstructedConnection(Connection conn, Collection<BlockPos> blocking)
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
		start = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		end = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		startB = buf.readBlockPos();
		endB = buf.readBlockPos();
		int count = buf.readInt();
		blocking = new ArrayList<>(count);
		for(int i = 0; i < count; ++i)
			blocking.add(buf.readBlockPos());
		wireType = WireType.getValue(buf.readString(100));
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeDouble(start.x).writeDouble(start.y).writeDouble(start.z);
		buf.writeDouble(end.x).writeDouble(end.y).writeDouble(end.z);
		buf.writeBlockPos(startB);
		buf.writeBlockPos(endB);
		buf.writeInt(blocking.size());
		for(BlockPos b : blocking)
			buf.writeBlockPos(b);
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