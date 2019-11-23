/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * @author BluSunrize - 12.10.2019
 */
public class ScaffoldingSlabBlock extends BlockIESlab
{
	public ScaffoldingSlabBlock(String name, Properties props, Class<? extends BlockItem> itemBlock, boolean isSlab)
	{
		super(name, props, itemBlock, isSlab);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return 0;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer)
	{
		return layer==BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
	{
		return true;
	}

	@Override
	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return false;
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return false;
	}
}
