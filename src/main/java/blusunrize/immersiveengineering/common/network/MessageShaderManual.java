/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.utils.IngredientUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Collection;
import java.util.UUID;

public class MessageShaderManual implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("shader_manual");
	private MessageType key;
	private ResourceLocation[] args;

	public MessageShaderManual(MessageType key, ResourceLocation... args)
	{
		this.key = key;
		this.args = args;
	}

	public MessageShaderManual(FriendlyByteBuf buf)
	{
		this.key = MessageType.values()[buf.readInt()];
		int l = buf.readInt();
		args = new ResourceLocation[l];
		for(int i = 0; i < l; i++)
			args[i] = new ResourceLocation(buf.readUtf(1000));
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(this.key.ordinal());
		if(args!=null)
		{
			buf.writeInt(this.args.length);
			for(ResourceLocation rl : args)
				buf.writeUtf(rl.toString());
		}
		else
			buf.writeInt(0);
	}

	public enum MessageType
	{
		SYNC,
		UNLOCK,
		SPAWN
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		if(context.flow().getReceptionSide()==LogicalSide.SERVER)
		{
			ServerPlayer player = serverPlayer(context);
			UUID playerId = player.getUUID();
			context.workHandler().execute(() -> {
				if(key==MessageType.SYNC)
				{
					Collection<ResourceLocation> received = ShaderRegistry.receivedShaders.get(playerId);
					ResourceLocation[] ss = received.toArray(new ResourceLocation[0]);
					PacketDistributor.PLAYER.with(player).send(new MessageShaderManual(MessageType.SYNC, ss));
				}
				else if(key==MessageType.UNLOCK&&args.length > 0)
				{
					ShaderRegistry.receivedShaders.put(playerId, args[0]);
				}
				else if(key==MessageType.SPAWN&&args.length > 0)
				{
					if(!player.getAbilities().instabuild)
						IngredientUtils.consumePlayerIngredient(player, ShaderRegistry.shaderRegistry.get(args[0]).replicationCost.get());
					ItemStack shaderStack = new ItemStack(ShaderRegistry.itemShader);
					ItemNBTHelper.putString(shaderStack, "shader_name", args[0].toString());
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
			context.workHandler().execute(() -> {
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
	public ResourceLocation id()
	{
		return ID;
	}
}