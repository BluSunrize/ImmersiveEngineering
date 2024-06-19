/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PipeValveBlock extends IEEntityBlock<PipeValveBlockEntity>
{
	public PipeValveBlock(Properties blockProps)
	{
		super(IEBlockEntities.PIPE_VALVE, blockProps);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.WATERLOGGED);
		builder.add(IEProperties.FACING_ALL);
		builder.add(IEProperties.UP);
		builder.add(IEProperties.DOWN);
		builder.add(IEProperties.NORTH);
		builder.add(IEProperties.SOUTH);
		builder.add(IEProperties.WEST);
		builder.add(IEProperties.EAST);
	}

	@Override
	@Deprecated
	public VoxelShape getBlockSupportShape(BlockState state, BlockGetter getter, BlockPos pos)
	{
		return Shapes.block();
	}

	@Override
	@Deprecated
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, world, pos, block, fromPos, isMoving);
		if (pos.relative(state.getValue(IEProperties.FACING_ALL)).equals(fromPos)||
			pos.relative(state.getValue(IEProperties.FACING_ALL).getOpposite()).equals(fromPos)) return;
		else
			setConnectorFromDirection(state, world, pos, fromPos, world.getBlockState(fromPos).isSignalSource());

	}

	private static void setConnectorFromDirection(BlockState state, Level world, BlockPos pos, BlockPos fromPos, boolean source)
	{
		for(Direction dir : Direction.values())
			if(pos.relative(dir).equals(fromPos))
				switch(dir)
				{
					case UP -> world.setBlockAndUpdate(pos, state.setValue(IEProperties.UP, source));
					case DOWN -> world.setBlockAndUpdate(pos, state.setValue(IEProperties.DOWN, source));
					case NORTH -> world.setBlockAndUpdate(pos, state.setValue(IEProperties.NORTH, source));
					case SOUTH -> world.setBlockAndUpdate(pos, state.setValue(IEProperties.SOUTH, source));
					case WEST -> world.setBlockAndUpdate(pos, state.setValue(IEProperties.WEST, source));
					case EAST -> world.setBlockAndUpdate(pos, state.setValue(IEProperties.EAST, source));
				}
	}
}
