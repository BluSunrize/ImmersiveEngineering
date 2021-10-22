/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemGetterList
{
	private final List<Function<LivingEntity, ItemStack>> getters = new ArrayList<>();

	public ItemGetterList(Function<LivingEntity, ItemStack> defaultGetter)
	{
		getters.add(defaultGetter);
	}

	public void addGetter(Function<LivingEntity, ItemStack> getter)
	{
		getters.add(getter);
	}

	public ItemStack getFrom(LivingEntity player)
	{
		for(Function<LivingEntity, ItemStack> getter : getters)
		{
			ItemStack fromGetter = getter.apply(player);
			if(!fromGetter.isEmpty())
				return fromGetter;
		}
		return ItemStack.EMPTY;
	}
}
