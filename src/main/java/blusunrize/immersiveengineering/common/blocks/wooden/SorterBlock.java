/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SorterBlock extends IETileProviderBlock
{
	boolean fluid;

	public SorterBlock(String name, boolean fluid)
	{
		super(name, Block.Properties.create(Material.WOOD).hardnessAndResistance(2F, 5F),
				BlockItemIE::new);
		this.fluid = fluid;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		if(fluid)
			return new FluidSorterTileEntity();
		else
			return new SorterTileEntity();
	}
}
