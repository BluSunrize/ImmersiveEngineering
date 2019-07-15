/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MiscConnectorBlock extends ConnectorBlock
{
	private final Supplier<TileEntityType<?>> tileType;

	public MiscConnectorBlock(String name, Supplier<TileEntityType<?>> tileType, BlockRenderLayer... layers)
	{
		super(name);
		this.tileType = tileType;
		setBlockLayer(layers);
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(BlockState state)
	{
		return tileType.get().create();
	}
}
