/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static net.minecraft.network.protocol.PacketFlow.CLIENTBOUND;

public class MessageDieselToolAttack implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("diesel_tool_attack");
	private final int holderID;

	public MessageDieselToolAttack(LivingEntity holder)
	{
		this.holderID = holder.getId();
	}

	public MessageDieselToolAttack(FriendlyByteBuf buf)
	{
		this.holderID = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(holderID);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		assert context.flow().equals(CLIENTBOUND); //todo: remove me?

//		context.workHandler().execute(() -> {
//			Level world = ImmersiveEngineering.proxy.getClientWorld();
//			if(world!=null) // This can happen if the task is scheduled right before leaving the world
//			{
//				Entity entity = world.getEntity(entityID);
//				if(entity instanceof LivingEntity holder)
//					DieselToolSoundHandler.handleAttackAction(holder, action, targetPos);
//			}
//		});
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
