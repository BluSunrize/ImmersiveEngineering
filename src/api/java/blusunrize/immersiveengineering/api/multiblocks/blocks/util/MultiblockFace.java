/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.util;

import net.minecraft.core.BlockPos;

public record MultiblockFace(RelativeBlockFace face, BlockPos posInMultiblock)
{
	public MultiblockFace(int x, int y, int z, RelativeBlockFace face)
	{
		this(face, new BlockPos(x, y, z));
	}
}
