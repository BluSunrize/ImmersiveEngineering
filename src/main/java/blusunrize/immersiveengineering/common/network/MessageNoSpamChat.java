/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.mixin.accessors.client.ChatComponentAccess;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Objects;

public record MessageNoSpamChat(Component message) implements IMessage
{
	public static final Type<MessageNoSpamChat> ID = IMessage.createType("no_spam_chat");
	public static final StreamCodec<RegistryFriendlyByteBuf, MessageNoSpamChat> CODEC = ComponentSerialization.TRUSTED_STREAM_CODEC
			.map(MessageNoSpamChat::new, MessageNoSpamChat::message);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			final ChatComponent chat = Minecraft.getInstance().gui.getChat();
			final ChatComponentAccess chatAccess = ((ChatComponentAccess)chat);
			final List<GuiMessage> allMessages = chatAccess.getAllMessages();
			allMessages.removeIf(guiMessage -> Objects.equals(guiMessage.signature(), ChatUtils.NO_SPAM_SIGNATURE));
			chatAccess.invokeRefreshTrimmedMessages();
			chat.addMessage(message, ChatUtils.NO_SPAM_SIGNATURE, null);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
