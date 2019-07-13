/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class StoneMultiBlock extends IEMultiblockBlock
{
	private Supplier<TileEntityType<?>> type;

	public StoneMultiBlock(String name, Supplier<TileEntityType<?>> type)
	{
		super(name, Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 20),
				ItemBlockIEBase.class, IEProperties.ACTIVE);
		this.type = type;
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(BlockState state)
	{
		return type.get().create();
	}
}
