/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageOpenManual() implements IMessage
{
	public static final Type<MessageOpenManual> ID = IMessage.createType("open_manual");
	public static final StreamCodec<ByteBuf, MessageOpenManual> CODEC = StreamCodec.unit(new MessageOpenManual());

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(ImmersiveEngineering.proxy::openManual);
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
