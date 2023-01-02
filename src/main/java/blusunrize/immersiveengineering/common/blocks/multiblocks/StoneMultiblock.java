/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.register.IEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public abstract class StoneMultiblock extends IETemplateMultiblock
{
	public StoneMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, IEBlocks.BlockEntry<?> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, size, baseState);
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
	public BlockPos multiblockToModelPos(BlockPos posInMultiblock, @Nonnull Level level)
	{
		return super.multiblockToModelPos(new BlockPos(
				getSize(level).getX()-posInMultiblock.getX()-1,
				posInMultiblock.getY(),
				getSize(level).getZ()-posInMultiblock.getZ()-1
		), level);
	}
}
