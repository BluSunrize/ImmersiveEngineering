/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSkyhookSync implements IMessage
{
	int entityID;
	Connection connection;
	double linePos;

	public MessageSkyhookSync(EntitySkylineHook entity)
	{
		entityID = entity.getEntityId();
		connection = entity.getConnection();
		linePos = entity.linePos;
	}

	public MessageSkyhookSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		entityID = buf.readInt();
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		connection = Connection.readFromNBT(tag);
		linePos = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(entityID);
		ByteBufUtils.writeTag(buf, connection.writeToNBT());
		buf.writeDouble(linePos);
	}

	public static class Handler implements IMessageHandler<MessageSkyhookSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSkyhookSync message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = ImmersiveEngineering.proxy.getClientWorld();
				if(world!=null)
				{
					Entity ent = world.getEntityByID(message.entityID);
					if(ent instanceof EntitySkylineHook)
					{
						message.connection.getSubVertices(world);
						((EntitySkylineHook)ent).setConnectionAndPos(message.connection, message.linePos);
					}
				}
			});
			return null;
		}
	}
}