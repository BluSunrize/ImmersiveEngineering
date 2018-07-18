/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IIEInventory
{
	NonNullList<ItemStack> getInventory();

	boolean isStackValid(int slot, ItemStack stack);

	int getSlotLimit(int slot);

	void doGraphicalUpdates(int slot);

	default NonNullList<ItemStack> getDroppedItems()
	{
		return getInventory();
	}

	default int getComparatedSize()
	{
		return getInventory()!=null?getInventory().size(): 0;
	}
}
