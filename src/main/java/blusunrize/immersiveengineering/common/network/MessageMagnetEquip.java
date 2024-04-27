/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.IEShieldItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageMagnetEquip(int fetchSlot) implements IMessage
{
	public static final Type<MessageMagnetEquip> ID = IMessage.createType("magnet_equip");
	public static final StreamCodec<ByteBuf, MessageMagnetEquip> CODEC = ByteBufCodecs.INT
			.map(MessageMagnetEquip::new, MessageMagnetEquip::fetchSlot);

	@Override
	public void process(IPayloadContext context)
	{
		Player player = context.player();
		context.enqueueWork(() -> {
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
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}