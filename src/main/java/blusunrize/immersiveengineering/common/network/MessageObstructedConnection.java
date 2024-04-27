/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.client.LevelStageRenders;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Collection;

public record MessageObstructedConnection(
		Collection<BlockPos> blocking, SyncedConnection connection
) implements IMessage
{
	public static final Type<MessageObstructedConnection> ID = IMessage.createType("obstructed_connection");
	public static final StreamCodec<ByteBuf, MessageObstructedConnection> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), MessageObstructedConnection::blocking,
			SyncedConnection.CODEC, MessageObstructedConnection::connection,
			MessageObstructedConnection::new
	);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> LevelStageRenders.FAILED_CONNECTIONS.put(
				connection.toConnection(), Pair.of(blocking, new MutableInt(200))
		));
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}