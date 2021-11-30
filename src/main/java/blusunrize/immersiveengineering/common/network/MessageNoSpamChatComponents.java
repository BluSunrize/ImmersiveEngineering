/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.util.ChatUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageNoSpamChatComponents implements IMessage
{
	private Component[] chatMessages;

	public MessageNoSpamChatComponents(Component... chatMessages)
	{
		this.chatMessages = chatMessages;
	}

	public MessageNoSpamChatComponents(FriendlyByteBuf buf)
	{
		int l = buf.readInt();
		chatMessages = new Component[l];
		for(int i = 0; i < l; i++)
			chatMessages[i] = Component.Serializer.fromJson(buf.readUtf(1000));
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(chatMessages.length);
		for(Component component : chatMessages)
			buf.writeUtf(Component.Serializer.toJson(component));
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> ChatUtils.sendClientNoSpamMessages(chatMessages));
	}
}