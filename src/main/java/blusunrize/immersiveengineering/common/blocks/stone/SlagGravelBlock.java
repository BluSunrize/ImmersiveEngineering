/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class SlagGravelBlock extends FallingBlock
{
	public SlagGravelBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected MapCodec<? extends FallingBlock> codec()
	{
		return MapCodec.unit(this);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state)
	{
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand)
	{
		BlockPos posAbove = pos.above();
		BlockState stateAbove = level.getBlockState(posAbove);
		BlockPos posTopPlant = null;
		while(stateAbove.getFluidState().is(FluidTags.WATER)&&stateAbove.getBlock() instanceof BonemealableBlock)
		{
			posTopPlant = posAbove;
			posAbove = posAbove.above();
			stateAbove = level.getBlockState(posAbove);
		}
		if(posTopPlant!=null)
			level.getBlockState(posTopPlant).randomTick(level, posTopPlant, rand);
	}

}
