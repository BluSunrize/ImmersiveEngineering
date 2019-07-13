/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class LanternBlock extends IETileProviderBlock
{
	public LanternBlock(String name)
	{
		super(name, Properties.create(Material.IRON).hardnessAndResistance(3, 15), ItemBlockIEBase.class);
	}

	//TODO replace with states
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(BlockState state)
	{
		return LanternTileEntity.TYPE.create();
	}
}
