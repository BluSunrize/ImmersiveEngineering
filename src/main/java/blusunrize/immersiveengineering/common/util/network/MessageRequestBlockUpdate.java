package blusunrize.immersiveengineering.common.util.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageRequestBlockUpdate implements IMessage
{
	int dim;
	BlockPos pos;
	public MessageRequestBlockUpdate(int dimension, BlockPos pos)
	{
		this.pos = pos;
		dim = dimension;
	}
	public MessageRequestBlockUpdate()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		dim = buf.readInt();
		pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(dim).writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
	}

	public static class Handler implements IMessageHandler<MessageRequestBlockUpdate, IMessage>
	{
		@Override
		public IMessage onMessage(MessageRequestBlockUpdate message, MessageContext ctx)
		{
			if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
			{
				World w = MinecraftServer.getServer().worldServerForDimension(message.dim);
				if(w!=null)
				{
					w.markBlockForUpdate(message.pos);
				}
			}
			return null;
		}
	}
}