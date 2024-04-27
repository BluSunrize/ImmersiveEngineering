/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers.DataPair;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record MessageContainerData(List<Pair<Integer, DataPair<?>>> synced) implements IMessage
{
	public static final Type<MessageContainerData> ID = IMessage.createType("container_data");
	private static final StreamCodec<RegistryFriendlyByteBuf, Pair<Integer, DataPair<?>>> PAIR_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, Pair::getFirst,
			DataPair.CODEC, Pair::getSecond,
			Pair::new
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, MessageContainerData> CODEC = PAIR_CODEC
			.apply(ByteBufCodecs.list())
			.map(MessageContainerData::new, MessageContainerData::synced);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			AbstractContainerMenu currentContainer = ImmersiveEngineering.proxy.getClientPlayer().containerMenu;
			if(currentContainer instanceof IEContainerMenu ieContainer)
				ieContainer.receiveSync(synced);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
