/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback;

import blusunrize.immersiveengineering.client.models.obj.callback.block.BlockCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.item.ItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class DefaultCallback<T> implements ItemCallback<T>, BlockCallback<T>
{
	public static final DefaultCallback<?> INSTANCE = new DefaultCallback<>();

	@Override
	public T extractKey(ItemStack stack, LivingEntity owner)
	{
		return null;
	}

	@Override
	public T extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> DefaultCallback<T> cast()
	{
		return (DefaultCallback<T>)INSTANCE;
	}
}
