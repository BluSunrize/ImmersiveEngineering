/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class ShaderBannerBlock extends IETileProviderBlock
{
	public ShaderBannerBlock()
	{
		super("shader_banner", Block.Properties.create(Material.WOOL), BlockItemIE.class,
				IEProperties.FACING_ALL);
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(BlockState state)
	{
		return new ShaderBannerTileEntity();
	}
}
