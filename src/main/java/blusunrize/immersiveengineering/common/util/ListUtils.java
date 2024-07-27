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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public static NonNullList<ItemStack> fromStream(Stream<ItemStack> data, int exactSize)
	{
		var result = data
				.limit(exactSize)
				.collect(Collectors.toCollection(NonNullList::create));
		while(result.size() < exactSize)
			result.add(ItemStack.EMPTY);
		return result;
	}
}
