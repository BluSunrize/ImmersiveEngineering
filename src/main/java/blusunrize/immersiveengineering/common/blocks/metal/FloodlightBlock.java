/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class FloodlightBlock extends MiscConnectorBlock<FloodlightTileEntity>
{

	public FloodlightBlock(String name)
	{
		super(name, IETileTypes.FLOODLIGHT, IEProperties.ACTIVE, IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
	{
		return state.get(IEProperties.ACTIVE)?15: 0;
	}
}
