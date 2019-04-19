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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public class BlockScaffolding extends BlockIEBase.IELadderBlock
{
	public BlockScaffolding(String name, Properties material)
	{
		super(name, material, ItemBlockIEBase.class);
		setNotNormalBlock();
	}

	@Override
	public boolean isSideInvisible(IBlockState state, IBlockState adjState, EnumFacing side)
	{
		return adjState.getBlock()==this;
	}
}
