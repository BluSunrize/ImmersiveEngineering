/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageBirthdayParty implements IMessage
{
	int entityId;

	public MessageBirthdayParty(EntityLivingBase entity)
	{
		this.entityId = entity.getEntityId();
	}

	public MessageBirthdayParty()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.entityId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.entityId);
	}

	public static class HandlerClient implements IMessageHandler<MessageBirthdayParty, IMessage>
	{
		@Override
		public IMessage onMessage(MessageBirthdayParty message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = ImmersiveEngineering.proxy.getClientWorld();
				if (world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					Entity entity = world.getEntityByID(message.entityId);
					if(entity!=null&&entity instanceof EntityLivingBase)
					{
						world.makeFireworks(entity.posX, entity.posY, entity.posZ, 0, 0, 0, Utils.getRandomFireworkExplosion(Utils.RAND, 4));
						entity.getEntityData().setBoolean("headshot", true);
					}
				}
			});
			return null;
		}
	}
}