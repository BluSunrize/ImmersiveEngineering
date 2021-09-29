/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class TurntableBlock extends IEEntityBlock<TurntableBlockEntity>
{
	public TurntableBlock(Properties props)
	{
		super(IEBlockEntities.TURNTABLE, props);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_ALL);
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return false;
	}


	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if(state.hasProperty(IEProperties.FACING_ALL) && newState.hasProperty(IEProperties.FACING_ALL))
			((TurntableBlockEntity)world.getBlockEntity(pos)).verticalTransitionRotationMap(state.getValue(IEProperties.FACING_ALL), newState.getValue(IEProperties.FACING_ALL));
		super.onRemove(state, world, pos, newState, isMoving);
	}
}
