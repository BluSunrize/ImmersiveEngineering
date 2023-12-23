/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public abstract class InternalStorageItem extends IEBaseItem
{

	public InternalStorageItem(Properties props)
	{
		super(props);
	}

	public abstract int getSlotCount();

	public static void registerCapabilitiesISI(ItemCapabilityRegistrar registrar)
	{
		registrar.register(ItemHandler.ITEM, stack -> {
			AttachmentType<IEItemStackHandler> key = IEDataAttachments.ITEM_INVENTORY.get();
			InternalStorageItem item = (InternalStorageItem)stack.getItem();
			if(!stack.hasData(key))
				stack.setData(key, new IEItemStackHandler(item, null));
			IEItemStackHandler inventory = stack.getData(key);
			if(inventory.getSlots()!=item.getSlotCount())
			{
				inventory = new IEItemStackHandler(item, inventory);
				stack.setData(key, inventory);
			}
			return inventory;
		});
	}

	public void setContainedItems(ItemStack stack, NonNullList<ItemStack> inventory)
	{
		IItemHandler handler = stack.getCapability(ItemHandler.ITEM);
		if(handler instanceof IItemHandlerModifiable modifiable)
		{
			if(inventory.size()!=modifiable.getSlots())
				throw new IllegalArgumentException("Parameter inventory has "+inventory.size()+" slots, capability inventory has "+modifiable.getSlots());
			for(int i = 0; i < modifiable.getSlots(); i++)
				modifiable.setStackInSlot(i, inventory.get(i));
		}
		else
			IELogger.warn("No valid inventory handler found for "+stack);
	}

	public NonNullList<ItemStack> getContainedItems(ItemStack stack)
	{
		IItemHandler handler = stack.getCapability(ItemHandler.ITEM);
		if(handler!=null)
		{
			if(handler instanceof IEItemStackHandler ieHandler)
				return ieHandler.getContainedItems();
			else
			{
				IELogger.warn("Inefficiently getting contained items. Why does "+stack+" have a non-IE IItemHandler?");
				NonNullList<ItemStack> inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
				for(int i = 0; i < handler.getSlots(); i++)
					inv.set(i, handler.getStackInSlot(i));
				return inv;
			}
		}
		else
			return NonNullList.create();
	}

	public static final IAttachmentSerializer<CompoundTag, IEItemStackHandler> DATA_SERIALIZER = new IAttachmentSerializer<CompoundTag, IEItemStackHandler>()
	{
		@Override
		public IEItemStackHandler read(CompoundTag tag)
		{
			if(tag.isEmpty())
				return null;
			IEItemStackHandler result = new IEItemStackHandler();
			result.deserializeNBT(tag);
			return result;
		}

		@Override
		public CompoundTag write(IEItemStackHandler attachment)
		{
			if(attachment==null)
				return new CompoundTag();
			else
				return attachment.serializeNBT();
		}
	};
}
