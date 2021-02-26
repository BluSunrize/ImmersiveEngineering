/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.client;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nullable;

/**
 * Implement in either block or tile entity when using split models. Returning
 */
public interface IModelOffsetProvider
{
	@Deprecated
	default BlockPos getModelOffset(BlockState state)
	{
		return getModelOffset(state, null);
	}

	/**
	 * @param size Size of the bounding box of the parts of the model registered in the JSON file
	 * @return (x, y, z) to use the model intersected with [x, x+1)x[y, y+1)x[z, z+1) for this block.
	 */
	default BlockPos getModelOffset(BlockState state, @Nullable Vector3i size)
	{
		return getModelOffset(state);
	}
}
