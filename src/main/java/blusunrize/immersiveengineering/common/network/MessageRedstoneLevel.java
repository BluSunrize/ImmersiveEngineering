/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.VoltmeterItem;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteRedstoneData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageRedstoneLevel(VoltmeterItem.RemoteRedstoneData data) implements IMessage
{
	public static final Type<MessageRedstoneLevel> ID = IMessage.createType("redstone_level");
	public static final StreamCodec<ByteBuf, MessageRedstoneLevel> CODEC = RemoteRedstoneData.STREAM_CODEC
			.map(MessageRedstoneLevel::new, MessageRedstoneLevel::data);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> VoltmeterItem.lastRedstoneUpdate = data);
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
