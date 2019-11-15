/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MiscConnectorBlock extends ConnectorBlock
{
	public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;
	private final Supplier<TileEntityType<?>> tileType;

	public MiscConnectorBlock(String name, Supplier<TileEntityType<?>> tileType, BlockRenderLayer... layers)
	{
		super(name);
		this.tileType = tileType;
		if(layers.length > 0)
			setBlockLayer(layers);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return tileType.get().create();
	}
}
