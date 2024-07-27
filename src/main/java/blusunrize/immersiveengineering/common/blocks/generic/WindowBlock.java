/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WindowBlock extends IEBaseBlock
{
	private static final VoxelShape X_FACING = Block.box(6.75, 0, 0, 9.25, 16, 16);
	private static final VoxelShape Y_FACING = Block.box(0, 6.75, 0, 16, 9.25, 16);
	private static final VoxelShape Z_FACING = Block.box(0, 0, 6.75, 16, 16, 9.25);

	public WindowBlock(Properties blockProps)
	{
		super(blockProps);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_ALL);
	}

	protected Direction getDefaultFacing()
	{
		return Direction.NORTH;
	}

	@Override
	protected BlockState getInitDefaultState()
	{
		BlockState ret = super.getInitDefaultState();
		return ret.setValue(IEProperties.FACING_ALL, getDefaultFacing());
	}

	public BlockState getStateForPlacement(BlockPlaceContext pContext)
	{
		return this.defaultBlockState().setValue(IEProperties.FACING_ALL, pContext.getNearestLookingDirection());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		Direction facing = state.getValue(IEProperties.FACING_ALL);
		return switch(facing.getAxis())
		{
			case X -> X_FACING;
			case Y -> Y_FACING;
			case Z -> Z_FACING;
		};
	}
}
