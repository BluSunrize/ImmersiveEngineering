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
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class MessageRequestBlockUpdate implements IMessage
{
	BlockPos pos;

	public MessageRequestBlockUpdate(BlockPos pos)
	{
		this.pos = pos;
	}

	public MessageRequestBlockUpdate()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
	}

	public static class Handler implements IMessageHandler<MessageRequestBlockUpdate, IMessage>
	{
		@Override
		public IMessage onMessage(MessageRequestBlockUpdate message, MessageContext ctx)
		{
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			world.addScheduledTask(() -> {
				if(world.isBlockLoaded(message.pos))
				{
					int dim = world.provider.getDimension();
					EventHandler.requestedBlockUpdates.offer(new ImmutablePair<>(dim, message.pos));
				}
			});
			return null;
		}
	}
}