/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class MessageMineralListSync implements IMessage
{
	HashMap<MineralMix, Integer> map = new HashMap<MineralMix, Integer>();

	public MessageMineralListSync(HashMap<MineralMix, Integer> map)
	{
		this.map = map;
	}

	public MessageMineralListSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int size = buf.readInt();
		for(int i = 0; i < size; i++)
		{
			NBTTagCompound tag = ByteBufUtils.readTag(buf);
			MineralMix mix = MineralMix.readFromNBT(tag);
			if(mix!=null)
				map.put(mix, tag.getInteger("weight"));
		}

	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(map.size());
		for(Map.Entry<MineralMix, Integer> e : map.entrySet())
		{
			NBTTagCompound tag = e.getKey().writeToNBT();
			tag.setInteger("weight", e.getValue());
			ByteBufUtils.writeTag(buf, tag);
		}
	}

	public static class Handler implements IMessageHandler<MessageMineralListSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMineralListSync message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> onMessageMain(message));
			return null;
		}

		private void onMessageMain(MessageMineralListSync message)
		{
			ExcavatorHandler.mineralList.clear();
			for(MineralMix min : message.map.keySet())
				ExcavatorHandler.mineralList.put(min, message.map.get(min));
			ClientProxy.handleMineralManual();
		}
	}
}