/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageBirthdayParty implements IMessage
{
	int entityId;

	public MessageBirthdayParty(LivingEntity entity)
	{
		this.entityId = entity.getEntityId();
	}

	public MessageBirthdayParty(PacketBuffer buf)
	{
		entityId = buf.readInt();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(this.entityId);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Minecraft.getInstance().addScheduledTask(() -> {
			World world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null) // This can happen if the task is scheduled right before leaving the world
			{
				Entity entity = world.getEntityByID(entityId);
				if(entity!=null&&entity instanceof LivingEntity)
				{
					world.makeFireworks(entity.posX, entity.posY, entity.posZ, 0, 0, 0, Utils.getRandomFireworkExplosion(Utils.RAND, 4));
					entity.getEntityData().putBoolean("headshot", true);
				}
			}
		});
	}
}