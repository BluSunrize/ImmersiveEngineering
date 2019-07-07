/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageSpeedloaderSync implements IMessage
{
	int slot;
	Hand hand;

	public MessageSpeedloaderSync(int slot, Hand hand)
	{
		this.slot = slot;
		this.hand = hand;
	}

	public MessageSpeedloaderSync(PacketBuffer buf)
	{
		slot = buf.readByte();
		hand = Hand.values()[buf.readByte()];
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeByte(slot);
		buf.writeByte(hand.ordinal());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Minecraft.getInstance().addScheduledTask(() -> {
			PlayerEntity player = ImmersiveEngineering.proxy.getClientPlayer();
			if(player!=null)
			{
				if(player.getHeldItem(hand).getItem() instanceof ItemRevolver)
				{
					player.playSound(IESounds.revolverReload, 1f, 1f);
					ItemNBTHelper.setInt(player.getHeldItem(hand), "reload", 60);
				}
				player.inventory.setInventorySlotContents(slot, new ItemStack(IEContent.itemSpeedloader));
			}
		});
	}
}