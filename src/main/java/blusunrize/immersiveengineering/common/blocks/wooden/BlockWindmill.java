/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockWindmill extends BlockIETileProvider
{
	public BlockWindmill(String name)
	{
		super(name, Properties.create(Material.WOOD).hardnessAndResistance(2, 5),
				ItemBlockIEBase.class, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL);
		setNotNormalBlock();
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(IBlockState state)
	{
		return new TileEntityWindmill();
	}
}
