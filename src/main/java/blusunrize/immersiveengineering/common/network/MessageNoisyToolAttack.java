/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.sound.NoisyToolSoundHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageNoisyToolAttack(int noisyToolHolderID) implements IMessage
{
	public static final Type<MessageNoisyToolAttack> ID = IMessage.createType("noisy_tool_attack");
	public static final StreamCodec<ByteBuf, MessageNoisyToolAttack> CODEC = ByteBufCodecs.INT
			.map(MessageNoisyToolAttack::new, MessageNoisyToolAttack::noisyToolHolderID);

	public MessageNoisyToolAttack(LivingEntity holder)
	{
		this(holder.getId());
	}

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null)
			{
				Entity entity = world.getEntity(noisyToolHolderID);
				if(entity instanceof LivingEntity noisyToolHolder)
					NoisyToolSoundHandler.handleAttack(noisyToolHolder);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
