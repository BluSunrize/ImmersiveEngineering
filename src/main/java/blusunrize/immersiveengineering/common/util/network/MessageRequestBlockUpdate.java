/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.EventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;

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
			synchronized (EventHandler.requestedBlockUpdates)
			{
				EventHandler.requestedBlockUpdates.offer(new ImmutablePair<Integer, BlockPos>(message.dim, message.pos));
			}
			return null;
		}
	}
}