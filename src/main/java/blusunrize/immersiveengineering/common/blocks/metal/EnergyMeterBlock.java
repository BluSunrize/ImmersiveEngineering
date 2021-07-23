/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class EnergyMeterBlock extends MiscConnectableBlock<EnergyMeterTileEntity>
{
	public static final Property<Direction> FACING = IEProperties.FACING_HORIZONTAL;
	public static final Property<Boolean> DUMMY = IEProperties.MULTIBLOCKSLAVE;

	public EnergyMeterBlock(Properties props)
	{
		super(props, IETileTypes.ENERGY_METER);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(DUMMY, FACING, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		Direction newFacing = rot.rotate(state.getValue(FACING));
		return state.setValue(FACING, newFacing);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		if(mirrorIn==Mirror.NONE)
			return state;
		Direction oldFacing = state.getValue(FACING);
		Direction newFacing = mirrorIn.mirror(oldFacing);
		return state.setValue(FACING, newFacing);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction updateSide, BlockState updatedState,
										  LevelAccessor worldIn, BlockPos currentPos, BlockPos updatedPos)
	{
		Direction facing = stateIn.getValue(FACING);
		boolean dummy = stateIn.getValue(DUMMY);
		BlockPos otherHalf = currentPos.above(dummy?-1: 1);
		BlockState otherState = worldIn.getBlockState(otherHalf);
		// Check if current facing is correct, else assume facing of partner
		if(otherState.getBlock()==this)
			if(otherState.getValue(FACING)==facing&&otherState.getValue(DUMMY)==!dummy)
				return stateIn;
			else
				return stateIn.setValue(FACING, otherState.getValue(FACING));
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context)
	{
		return areAllReplaceable(
				context.getClickedPos(),
				context.getClickedPos().above(1),
				context
		);
	}
}
