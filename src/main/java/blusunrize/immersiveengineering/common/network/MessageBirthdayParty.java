/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageBirthdayParty implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("birthday_party");
	int entityId;

	public MessageBirthdayParty(LivingEntity entity)
	{
		this.entityId = entity.getId();
	}

	public MessageBirthdayParty(FriendlyByteBuf buf)
	{
		entityId = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(this.entityId);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		context.workHandler().execute(() -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null) // This can happen if the task is scheduled right before leaving the world
			{
				Entity entity = world.getEntity(entityId);
				if(entity!=null&&entity instanceof LivingEntity)
				{
					world.createFireworks(entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0, Utils.getRandomFireworkExplosion(ApiUtils.RANDOM, 4));
					entity.getPersistentData().putBoolean("headshot", true);
				}
			}
		});
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}