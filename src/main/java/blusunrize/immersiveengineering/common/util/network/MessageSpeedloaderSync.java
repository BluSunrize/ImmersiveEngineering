/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSpeedloaderSync implements IMessage
{
	int slot;
	EnumHand hand;

	public MessageSpeedloaderSync(int slot, EnumHand hand)
	{
		this.slot = slot;
		this.hand = hand;
	}

	public MessageSpeedloaderSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		slot = buf.readByte();
		hand = EnumHand.values()[buf.readByte()];
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeByte(slot);
		buf.writeByte(hand.ordinal());
	}

	public static class Handler implements IMessageHandler<MessageSpeedloaderSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSpeedloaderSync message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityPlayer player = ImmersiveEngineering.proxy.getClientPlayer();
				if (player!=null)
				{
					if(player.getHeldItem(message.hand).getItem() instanceof ItemRevolver)
					{
						player.playSound(IESounds.revolverReload, 1f, 1f);
						ItemNBTHelper.setInt(player.getHeldItem(message.hand), "reload", 60);
					}
					player.inventory.setInventorySlotContents(message.slot, new ItemStack(IEContent.itemSpeedloader));
				}
			});
			return null;
		}
	}
}