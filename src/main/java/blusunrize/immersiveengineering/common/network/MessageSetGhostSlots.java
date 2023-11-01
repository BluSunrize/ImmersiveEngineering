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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent.Context;

public class MessageSetGhostSlots implements IMessage
{
	private final Int2ObjectMap<ItemStack> stacksToSet;

	public MessageSetGhostSlots(Int2ObjectMap<ItemStack> stacksToSet)
	{
		this.stacksToSet = stacksToSet;
	}

	public MessageSetGhostSlots(FriendlyByteBuf buf)
	{
		int size = buf.readInt();
		stacksToSet = new Int2ObjectOpenHashMap<>(size);
		for(int i = 0; i < size; i++)
		{
			int slot = buf.readInt();
			stacksToSet.put(slot, buf.readItem());
		}
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(stacksToSet.size());
		for(Entry<ItemStack> e : stacksToSet.int2ObjectEntrySet())
		{
			buf.writeInt(e.getIntKey());
			buf.writeItem(e.getValue());
		}
	}

	@Override
	public void process(Context context)
	{
		ServerPlayer player = context.getSender();
		assert player!=null;
		context.enqueueWork(() -> {
			AbstractContainerMenu container = player.containerMenu;
			if(container!=null)
				for(Entry<ItemStack> e : stacksToSet.int2ObjectEntrySet())
				{
					int slot = e.getIntKey();
					if(slot >= 0&&slot < container.slots.size())
					{
						Slot target = container.slots.get(slot);
						if(!(target instanceof ItemHandlerGhost))
						{
							IELogger.error("Player "+player.getDisplayName()+" tried to set the contents of a non-ghost slot."+
									"This is either a bug in IE or an attempt at cheating.");
							return;
						}
						//TODO this is most likely broken!
						container.setItem(slot, container.getStateId(), e.getValue());
					}
				}
		});
	}
}