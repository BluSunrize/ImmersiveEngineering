/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent.Context;

public class MessageScrollwheelItem implements IMessage
{
	private final boolean forward;

	public MessageScrollwheelItem(boolean forward)
	{
		this.forward = forward;
	}

	public MessageScrollwheelItem(FriendlyByteBuf buf)
	{
		this.forward = buf.readBoolean();
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeBoolean(this.forward);
	}

	@Override
	public void process(Context context)
	{
		ServerPlayer player = context.getSender();
		assert player!=null;
		context.enqueueWork(() -> {
			ItemStack equipped = player.getItemInHand(InteractionHand.MAIN_HAND);
			if(equipped.getItem() instanceof IScrollwheel)
				((IScrollwheel)equipped.getItem()).onScrollwheel(equipped, player, forward);
		});
	}
}