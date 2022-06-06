/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface IIEInventory extends IDropInventory
{
	@Nullable
	NonNullList<ItemStack> getInventory();

	boolean isStackValid(int slot, ItemStack stack);

	int getSlotLimit(int slot);

	void doGraphicalUpdates();

	@Override
	default Stream<ItemStack> getDroppedItems()
	{
		return getInventory()!=null?getInventory().stream(): Stream.of();
	}

	default int getComparatedSize()
	{
		return getInventory()!=null?getInventory().size(): 0;
	}
}
