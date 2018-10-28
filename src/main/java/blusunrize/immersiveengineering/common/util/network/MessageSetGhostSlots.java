/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.gui.IESlot.Ghost;
import blusunrize.immersiveengineering.common.util.IELogger;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSetGhostSlots implements IMessage
{
	private Int2ObjectMap<ItemStack> stacksToSet;

	public MessageSetGhostSlots(Int2ObjectMap<ItemStack> stacksToSet)
	{
		this.stacksToSet = stacksToSet;
	}

	public MessageSetGhostSlots()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int size = buf.readInt();
		stacksToSet = new Int2ObjectOpenHashMap<>(size);
		for(int i = 0; i < size; i++)
		{
			int slot = buf.readInt();
			NBTTagCompound nbt = ByteBufUtils.readTag(buf);
			assert nbt!=null;
			ItemStack stack = new ItemStack(nbt);
			stacksToSet.put(slot, stack);
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(stacksToSet.size());
		for(Entry<ItemStack> e : stacksToSet.int2ObjectEntrySet())
		{
			buf.writeInt(e.getIntKey());
			NBTTagCompound nbt = new NBTTagCompound();
			e.getValue().writeToNBT(nbt);
			ByteBufUtils.writeTag(buf, nbt);
		}
	}

	public static class Handler implements IMessageHandler<MessageSetGhostSlots, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSetGhostSlots msg, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();
			world.addScheduledTask(() -> {
				Container container = player.openContainer;
				if(container!=null)
					for(Entry<ItemStack> e : msg.stacksToSet.int2ObjectEntrySet())
					{
						int slot = e.getIntKey();
						if(slot >= 0&&slot < container.inventorySlots.size())
						{
							Slot target = container.inventorySlots.get(slot);
							if(!(target instanceof Ghost))
							{
								IELogger.error("Player "+player.getDisplayName()+" tried to set the contents of a non-ghost slot."+
										"This is either a bug in IE or an attempt at cheating.");
								return;
							}
							container.putStackInSlot(slot, e.getValue());
						}
					}
			});
			return null;
		}
	}
}