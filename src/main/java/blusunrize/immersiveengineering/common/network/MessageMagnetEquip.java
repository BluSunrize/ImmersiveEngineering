/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageMagnetEquip implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("magnet_equip");
	private int fetchSlot;

	public MessageMagnetEquip(int fetch)
	{
		this.fetchSlot = fetch;
	}

	public MessageMagnetEquip(FriendlyByteBuf buf)
	{
		this.fetchSlot = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(this.fetchSlot);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		Player player = context.player().orElseThrow();
		assert player!=null;
		context.workHandler().execute(() -> {
			ItemStack held = player.getItemInHand(InteractionHand.OFF_HAND);
			if(fetchSlot >= 0)
			{
				ItemStack s = player.getInventory().items.get(fetchSlot);
				if(!s.isEmpty()&&s.getItem() instanceof IEShieldItem&&((IEShieldItem)s.getItem()).getUpgrades(s).getBoolean("magnet"))
				{
					((IEShieldItem)s.getItem()).getUpgrades(s).putInt("prevSlot", fetchSlot);
					player.getInventory().items.set(fetchSlot, held);
					player.setItemInHand(InteractionHand.OFF_HAND, s);
				}
			}
			else
			{
				if(held.getItem() instanceof IEShieldItem&&((IEShieldItem)held.getItem()).getUpgrades(held).getBoolean("magnet"))
				{
					int prevSlot = ((IEShieldItem)held.getItem()).getUpgrades(held).getInt("prevSlot");
					ItemStack s = player.getInventory().items.get(prevSlot);
					player.getInventory().items.set(prevSlot, held);
					player.setItemInHand(InteractionHand.OFF_HAND, s);
					((IEShieldItem)held.getItem()).getUpgrades(held).remove("prevSlot");
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