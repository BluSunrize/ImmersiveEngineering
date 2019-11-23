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
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BarrelBlock extends IETileProviderBlock
{
	private final boolean metal;

	public BarrelBlock(String name, boolean metal)
	{
		super(name, getProperties(metal), BlockItemIE.class);
		this.metal = metal;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		if(metal)
			return new MetalBarrelTileEntity();
		else
			return new WoodenBarrelTileEntity();
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
