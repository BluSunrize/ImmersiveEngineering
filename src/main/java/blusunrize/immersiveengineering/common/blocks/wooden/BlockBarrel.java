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

public class BlockBarrel extends BlockIETileProvider
{
	public BlockBarrel(String name, boolean metal)
	{
		super(name, getProperties(metal), ItemBlockIEBase.class);
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(IBlockReader worldIn)
	{
		return new TileEntityWoodenBarrel();
	}

	private static Block.Properties getProperties(boolean metal)
	{
		Block.Properties base;
		if(metal)
			base = Block.Properties.create(Material.IRON);
		else
			base = Block.Properties.create(Material.WOOD);
		return base.hardnessAndResistance(2, 5);
	}
}
