/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.ComponentItemHandler;

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
			InternalStorageItem item = (InternalStorageItem)stack.getItem();
			return new ComponentItemHandler(stack, IEDataComponents.GENERIC_ITEMS.get(), item.getSlotCount());
		});
	}

	public void setContainedItems(ItemStack stack, NonNullList<ItemStack> inventory)
	{
		stack.set(IEDataComponents.GENERIC_ITEMS, ItemContainerContents.fromItems(inventory));
	}

	public static ItemContainerContents getContainedItems(ItemStack stack)
	{
		return stack.getOrDefault(IEDataComponents.GENERIC_ITEMS, ItemContainerContents.EMPTY);
	}
}
