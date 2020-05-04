/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ShaderBannerBlock extends IETileProviderBlock
{
	public ShaderBannerBlock(String name, IProperty... stateProps)
	{
		super(name, Block.Properties.create(Material.WOOL).doesNotBlockMovement(), (b, p) -> null, stateProps);
		setNotNormalBlock();
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return new ShaderBannerTileEntity();
	}

	@Nonnull
	@Override
	public Item asItem()
	{
		return Items.WHITE_BANNER;
	}
}
