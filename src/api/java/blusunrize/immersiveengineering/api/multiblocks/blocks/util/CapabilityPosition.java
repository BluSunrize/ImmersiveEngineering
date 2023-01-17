/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.util;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public record CapabilityPosition(BlockPos posInMultiblock, @Nullable RelativeBlockFace side)
{
	public CapabilityPosition(int x, int y, int z, @Nullable RelativeBlockFace side)
	{
		this(new BlockPos(x, y, z), side);
	}

	public static CapabilityPosition opposing(MultiblockFace multiblockPos)
	{
		return new CapabilityPosition(
				multiblockPos.face().offsetRelative(multiblockPos.posInMultiblock(), 1),
				multiblockPos.face().getOpposite()
		);
	}

	public boolean equalsOrNullFace(CapabilityPosition other)
	{
		return other.side==null||equals(other);
	}
}
