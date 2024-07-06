/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.utils.IECodecs;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record SyncedConnection(
		ConnectionPoint start, ConnectionPoint end, WireType type, Vec3 offsetStart, Vec3 offsetEnd
)
{
	public static final StreamCodec<ByteBuf, SyncedConnection> CODEC = StreamCodec.composite(
			ConnectionPoint.CODECS.streamCodec(), SyncedConnection::start,
			ConnectionPoint.CODECS.streamCodec(), SyncedConnection::end,
			WireType.STREAM_CODEC, SyncedConnection::type,
			IECodecs.VEC3_STREAM_CODEC, SyncedConnection::offsetStart,
			IECodecs.VEC3_STREAM_CODEC, SyncedConnection::offsetEnd,
			SyncedConnection::new
	);

	public SyncedConnection(Connection connection)
	{
		this(
				connection.getEndA(), connection.getEndB(),
				connection.type,
				connection.getEndAOffset(), connection.getEndBOffset()
		);
	}

	public Connection toConnection()
	{
		return new Connection(type, start, end, offsetStart, offsetEnd);
	}
}
