/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.RevolverItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageRevolverRotate implements IMessage
{
	private boolean forward;

	public MessageRevolverRotate(boolean forward)
	{
		this.forward = forward;
	}

	public MessageRevolverRotate(PacketBuffer buf)
	{
		this.forward = buf.readBoolean();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeBoolean(this.forward);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ServerPlayerEntity player = ctx.getSender();
		assert player!=null;
		ctx.enqueueWork(() -> {
			ItemStack equipped = player.getHeldItem(Hand.MAIN_HAND);
			if(equipped.getItem() instanceof RevolverItem)
				((RevolverItem)equipped.getItem()).rotateCylinder(equipped, player, forward);
		});
	}
}