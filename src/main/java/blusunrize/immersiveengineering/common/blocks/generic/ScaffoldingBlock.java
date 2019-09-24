/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;

public class ScaffoldingBlock extends IEBaseBlock.IELadderBlock
{
	public ScaffoldingBlock(String name, Properties material)
	{
		super(name, material, BlockItemIE.class);
		setNotNormalBlock();
		setBlockLayer(BlockRenderLayer.CUTOUT);
	}

	@Override
	public boolean isSideInvisible(BlockState state, BlockState adjState, Direction side)
	{
		return adjState.getBlock()==this;
	}
}
