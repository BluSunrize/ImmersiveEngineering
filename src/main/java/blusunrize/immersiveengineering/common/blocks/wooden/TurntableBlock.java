/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TurntableBlock extends IETileProviderBlock
{
	public TurntableBlock(String name)
	{
		super(name, Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2, 5),
				BlockItemIE::new, IEProperties.FACING_ALL);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return new TurntableTileEntity();
	}

	@Override
	public boolean canProvidePower(BlockState state)
	{
		return false;
	}


	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if(state.func_235901_b_(IEProperties.FACING_ALL) && newState.func_235901_b_(IEProperties.FACING_ALL))
			((TurntableTileEntity)world.getTileEntity(pos)).verticalTransitionRotationMap(state.get(IEProperties.FACING_ALL), newState.get(IEProperties.FACING_ALL));
		super.onReplaced(state, world, pos, newState, isMoving);
	}
}
