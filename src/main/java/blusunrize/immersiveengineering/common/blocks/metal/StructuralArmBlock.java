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

public class StructuralArmBlock extends IETileProviderBlock
{
	public StructuralArmBlock(String name)
	{
		super(name, Properties.create(Material.IRON).hardnessAndResistance(3, 15), ItemBlockIEBase.class);
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(BlockState state)
	{
		return StructuralArmTileEntity.TYPE.create();
	}
}
