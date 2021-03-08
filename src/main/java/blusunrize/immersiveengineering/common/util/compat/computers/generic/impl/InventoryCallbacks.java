package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import net.minecraft.item.ItemStack;

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
		String uppercaseDesc = Character.toUpperCase(desc.charAt(0))+desc.substring(1);
		return javaName.replace("Desc", uppercaseDesc);
	}

	@ComputerCallable
	public int getDescSlotCount(CallbackEnvironment<T> env)
	{
		return getStacks.apply(env.getObject()).size();
	}

	@ComputerCallable
	public ItemStack getDescStack(CallbackEnvironment<T> env, int index)
	{
		--index; // 1-based indexing
		List<ItemStack> stacks = getStacks.apply(env.getObject());
		if(index < 0||index >= stacks.size())
			throw new RuntimeException("Index is out of bounds, only "+stacks.size()+" "+desc+" slots are available");
		else
			return stacks.get(index);
	}
}
