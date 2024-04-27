/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageContainerUpdate(int windowId, CompoundTag nbt) implements IMessage
{
	public static final Type<MessageContainerUpdate> ID = IMessage.createType("container_update");
	public static final StreamCodec<ByteBuf, MessageContainerUpdate> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MessageContainerUpdate::windowId,
			ByteBufCodecs.COMPOUND_TAG, MessageContainerUpdate::nbt,
			MessageContainerUpdate::new
	);

	@Override
	public void process(IPayloadContext context)
	{
		ServerPlayer player = IMessage.serverPlayer(context);
		context.enqueueWork(() -> {
			player.resetLastActionTime();
			if(player.containerMenu.containerId==windowId&&player.containerMenu instanceof IEContainerMenu ieMenu)
				ieMenu.receiveMessageFromScreen(nbt);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}