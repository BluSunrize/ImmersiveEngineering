/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class GenericTileBlock extends IETileProviderBlock
{
	private final Supplier<TileEntityType<?>> tileType;

	public GenericTileBlock(String name, Supplier<TileEntityType<?>> tileType, Properties blockProps, IProperty<?>... stateProps)
	{
		this(name, tileType, blockProps, BlockItemIE.class, stateProps);
	}

	public GenericTileBlock(String name, Supplier<TileEntityType<?>> tileType, Properties blockProps,
							@Nullable Class<? extends BlockItemIE> itemBlock, IProperty<?>... stateProps)
	{
		super(name, blockProps, itemBlock, stateProps);
		this.tileType = tileType;
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(BlockState state)
	{
		return tileType.get().create();
	}
}
