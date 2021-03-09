package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public class SingleItemCallback<T> extends Callback<T>
{
	private final Function<T, ItemStack> getStack;
	private final String desc;

	public SingleItemCallback(Function<T, ItemStack> getStack, String desc)
	{
		this.getStack = getStack;
		this.desc = desc;
	}

	public SingleItemCallback(Function<T, List<ItemStack>> getStack, int index, String desc)
	{
		this(getStack.andThen(l -> l.get(index)), desc);
	}

	@Override
	public String renameMethod(String javaName)
	{
		return javaName.replace("Desc", capitalize(desc));
	}

	@ComputerCallable
	public ItemStack getDesc(CallbackEnvironment<T> env)
	{
		return getStack.apply(env.getObject());
	}
}
