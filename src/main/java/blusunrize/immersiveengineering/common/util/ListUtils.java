/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by codew on 4/04/2017.
 */
public final class ListUtils
{
	private ListUtils()
	{
	}

	public static NonNullList<ItemStack> fromItems(List<ItemStack> stackList)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(stackList);
		return list;
	}

	public static <T> Collector<T, ?, NonNullList<T>> collector()
	{
		return Collectors.toCollection(NonNullList::create);
	}
}
