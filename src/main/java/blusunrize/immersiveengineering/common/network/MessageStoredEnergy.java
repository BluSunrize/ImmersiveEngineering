/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.VoltmeterItem;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteEnergyData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageStoredEnergy(VoltmeterItem.RemoteEnergyData data) implements IMessage
{
	public static final Type<MessageStoredEnergy> ID = IMessage.createType("stored_energy");
	public static final StreamCodec<ByteBuf, MessageStoredEnergy> CODEC = RemoteEnergyData.CODEC
			.map(MessageStoredEnergy::new, MessageStoredEnergy::data);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> VoltmeterItem.lastEnergyUpdate = data);
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}