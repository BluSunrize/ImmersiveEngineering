/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockSorter extends BlockIETileProvider
{
	boolean fluid;

	public BlockSorter(String name, boolean fluid)
	{
		super(name, Block.Properties.create(Material.WOOD).hardnessAndResistance(2F, 5F),
				ItemBlockIEBase.class);
		this.fluid = fluid;
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(IBlockReader worldIn)
	{
		if(fluid)
			return new TileEntityFluidSorter();
		else
			return new TileEntitySorter();
	}
}
