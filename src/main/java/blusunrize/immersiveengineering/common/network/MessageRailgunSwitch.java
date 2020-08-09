/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageRailgunSwitch implements IMessage
{
	private Hand hand;

	public MessageRailgunSwitch(Hand hand)
	{
		this.hand = hand;
	}

	public MessageRailgunSwitch(PacketBuffer buf)
	{
		this.hand = Hand.values()[buf.readShort()];
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeShort(this.hand.ordinal());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ServerPlayerEntity player = ctx.getSender();
		assert player!=null;
		ctx.enqueueWork(() -> {
			ItemStack equipped = player.getHeldItem(this.hand);
			if(equipped.getItem() instanceof RailgunItem)
				RailgunItem.nextAmmo(equipped, player);
		});
	}
}