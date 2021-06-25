/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

import java.util.function.Supplier;

public class BlockIESlab<T extends Block & IIEBlock> extends SlabBlock implements IIEBlock
{
	private final Supplier<T> base;

	public BlockIESlab(Properties props, Supplier<T> base)
	{
		super(props.setSuffocates(causesSuffocation(base)).setOpaque(isNormalCube(base)));
		this.base = base;
	}

	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
	{
		double relativeEntityPosition = entity.getPositionVec().getY()-pos.getY();
		switch(state.get(SlabBlock.TYPE))
		{
			case TOP:
				return 0.5 < relativeEntityPosition&&relativeEntityPosition < 1;
			case BOTTOM:
				return 0 < relativeEntityPosition&&relativeEntityPosition < 0.5;
			case DOUBLE:
				return true;
		}
		return false;
	}

	@Override
	public boolean hasFlavour()
	{
		return base.get().hasFlavour();
	}

	@Override
	public String getNameForFlavour()
	{
		return base.get().getNameForFlavour();
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return Math.min(base.get().getOpacity(state, worldIn, pos), super.getOpacity(state, worldIn, pos));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
	{
		return super.propagatesSkylightDown(state, reader, pos)||base.get().propagatesSkylightDown(state, reader, pos);
	}

	public static AbstractBlock.IPositionPredicate causesSuffocation(Supplier<? extends Block> base)
	{
		return (state, world, pos) ->
			base.get().getDefaultState().isSuffocating(world, pos) && state.get(TYPE) == SlabType.DOUBLE;
	}

	public static AbstractBlock.IPositionPredicate isNormalCube(Supplier<? extends Block> base)
	{
		return (state, world, pos) ->
				base.get().getDefaultState().isNormalCube(world, pos) && state.get(TYPE) == SlabType.DOUBLE;
	}
}
