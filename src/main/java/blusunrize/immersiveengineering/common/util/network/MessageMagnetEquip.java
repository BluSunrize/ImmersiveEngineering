/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.items.ItemIEShield;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMagnetEquip implements IMessage
{
	int fetchSlot;

	public MessageMagnetEquip(int fetch)
	{
		this.fetchSlot = fetch;
	}

	public MessageMagnetEquip()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.fetchSlot = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.fetchSlot);
	}

	public static class Handler implements IMessageHandler<MessageMagnetEquip, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMagnetEquip message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				ItemStack held = player.getHeldItem(EnumHand.OFF_HAND);
				if(message.fetchSlot >= 0)
				{
					ItemStack s = player.inventory.mainInventory.get(message.fetchSlot);
					if(!s.isEmpty()&&s.getItem() instanceof ItemIEShield&&((ItemIEShield)s.getItem()).getUpgrades(s).getBoolean("magnet"))
					{
						((ItemIEShield)s.getItem()).getUpgrades(s).setInteger("prevSlot", message.fetchSlot);
						player.inventory.mainInventory.set(message.fetchSlot, held);
						player.setHeldItem(EnumHand.OFF_HAND, s);
					}
				}
				else
				{
					int prevSlot = ((ItemIEShield)held.getItem()).getUpgrades(held).getInteger("prevSlot");
					ItemStack s = player.inventory.mainInventory.get(prevSlot);
					player.inventory.mainInventory.set(prevSlot, held);
					player.setHeldItem(EnumHand.OFF_HAND, s);
					((ItemIEShield)held.getItem()).getUpgrades(held).removeTag("prevSlot");
				}
			});
			return null;
		}
	}
}