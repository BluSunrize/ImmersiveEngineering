/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockMiscConnector extends BlockConnector
{
	private final TileEntityType<?> tileType;

	public BlockMiscConnector(String name, TileEntityType<?> tileType, BlockRenderLayer... layers)
	{
		super(name);
		this.tileType = tileType;
		setBlockLayer(layers);
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(IBlockReader worldIn)
	{
		return tileType.create();
	}
}
