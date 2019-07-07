/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class BlockScaffolding extends BlockIEBase.IELadderBlock
{
	public BlockScaffolding(String name, Properties material)
	{
		super(name, material, ItemBlockIEBase.class);
		setNotNormalBlock();
	}

	@Override
	public boolean isSideInvisible(BlockState state, BlockState adjState, Direction side)
	{
		return adjState.getBlock()==this;
	}
}
