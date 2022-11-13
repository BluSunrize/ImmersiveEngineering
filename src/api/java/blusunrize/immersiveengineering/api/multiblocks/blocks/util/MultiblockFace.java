package blusunrize.immersiveengineering.api.multiblocks.blocks.util;

import net.minecraft.core.BlockPos;

public record MultiblockFace(RelativeBlockFace face, BlockPos posInMultiblock)
{
	public MultiblockFace(int x, int y, int z, RelativeBlockFace face)
	{
		this(face, new BlockPos(x, y, z));
	}
}
