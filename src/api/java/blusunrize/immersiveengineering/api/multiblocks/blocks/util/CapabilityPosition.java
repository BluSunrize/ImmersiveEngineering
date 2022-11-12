package blusunrize.immersiveengineering.api.multiblocks.blocks.util;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public record CapabilityPosition(BlockPos posInMultiblock, @Nullable RelativeBlockFace side)
{
	public CapabilityPosition(int x, int y, int z, @Nullable RelativeBlockFace side)
	{
		this(new BlockPos(x, y, z), side);
	}

	public boolean equalsOrNullFace(CapabilityPosition other)
	{
		return other.side==null||equals(other);
	}
}
