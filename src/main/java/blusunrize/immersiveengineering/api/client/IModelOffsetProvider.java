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

/**
 * Implement in either block or tile entity when using split models. Returning (x, y, z) means that the model
 * intersected with [x, x+1)x[y, y+1)x[z, z+1) will be used for this block.
 */
public interface IModelOffsetProvider
{
	BlockPos getModelOffset(BlockState state);
}
