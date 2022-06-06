/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public interface IDropInventory
{
	Stream<ItemStack> getDroppedItems();
}
