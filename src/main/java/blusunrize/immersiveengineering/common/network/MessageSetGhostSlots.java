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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageSetGhostSlots implements IMessage
{
	private final Int2ObjectMap<ItemStack> stacksToSet;

	public MessageSetGhostSlots(Int2ObjectMap<ItemStack> stacksToSet)
	{
		this.stacksToSet = stacksToSet;
	}

	public MessageSetGhostSlots(PacketBuffer buf)
	{
		int size = buf.readInt();
		stacksToSet = new Int2ObjectOpenHashMap<>(size);
		for(int i = 0; i < size; i++)
		{
			int slot = buf.readInt();
			stacksToSet.put(slot, buf.readItemStack());
		}
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(stacksToSet.size());
		for(Entry<ItemStack> e : stacksToSet.int2ObjectEntrySet())
		{
			buf.writeInt(e.getIntKey());
			buf.writeItemStack(e.getValue());
		}
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ServerPlayerEntity player = ctx.getSender();
		assert player!=null;
		ctx.enqueueWork(() -> {
			Container container = player.openContainer;
			if(container!=null)
				for(Entry<ItemStack> e : stacksToSet.int2ObjectEntrySet())
				{
					int slot = e.getIntKey();
					if(slot >= 0&&slot < container.inventorySlots.size())
					{
						Slot target = container.inventorySlots.get(slot);
						if(!(target instanceof ItemHandlerGhost))
						{
							IELogger.error("Player "+player.getDisplayName()+" tried to set the contents of a non-ghost slot."+
									"This is either a bug in IE or an attempt at cheating.");
							return;
						}
						container.putStackInSlot(slot, e.getValue());
					}
				}
		});
	}
}