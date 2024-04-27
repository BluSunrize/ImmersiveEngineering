/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.utils;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ItemStackHashStrategy implements Hash.Strategy<ItemStack>
{
	public static final Hash.Strategy<ItemStack> INSTANCE = new ItemStackHashStrategy();

	@Override
	public int hashCode(ItemStack stack)
	{
		return ItemStack.hashItemAndComponents(stack);
	}

	@Override
	public boolean equals(ItemStack a, ItemStack b)
	{
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else
			return ItemStack.isSameItemSameComponents(a, b);
	}
}
