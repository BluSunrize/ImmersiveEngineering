/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * @author BluSunrize - 27.10.2015
 * <p>
 * An item that contains an internal inventory, like drill or revolver
 * Deprecated in favor of capabilities
 */
@Deprecated
public interface IInternalStorageItem
{
	NonNullList<ItemStack> getContainedItems(ItemStack stack);

	void setContainedItems(ItemStack stack, NonNullList<ItemStack> stackList);

	int getInternalSlots(ItemStack stack);
}