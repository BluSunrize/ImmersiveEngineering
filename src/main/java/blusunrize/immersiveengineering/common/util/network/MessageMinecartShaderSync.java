/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.client.models.ModelShaderMinecart;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMinecartShaderSync implements IMessage
{
	int entityID;
	boolean request = false;
	ItemStack shader;

	public MessageMinecartShaderSync(Entity entity, Object o)
	{
		this.entityID = entity.getEntityId();
		if(o instanceof ShaderWrapper)
			shader = ((ShaderWrapper)o).getShaderItem();
		else
			request = true;
	}

	public MessageMinecartShaderSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.entityID = buf.readInt();
		this.request = buf.readBoolean();
		if(!request)
			this.shader = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.entityID);
		buf.writeBoolean(this.request);
		if(!request)
			ByteBufUtils.writeItemStack(buf, this.shader);
	}

	public static class HandlerServer implements IMessageHandler<MessageMinecartShaderSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMinecartShaderSync message, MessageContext ctx)
		{
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			world.addScheduledTask(() -> {
				Entity entity = world.getEntityByID(message.entityID);
				if(entity!=null&&entity.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
				{
					ShaderWrapper handler = entity.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
					if(handler!=null)
						ImmersiveEngineering.packetHandler.sendToDimension(new MessageMinecartShaderSync(entity, handler), world.provider.getDimension());
				}
			});
			return null;
		}
	}

	public static class HandlerClient implements IMessageHandler<MessageMinecartShaderSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMinecartShaderSync message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = ImmersiveEngineering.proxy.getClientWorld();
				if (world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					Entity entity = world.getEntityByID(message.entityID);
					if(entity instanceof EntityMinecart)
						ModelShaderMinecart.shadedCarts.put(message.entityID, message.shader);
				}
			});
			return null;
		}
	}
}