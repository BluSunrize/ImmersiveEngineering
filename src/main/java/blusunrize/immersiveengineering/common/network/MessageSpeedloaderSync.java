/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageSpeedloaderSync implements IMessage
{
	private int slot;
	private InteractionHand hand;

	public MessageSpeedloaderSync(int slot, InteractionHand hand)
	{
		this.slot = slot;
		this.hand = hand;
	}

	public MessageSpeedloaderSync(FriendlyByteBuf buf)
	{
		slot = buf.readByte();
		hand = InteractionHand.values()[buf.readByte()];
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeByte(slot);
		buf.writeByte(hand.ordinal());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			Player player = ImmersiveEngineering.proxy.getClientPlayer();
			if(player!=null)
			{
				if(player.getItemInHand(hand).getItem() instanceof RevolverItem)
				{
					player.playSound(IESounds.revolverReload, 1f, 1f);
					ItemNBTHelper.putInt(player.getItemInHand(hand), "reload", 60);
				}
				// TODO this is a speedloader dupe!
				player.getInventory().setItem(slot, new ItemStack(Weapons.speedloader));
			}
		});
	}
}