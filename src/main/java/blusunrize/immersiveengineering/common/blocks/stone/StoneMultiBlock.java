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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class StoneMultiBlock extends IEMultiblockBlock
{
	private Supplier<TileEntityType<?>> type;

	public StoneMultiBlock(String name, Supplier<TileEntityType<?>> type)
	{
		super(name, Block.Properties.create(Material.ROCK).hardnessAndResistance(2, 20).notSolid(),
				IEProperties.ACTIVE);
		this.type = type;
		lightOpacity = 0;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return type.get().create();
	}
}
