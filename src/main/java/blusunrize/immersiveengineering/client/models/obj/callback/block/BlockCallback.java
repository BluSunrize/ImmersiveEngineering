/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.client.models.obj.callback.DefaultCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public interface BlockCallback<T> extends IEOBJCallback<T>
{
	T extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity);

	static <T> BlockCallback<T> castOrDefault(IEOBJCallback<T> generic)
	{
		if(generic instanceof BlockCallback<T> blockCB)
			return blockCB;
		else
			return DefaultCallback.cast();
	}
}
