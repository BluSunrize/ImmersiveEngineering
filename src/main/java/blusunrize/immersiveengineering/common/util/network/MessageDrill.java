package blusunrize.immersiveengineering.common.util.network;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;

import blusunrize.immersiveengineering.common.items.ItemDrill;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class MessageDrill implements IMessage
{
	String player;
	byte val;
	public MessageDrill(String p, byte v)
	{
		val = v;
		player = p;
	}
	public MessageDrill()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		val = buf.readByte();
		player = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeByte(val);
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
					ItemDrill.animationTimer = new HashMap<>();
				ItemDrill.animationTimer.put(message.player, (int)message.val);
			}
			return null;
		}
	}
}