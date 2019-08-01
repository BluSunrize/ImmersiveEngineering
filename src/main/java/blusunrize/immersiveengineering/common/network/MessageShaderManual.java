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
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public class MessageShaderManual implements IMessage
{
	private MessageType key;
	private String[] args;

	public MessageShaderManual(MessageType key, String... args)
	{
		this.key = key;
		this.args = args;
	}

	public MessageShaderManual(PacketBuffer buf)
	{
		this.key = MessageType.values()[buf.readInt()];
		int l = buf.readInt();
		args = new String[l];
		for(int i = 0; i < l; i++)
			args[i] = buf.readString(1000);
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(this.key.ordinal());
		if(args!=null)
		{
			buf.writeInt(this.args.length);
			for(String s : args)
				buf.writeString(s);
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
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		if(ctx.getDirection().getReceptionSide()==LogicalSide.SERVER)
		{
			ServerPlayerEntity player = ctx.getSender();
			assert player!=null;
			UUID playerId = player.getUniqueID();
			ctx.enqueueWork(() -> {
				if(key==MessageType.SYNC)
				{
					Collection<String> received = ShaderRegistry.receivedShaders.get(playerId);
					String[] ss = received.toArray(new String[0]);
					ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> player),
							new MessageShaderManual(MessageType.SYNC, ss));
				}
				else if(key==MessageType.UNLOCK&&args.length > 0)
				{
					ShaderRegistry.receivedShaders.put(playerId, args[0]);
				}
				else if(key==MessageType.SPAWN&&args.length > 0)
				{
					if(!player.abilities.isCreativeMode)
						ApiUtils.consumePlayerIngredient(player, ShaderRegistry.shaderRegistry.get(args[0]).replicationCost);
					ItemStack shaderStack = new ItemStack(ShaderRegistry.itemShader);
					ItemNBTHelper.putString(shaderStack, "shader_name", args[0]);
					ItemEntity entityitem = player.dropItem(shaderStack, false);
					if(entityitem!=null)
					{
						entityitem.setNoPickupDelay();
						entityitem.setOwnerId(player.getUniqueID());
					}
				}
			});
		}
		else
			ctx.enqueueWork(() -> {
				if(key==MessageType.SYNC)
				{
					PlayerEntity player = ImmersiveEngineering.proxy.getClientPlayer();
					if(player!=null)
					{
						UUID name = player.getUniqueID();
						for(String shader : args)
							if(shader!=null)
								ShaderRegistry.receivedShaders.put(name, shader);
					}
				}
			});
	}
}