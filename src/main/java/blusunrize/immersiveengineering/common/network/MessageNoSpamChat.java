package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;

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
			chat.deleteMessage(ChatUtils.NO_SPAM_SIGNATURE);
			chat.addMessage(message, ChatUtils.NO_SPAM_SIGNATURE, null);
		});
	}
}
