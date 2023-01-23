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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record MessageNoSpamChat(Component message) implements IMessage
{
	public MessageNoSpamChat(FriendlyByteBuf buffer)
	{
		this(buffer.readComponent());
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeComponent(message);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			final ChatComponent chat = Minecraft.getInstance().gui.getChat();
			final ChatComponentAccess chatAccess = ((ChatComponentAccess)chat);
			final List<GuiMessage> allMessages = chatAccess.getAllMessages();
			allMessages.removeIf(guiMessage -> Objects.equals(guiMessage.signature(), ChatUtils.NO_SPAM_SIGNATURE));
			chatAccess.invokeRefreshTrimmedMessage();
			chat.addMessage(message, ChatUtils.NO_SPAM_SIGNATURE, null);
		});
	}
}
