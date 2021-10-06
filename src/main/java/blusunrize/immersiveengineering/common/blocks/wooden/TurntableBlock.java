/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;

public class TurntableBlock extends IETileProviderBlock<TurntableTileEntity>
{
	public TurntableBlock(String name)
	{
		super(name, IETileTypes.TURNTABLE, Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2, 5),
				BlockItemIE::new);
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
			((TurntableTileEntity)world.getBlockEntity(pos)).verticalTransitionRotationMap(state.getValue(IEProperties.FACING_ALL), newState.getValue(IEProperties.FACING_ALL));
		super.onRemove(state, world, pos, newState, isMoving);
	}
}
