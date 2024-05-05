/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.utils.IngredientUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record MessageShaderManual(MessageType key, List<ResourceLocation> args) implements IMessage
{
	public static final Type<MessageShaderManual> ID = IMessage.createType("shader_manual");
	public static final StreamCodec<ByteBuf, MessageShaderManual> CODEC = StreamCodec.composite(
			ByteBufCodecs.idMapper(i -> MessageType.values()[i], MessageType::ordinal), MessageShaderManual::key,
			ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), MessageShaderManual::args,
			MessageShaderManual::new
	);

	public MessageShaderManual(MessageType key, ResourceLocation... args)
	{
		this(key, Arrays.asList(args));
	}

	public enum MessageType
	{
		SYNC,
		UNLOCK,
		SPAWN
	}

	@Override
	public void process(IPayloadContext context)
	{
		if(context.flow().getReceptionSide()==LogicalSide.SERVER)
		{
			ServerPlayer player = IMessage.serverPlayer(context);
			UUID playerId = player.getUUID();
			context.enqueueWork(() -> {
				if(key==MessageType.SYNC)
				{
					Collection<ResourceLocation> received = ShaderRegistry.receivedShaders.get(playerId);
					ResourceLocation[] ss = received.toArray(new ResourceLocation[0]);
					PacketDistributor.sendToPlayer(player, new MessageShaderManual(MessageType.SYNC, ss));
				}
				else if(key==MessageType.UNLOCK&&!args.isEmpty())
				{
					ShaderRegistry.receivedShaders.put(playerId, args.get(0));
				}
				else if(key==MessageType.SPAWN&&!args.isEmpty())
				{
					if(!player.getAbilities().instabuild)
						IngredientUtils.consumePlayerIngredient(player, ShaderRegistry.shaderRegistry.get(args.get(0)).replicationCost.get());
					ItemStack shaderStack = new ItemStack(ShaderRegistry.itemShader);
					ItemNBTHelper.putString(shaderStack, "shader_name", args.get(0).toString());
					ItemEntity entityitem = player.drop(shaderStack, false);
					if(entityitem!=null)
					{
						entityitem.setNoPickUpDelay();
						entityitem.setThrower(player);
					}
				}
			});
		}
		else
			context.enqueueWork(() -> {
				if(key==MessageType.SYNC)
				{
					Player player = ImmersiveEngineering.proxy.getClientPlayer();
					if(player!=null)
					{
						UUID name = player.getUUID();
						for(ResourceLocation shader : args)
							if(shader!=null)
								ShaderRegistry.receivedShaders.put(name, shader);
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