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
import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageBirthdayParty(int entityID) implements IMessage
{
	public static final Type<MessageBirthdayParty> ID = IMessage.createType("birthday_party");
	public static final StreamCodec<ByteBuf, MessageBirthdayParty> CODEC = ByteBufCodecs.INT
			.map(MessageBirthdayParty::new, MessageBirthdayParty::entityID);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null) // This can happen if the task is scheduled right before leaving the world
			{
				Entity entity = world.getEntity(entityID);
				if(entity!=null&&entity instanceof LivingEntity)
				{
					world.createFireworks(entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0, Utils.getRandomFireworkExplosion(ApiUtils.RANDOM, 4));
					entity.getPersistentData().putBoolean("headshot", true);
				}
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}