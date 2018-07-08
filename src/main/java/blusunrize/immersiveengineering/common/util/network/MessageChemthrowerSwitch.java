/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.items.ItemChemthrower;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChemthrowerSwitch implements IMessage
{
	boolean forward;

	public MessageChemthrowerSwitch(boolean forward)
	{
		this.forward = forward;
	}

	public MessageChemthrowerSwitch()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.forward = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(this.forward);
	}

	public static class Handler implements IMessageHandler<MessageChemthrowerSwitch, IMessage>
	{
		@Override
		public IMessage onMessage(MessageChemthrowerSwitch message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				ItemStack equipped = player.getHeldItem(EnumHand.MAIN_HAND);
				if(equipped.getItem() instanceof ItemChemthrower&&((ItemChemthrower)equipped.getItem()).getUpgrades(equipped).getBoolean("multitank"))
					((ItemChemthrower)equipped.getItem()).switchTank(equipped, message.forward);
			});
			return null;
		}
	}
}