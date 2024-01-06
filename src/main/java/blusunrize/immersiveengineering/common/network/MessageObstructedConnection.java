/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.LevelStageRenders;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Collection;

public class MessageObstructedConnection implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("obstructed_connection");
	private final Vec3 start, end;
	private final BlockPos startB, endB;
	private final Collection<BlockPos> blocking;
	private final WireType wireType;

	public MessageObstructedConnection(Connection conn, Collection<BlockPos> blocking)
	{
		this.blocking = blocking;
		start = conn.getEndAOffset();
		end = conn.getEndBOffset();
		startB = conn.getEndA().position();
		endB = conn.getEndB().position();
		wireType = conn.type;
	}

	public MessageObstructedConnection(FriendlyByteBuf buf)
	{
		start = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		end = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		startB = buf.readBlockPos();
		endB = buf.readBlockPos();
		int count = buf.readInt();
		blocking = new ArrayList<>(count);
		for(int i = 0; i < count; ++i)
			blocking.add(buf.readBlockPos());
		wireType = WireType.getValue(buf.readUtf(100));
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeDouble(start.x).writeDouble(start.y).writeDouble(start.z);
		buf.writeDouble(end.x).writeDouble(end.y).writeDouble(end.z);
		buf.writeBlockPos(startB);
		buf.writeBlockPos(endB);
		buf.writeInt(blocking.size());
		for(BlockPos b : blocking)
			buf.writeBlockPos(b);
		buf.writeUtf(wireType.getUniqueName());
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		context.workHandler().execute(() -> {
			Connection conn = new Connection(
					wireType, new ConnectionPoint(startB, 0), new ConnectionPoint(endB, 0), start, end
			);
			LevelStageRenders.FAILED_CONNECTIONS.put(conn, Pair.of(blocking, new MutableInt(200)));
		});
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}