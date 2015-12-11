package blusunrize.immersiveengineering.common.util.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class MessageRequestBlockUpdate implements IMessage
{
	int x, y, z, dim;
	public MessageRequestBlockUpdate(int x, int y, int z, int dimension)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		dim = dimension;
	}
	public MessageRequestBlockUpdate()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		dim = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(x).writeInt(y).writeInt(z).writeInt(dim);
	}

	public static class Handler implements IMessageHandler<MessageRequestBlockUpdate, IMessage>
	{
		@Override
		public IMessage onMessage(MessageRequestBlockUpdate message, MessageContext ctx)
		{
			if (FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
			{
				World w = MinecraftServer.getServer().worldServerForDimension(message.dim);
				if (w!=null)
				{
					w.markBlockForUpdate(message.x, message.y, message.z);
				}
			}
			return null;
		}
	}
}