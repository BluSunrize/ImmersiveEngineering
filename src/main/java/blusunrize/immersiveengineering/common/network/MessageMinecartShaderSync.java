/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.client.render.entity.ShaderMinecartRenderer;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record MessageMinecartShaderSync(int entityID, Optional<ResourceLocation> shader) implements IMessage
{
	public static final Type<MessageMinecartShaderSync> ID = IMessage.createType("minecart_shader_sync");
	public static final StreamCodec<RegistryFriendlyByteBuf, MessageMinecartShaderSync> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MessageMinecartShaderSync::entityID,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), MessageMinecartShaderSync::shader,
			MessageMinecartShaderSync::new
	);

	@Override
	public void process(IPayloadContext context)
	{
		if(context.flow().getReceptionSide()==LogicalSide.SERVER)
		{
			Level world = context.player().level();
			context.enqueueWork(() -> {
				Entity entity = world.getEntity(entityID);
				if(!(entity instanceof AbstractMinecart))
					return;
				ShaderWrapper cap = entity.getData(IEDataAttachments.MINECART_SHADER);
				if(cap!=null)
					PacketDistributor.sendToPlayersInDimension(
							(ServerLevel)world, new MessageMinecartShaderSync(entity.getId(), Optional.ofNullable(cap.getShader()))
					);
			});
		}
		else
			context.enqueueWork(() -> {
				Level world = ImmersiveEngineering.proxy.getClientWorld();
				if (world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					Entity entity = world.getEntity(entityID);
					if(entity instanceof AbstractMinecart)
						ShaderMinecartRenderer.shadedCarts.put(entityID, shader.orElse(null));
				}
			});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}