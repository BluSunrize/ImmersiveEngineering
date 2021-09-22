/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback;

import net.minecraft.world.item.ItemStack;

public class DefaultCallback<T> implements ItemCallback<T>//TODO , BlockCallback<Unit>
{
	public static final DefaultCallback<?> INSTANCE = new DefaultCallback<>();

	@Override
	public T extractKey(ItemStack stack)
	{
		throw null;
	}

	@SuppressWarnings("unchecked")
	public static <T> DefaultCallback<T> cast()
	{
		return (DefaultCallback<T>)INSTANCE;
	}
}
