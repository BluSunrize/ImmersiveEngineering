/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageTileSync implements IMessage
{
	BlockPos pos;
	NBTTagCompound nbt;

	public MessageTileSync(TileEntityIEBase tile, NBTTagCompound nbt)
	{
		this.pos = tile.getPos();
		this.nbt = nbt;
	}

	public MessageTileSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
		ByteBufUtils.writeTag(buf, this.nbt);
	}

	public static class HandlerServer implements IMessageHandler<MessageTileSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageTileSync message, MessageContext ctx)
		{
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			world.addScheduledTask(() -> {
				if(world.isBlockLoaded(message.pos))
				{
					TileEntity tile = world.getTileEntity(message.pos);
					if(tile instanceof TileEntityIEBase)
						((TileEntityIEBase)tile).receiveMessageFromClient(message.nbt);
				}
			});
			return null;
		}
	}

	public static class HandlerClient implements IMessageHandler<MessageTileSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageTileSync message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = ImmersiveEngineering.proxy.getClientWorld();
				if (world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					TileEntity tile = world.getTileEntity(message.pos);
					if(tile instanceof TileEntityIEBase)
						((TileEntityIEBase)tile).receiveMessageFromServer(message.nbt);
				}
			});
			return null;
		}
	}
}