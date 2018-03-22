/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEItems;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collection;

public class MessageShaderManual implements IMessage
{
	MessageType key;
	String[] args;
	public MessageShaderManual(MessageType key, String... args)
	{
		this.key = key;
		this.args = args;
	}
	public MessageShaderManual()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.key = MessageType.values()[buf.readInt()];
		int l = buf.readInt();
		args = new String[l];
		for(int i=0; i<l; i++)
			args[i] = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.key.ordinal());
		if(args!=null)
		{
			buf.writeInt(this.args.length);
			for(String s : args)
				ByteBufUtils.writeUTF8String(buf, s);
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

	public static class HandlerServer implements IMessageHandler<MessageShaderManual, IMessage>
	{
		@Override
		public IMessage onMessage(MessageShaderManual message, MessageContext ctx)
		{
			if(message.key==MessageType.SYNC && message.args.length>0)
			{
				Collection<String> received = ShaderRegistry.receivedShaders.get(message.args[0]);
				String[] ss = received.toArray(new String[received.size()+1]);
				System.arraycopy(ss,0, ss,1, ss.length-1);
				ss[0] = message.args[0];
				ImmersiveEngineering.packetHandler.sendTo(new MessageShaderManual(MessageType.SYNC,ss), ctx.getServerHandler().player);
			}
			else if(message.key==MessageType.UNLOCK && message.args.length>1)
			{
				ShaderRegistry.receivedShaders.put(message.args[0], message.args[1]);
			}
			else if(message.key==MessageType.SPAWN && message.args.length>1)
			{
				EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(message.args[0]);
				if(!player.capabilities.isCreativeMode)
					ApiUtils.consumePlayerIngredient(player, ShaderRegistry.shaderRegistry.get(message.args[1]).replicationCost);
				ItemStack shaderStack = new ItemStack(IEItems.shader);
				ItemNBTHelper.setString(shaderStack, "shader_name", message.args[1]);
				EntityItem entityitem = player.dropItem(shaderStack, false);
				if(entityitem != null)
				{
					entityitem.setNoPickupDelay();
					entityitem.setOwner(player.getName());
				}
			}
			return null;
		}
	}
	public static class HandlerClient implements IMessageHandler<MessageShaderManual, IMessage>
	{
		@Override
		public IMessage onMessage(MessageShaderManual message, MessageContext ctx)
		{
			if(message.key==MessageType.SYNC && message.args.length>0)
			{
				String name = message.args[0];
				for(int i=1; i<message.args.length; i++)
					if(message.args[i]!=null)
						ShaderRegistry.receivedShaders.put(name, message.args[i]);
			}
			return null;
		}
	}
}