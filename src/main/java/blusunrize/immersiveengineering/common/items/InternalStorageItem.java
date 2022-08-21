/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class InternalStorageItem extends IEBaseItem
{

	public InternalStorageItem(Properties props)
	{
		super(props);
	}

	public abstract int getSlotCount();

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack);
		return null;
	}

	public void setContainedItems(ItemStack stack, NonNullList<ItemStack> inventory)
	{
		LazyOptional<IItemHandler> lazyHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
		lazyHandler.ifPresent(handler-> {
			if(handler instanceof IItemHandlerModifiable)
			{
				if(inventory.size()!=handler.getSlots())
					throw new IllegalArgumentException("Parameter inventory has "+inventory.size()+" slots, capability inventory has "+handler.getSlots());
				for(int i = 0; i < handler.getSlots(); i++)
					((IItemHandlerModifiable)handler).setStackInSlot(i, inventory.get(i));
			}
			else
				IELogger.warn("No valid inventory handler found for "+stack);
		});
	}

	public NonNullList<ItemStack> getContainedItems(ItemStack stack)
	{
		LazyOptional<IItemHandler> lazyHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
		Optional<NonNullList<ItemStack>> ret = lazyHandler.map(handler -> {
			if(handler instanceof IEItemStackHandler)
				return ((IEItemStackHandler)handler).getContainedItems();
			else
			{
				IELogger.warn("Inefficiently getting contained items. Why does "+stack+" have a non-IE IItemHandler?");
				NonNullList<ItemStack> inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
				for(int i = 0; i < handler.getSlots(); i++)
					inv.set(i, handler.getStackInSlot(i));
				return inv;
			}
		});
		return ret.orElse(NonNullList.create());
	}
}
