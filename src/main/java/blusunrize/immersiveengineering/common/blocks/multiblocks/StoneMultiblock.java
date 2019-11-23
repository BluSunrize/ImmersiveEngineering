/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.function.Supplier;

public abstract class StoneMultiblock extends IETemplateMultiblock
{
	public StoneMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Map<Block, Tag<Block>> tags, Supplier<BlockState> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, tags, baseState);
	}

	public StoneMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Supplier<BlockState> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, baseState);
	}

	@Override
	public boolean canBeMirrored()
	{
		return false;
	}

	@Override
	public Direction transformDirection(Direction original)
	{
		return original.getOpposite();
	}

	@Override
	public Direction untransformDirection(Direction transformed)
	{
		return transformed.getOpposite();
	}
}
