/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.util.ChatUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageNoSpamChatComponents implements IMessage
{
	private ITextComponent[] chatMessages;

	public MessageNoSpamChatComponents(ITextComponent... chatMessages)
	{
		this.chatMessages = chatMessages;
	}

	public MessageNoSpamChatComponents(PacketBuffer buf)
	{
		int l = buf.readInt();
		chatMessages = new ITextComponent[l];
		for(int i = 0; i < l; i++)
			chatMessages[i] = ITextComponent.Serializer.getComponentFromJson(buf.readString(1000));
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(chatMessages.length);
		for(ITextComponent component : chatMessages)
			buf.writeString(ITextComponent.Serializer.toJson(component));
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> ChatUtils.sendClientNoSpamMessages(chatMessages));
	}
}