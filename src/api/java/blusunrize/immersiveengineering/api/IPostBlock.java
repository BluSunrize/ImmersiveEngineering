/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

/**
 * Implemented on blocks that can have a transformer 'attached' to them (for example, wooden post).
 */
public interface IPostBlock
{
	/**
	 * Returns true if a transformer should render attached to this post
	 */
	boolean canConnectTransformer(BlockGetter world, BlockPos pos);
}
