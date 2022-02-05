/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public class InventoryCallbacks<T> extends Callback<T>
{
	private final Function<T, List<ItemStack>> getStacks;
	private final String desc;

	public InventoryCallbacks(Function<T, List<ItemStack>> getStacks, String desc)
	{
		this.getStacks = getStacks;
		this.desc = desc;
	}

	public InventoryCallbacks(
			Function<T, List<ItemStack>> getAllStacks, int begin, int count, String desc
	)
	{
		this(getAllStacks.andThen(l -> l.subList(begin, begin+count)), desc);
	}

	@Override
	public String renameMethod(String javaName)
	{
		return javaName.replace("Desc", capitalize(desc));
	}

	@ComputerCallable
	public int getDescSlotCount(CallbackEnvironment<T> env)
	{
		return getStacks.apply(env.object()).size();
	}

	@ComputerCallable
	public ItemStack getDescStack(CallbackEnvironment<T> env, @IndexArgument int index)
	{
		List<ItemStack> stacks = getStacks.apply(env.object());
		if(index < 0||index >= stacks.size())
			throw new RuntimeException("Index is out of bounds, only "+stacks.size()+" "+desc+" slots are available");
		else
			return stacks.get(index);
	}
}
