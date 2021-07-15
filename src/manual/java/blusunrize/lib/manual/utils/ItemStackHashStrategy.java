/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.utils;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemStackHashStrategy implements Hash.Strategy<ItemStack>
{
	public static final Hash.Strategy<ItemStack> INSTANCE = new ItemStackHashStrategy();

	@Override
	public int hashCode(ItemStack stack)
	{
		if(stack.isEmpty())
			return 0;
		int ret = System.identityHashCode(stack.getItem());
		ret = ret*31+Objects.hashCode(stack.getTag());
		return ret;
	}

	@Override
	public boolean equals(ItemStack a, ItemStack b)
	{
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		return a.getItem()==b.getItem()&&Objects.equals(a.getTag(), b.getTag());
	}
}
