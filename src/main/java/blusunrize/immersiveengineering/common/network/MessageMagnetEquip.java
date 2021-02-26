/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.IEShieldItem;
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
		Context ctx = context.get();
		ServerPlayerEntity player = ctx.getSender();
		assert player!=null;
		ctx.enqueueWork(() -> {
			ItemStack held = player.getHeldItem(Hand.OFF_HAND);
			if(fetchSlot >= 0)
			{
				ItemStack s = player.inventory.mainInventory.get(fetchSlot);
				if(!s.isEmpty()&&s.getItem() instanceof IEShieldItem&&((IEShieldItem)s.getItem()).getUpgrades(s).getBoolean("magnet"))
				{
					((IEShieldItem)s.getItem()).getUpgrades(s).putInt("prevSlot", fetchSlot);
					player.inventory.mainInventory.set(fetchSlot, held);
					player.setHeldItem(Hand.OFF_HAND, s);
				}
			}
			else
			{
				if(held.getItem() instanceof IEShieldItem&&((IEShieldItem)held.getItem()).getUpgrades(held).getBoolean("magnet"))
				{
					int prevSlot = ((IEShieldItem)held.getItem()).getUpgrades(held).getInt("prevSlot");
					ItemStack s = player.inventory.mainInventory.get(prevSlot);
					player.inventory.mainInventory.set(prevSlot, held);
					player.setHeldItem(Hand.OFF_HAND, s);
					((IEShieldItem)held.getItem()).getUpgrades(held).remove("prevSlot");
				}
			}
		});
	}
}