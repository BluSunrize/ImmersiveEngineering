package blusunrize.immersiveengineering.common.util.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.util.IELogger;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class MessageDrill implements IMessage
{
	String player;
	boolean start;
	public MessageDrill(String p, boolean s)
	{
		start = s;
		player = p;
	}
	public MessageDrill()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		start = buf.readBoolean();
		player = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(start);
		ByteBufUtils.writeUTF8String(buf, player);
	}

	public static class Handler implements IMessageHandler<MessageDrill, IMessage>
	{
		@Override
		public IMessage onMessage(MessageDrill message, MessageContext ctx)
		{
			if (FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
			{
				if (ItemDrill.animationTimer==null)
					ItemDrill.animationTimer = new ConcurrentHashMap<>();
				if (message.start)
					synchronized (ItemDrill.animationTimer)
					{
						ItemDrill.animationTimer.put(message.player, 40);
					}
				else if (!Minecraft.getMinecraft().isIntegratedServerRunning())
					synchronized (ItemDrill.animationTimer)
					{
						ItemDrill.animationTimer.put(message.player, 15);
					}
			}
			return null;
		}
	}
}