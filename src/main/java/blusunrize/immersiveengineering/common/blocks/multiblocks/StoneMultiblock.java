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
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.function.Supplier;

public abstract class StoneMultiblock extends IETemplateMultiblock
{
	public StoneMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Map<Block, ITag<Block>> tags, Supplier<BlockState> baseState)
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

	@Override
	public BlockPos multiblockToModelPos(BlockPos posInMultiblock)
	{
		return super.multiblockToModelPos(new BlockPos(
				getSize().getX()-posInMultiblock.getX()-1,
				posInMultiblock.getY(),
				getSize().getZ()-posInMultiblock.getZ()-1
		));
	}
}
