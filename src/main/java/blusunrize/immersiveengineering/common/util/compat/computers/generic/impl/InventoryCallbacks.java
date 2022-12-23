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
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InventoryCallbacks<T> extends Callback<T>
{
	private final BiFunction<T, Integer, ItemStack> getStack;
	private final int slotCount;
	private final String desc;

	public InventoryCallbacks(BiFunction<T, Integer, ItemStack> getStack, int slotCount, String desc)
	{
		this.getStack = getStack;
		this.slotCount = slotCount;
		this.desc = desc;
	}

	public static <T> InventoryCallbacks<T> fromList(
			Function<T, List<ItemStack>> getAllStacks, int begin, int count, String desc
	)
	{
		return new InventoryCallbacks<>((t, i) -> getAllStacks.apply(t).get(begin+i), count, desc);
	}

	public static <T> InventoryCallbacks<T> fromHandler(
			Function<T, IItemHandler> getInv, int begin, int count, String desc
	)
	{
		return new InventoryCallbacks<>((t, i) -> getInv.apply(t).getStackInSlot(begin+i), count, desc);
	}

	@Override
	public String renameMethod(String javaName)
	{
		return javaName.replace("Desc", capitalize(desc));
	}

	@ComputerCallable
	public int getDescSlotCount(CallbackEnvironment<T> env)
	{
		return slotCount;
	}

	@ComputerCallable
	public ItemStack getDescStack(CallbackEnvironment<T> env, @IndexArgument int index)
	{
		if(index < 0||index >= slotCount)
			throw new RuntimeException("Index is out of bounds, only "+slotCount+" "+desc+" slots are available");
		else
			return getStack.apply(env.object(), index);
	}
}
