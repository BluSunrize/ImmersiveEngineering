/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.ItemIEShield;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageMagnetEquip implements IMessage
{
	private int fetchSlot;

	public MessageMagnetEquip(int fetch)
	{
		this.fetchSlot = fetch;
	}

	public MessageMagnetEquip(PacketBuffer buf)
	{
		this.fetchSlot = buf.readInt();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(this.fetchSlot);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		ServerPlayerEntity player = context.get().getSender();
		assert player!=null;
		player.getServerWorld().addScheduledTask(() -> {
			ItemStack held = player.getHeldItem(Hand.OFF_HAND);
			if(fetchSlot >= 0)
			{
				ItemStack s = player.inventory.mainInventory.get(fetchSlot);
				if(!s.isEmpty()&&s.getItem() instanceof ItemIEShield&&((ItemIEShield)s.getItem()).getUpgrades(s).getBoolean("magnet"))
				{
					((ItemIEShield)s.getItem()).getUpgrades(s).setInt("prevSlot", fetchSlot);
					player.inventory.mainInventory.set(fetchSlot, held);
					player.setHeldItem(Hand.OFF_HAND, s);
				}
			}
			else
			{
				int prevSlot = ((ItemIEShield)held.getItem()).getUpgrades(held).getInt("prevSlot");
				ItemStack s = player.inventory.mainInventory.get(prevSlot);
				player.inventory.mainInventory.set(prevSlot, held);
				player.setHeldItem(Hand.OFF_HAND, s);
				((ItemIEShield)held.getItem()).getUpgrades(held).removeTag("prevSlot");
			}
		});
	}
}