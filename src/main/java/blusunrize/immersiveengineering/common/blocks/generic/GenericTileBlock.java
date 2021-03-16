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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class GenericTileBlock<T extends TileEntity> extends IETileProviderBlock
{
	private final RegistryObject<TileEntityType<T>> tileType;

	public GenericTileBlock(String name, RegistryObject<TileEntityType<T>> tileType, Properties blockProps)
	{
		this(name, tileType, blockProps, BlockItemIE::new);
	}

	public GenericTileBlock(String name, RegistryObject<TileEntityType<T>> tileType, Properties blockProps,
							BiFunction<Block, Item.Properties, Item> itemBlock)
	{
		super(name, blockProps, itemBlock);
		this.tileType = tileType;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return tileType.get().create();
	}
}
