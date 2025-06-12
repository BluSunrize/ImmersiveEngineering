/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.tool.upgrade.PrevSlot;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeData;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.UpgradeableToolItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
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
				UpgradeData upgrades = UpgradeableToolItem.getUpgradesStatic(s);
				if(s.getItem() instanceof IEShieldItem&&upgrades.has(UpgradeEffect.MAGNET))
				{
					var withSlot = upgrades.with(UpgradeEffect.MAGNET, new PrevSlot(fetchSlot));
					s.set(IEDataComponents.UPGRADE_DATA, withSlot);
					player.getInventory().items.set(fetchSlot, held);
					player.setItemInHand(InteractionHand.OFF_HAND, s);
				}
			}
			else
			{
				UpgradeData upgrades = UpgradeableToolItem.getUpgradesStatic(held);
				var prevSlot = upgrades.get(UpgradeEffect.MAGNET).prevSlot();
				if(held.getItem() instanceof IEShieldItem&&((IEShieldItem)held.getItem()).getUpgrades(held).has(UpgradeEffect.MAGNET)&&prevSlot.isPresent())
				{
					var withSlot = upgrades.with(UpgradeEffect.MAGNET, PrevSlot.NONE);
					held.set(IEDataComponents.UPGRADE_DATA, withSlot);
					ItemStack s = player.getInventory().items.get(prevSlot.get());
					player.getInventory().items.set(prevSlot.get(), held);
					player.setItemInHand(InteractionHand.OFF_HAND, s);
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