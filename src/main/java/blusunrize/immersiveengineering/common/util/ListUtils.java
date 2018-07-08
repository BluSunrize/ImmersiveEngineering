/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

/**
 * Created by codew on 4/04/2017.
 */
public final class ListUtils
{
	public static NonNullList<ItemStack> fromItem(ItemStack itemStack)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		if(itemStack!=null)
		{
			list.add(0, itemStack);
		}
		return list;
	}

	public static NonNullList<ItemStack> fromItems(ItemStack... itemStack)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		for(int i = 0; i < itemStack.length; i++)
		{
			list.add(i, itemStack[i]!=null?(itemStack[i]): ItemStack.EMPTY);
		}
		return list;
	}

	private ListUtils()
	{
	}

	public static NonNullList<ItemStack> fromItems(List<ItemStack> stackList)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		for(ItemStack itemStack : stackList)
		{
			list.add(itemStack);
		}
		return list;
	}
}
