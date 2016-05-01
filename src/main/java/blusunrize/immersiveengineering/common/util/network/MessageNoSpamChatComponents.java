package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.util.ChatUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageNoSpamChatComponents implements IMessage
{
	IChatComponent[] chatMessages;
	public MessageNoSpamChatComponents(IChatComponent... chatMessages)
	{
		this.chatMessages = chatMessages;
	}
	public MessageNoSpamChatComponents()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int l = buf.readInt();
		chatMessages = new IChatComponent[l];
		for(int i=0; i<l; i++)
			chatMessages[i] = IChatComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(chatMessages.length);
		for(IChatComponent component : chatMessages)
			ByteBufUtils.writeUTF8String(buf, IChatComponent.Serializer.componentToJson(component));
	}

	public static class Handler implements IMessageHandler<MessageNoSpamChatComponents, IMessage>
	{
		@Override
		public IMessage onMessage(MessageNoSpamChatComponents message, MessageContext ctx)
		{
			ChatUtils.sendClientNoSpamMessages(message.chatMessages);
			return null;
		}
	}
}