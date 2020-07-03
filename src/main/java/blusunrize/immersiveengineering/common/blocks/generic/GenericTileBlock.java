/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class GenericTileBlock extends IETileProviderBlock
{
	private final Supplier<TileEntityType<?>> tileType;

	public GenericTileBlock(String name, Supplier<TileEntityType<?>> tileType, Properties blockProps, Property<?>... stateProps)
	{
		this(name, tileType, blockProps, BlockItemIE::new, stateProps);
	}

	public GenericTileBlock(String name, Supplier<TileEntityType<?>> tileType, Properties blockProps,
							BiFunction<Block, Item.Properties, Item> itemBlock, Property<?>... stateProps)
	{
		super(name, blockProps, itemBlock, stateProps);
		this.tileType = tileType;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return tileType.get().create();
	}
}
