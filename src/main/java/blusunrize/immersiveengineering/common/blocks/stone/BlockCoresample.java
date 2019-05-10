/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockCoresample extends BlockIETileProvider
{
	public BlockCoresample(String name)
	{
		super(name, Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 20), ItemBlockIEBase.class,
				IEProperties.FACING_HORIZONTAL);
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(IBlockState state)
	{
		return new TileEntityCoresample();
	}
}
