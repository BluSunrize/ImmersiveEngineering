/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.gui.IESlot.ItemHandlerGhost;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public record MessageSetGhostSlots(Map<Integer, ItemStack> stacksToSet) implements IMessage
{
	public static final Type<MessageSetGhostSlots> ID = IMessage.createType("set_ghost_slot");
	private static final StreamCodec<RegistryFriendlyByteBuf, Map<Integer, ItemStack>> MAP_CODEC = ByteBufCodecs.map(
			HashMap::new, ByteBufCodecs.INT, ItemStack.OPTIONAL_STREAM_CODEC
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, MessageSetGhostSlots> CODEC = MAP_CODEC
			.map(MessageSetGhostSlots::new, MessageSetGhostSlots::stacksToSet);

	@Override
	public void process(IPayloadContext context)
	{
		Player player = context.player();
		context.enqueueWork(() -> {
			AbstractContainerMenu container = player.containerMenu;
			for(Entry<Integer, ItemStack> e : stacksToSet.entrySet())
			{
				int slot = e.getKey();
				if(slot >= 0&&slot < container.slots.size())
				{
					Slot target = container.slots.get(slot);
					if(!(target instanceof ItemHandlerGhost))
					{
						IELogger.error("Player "+player.getDisplayName()+" tried to set the contents of a non-ghost slot."+
								"This is either a bug in IE or an attempt at cheating.");
						return;
					}
					container.setItem(slot, container.getStateId(), e.getValue());
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