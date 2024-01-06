/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageScrollwheelItem implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("scrollwheel_item");
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
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBoolean(this.forward);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		Player player = context.player().orElseThrow();
		context.workHandler().execute(() -> {
			ItemStack equipped = player.getItemInHand(InteractionHand.MAIN_HAND);
			if(equipped.getItem() instanceof IScrollwheel)
				((IScrollwheel)equipped.getItem()).onScrollwheel(equipped, player, forward);
		});
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}