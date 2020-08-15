/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

//TODO the constructors are a mess, maybe add a builder or something?
public class MiscConnectorBlock extends ConnectorBlock
{
	public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;
	private final Supplier<TileEntityType<?>> tileType;

	public MiscConnectorBlock(String name, Supplier<TileEntityType<?>> tileType)
	{
		this(name, tileType, ImmutableList.of(IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED), ImmutableList.of());
	}

	public MiscConnectorBlock(String name, Supplier<TileEntityType<?>> tileType, IProperty<?>... extraProperties)
	{
		this(name, tileType, ImmutableList.copyOf(extraProperties), ImmutableList.of());
	}

	public MiscConnectorBlock(String name, Supplier<TileEntityType<?>> tileType, BlockRenderLayer... layers)
	{
		this(name, tileType, ImmutableList.of(IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED), ImmutableList.copyOf(layers));
	}

	public MiscConnectorBlock(String name, Supplier<TileEntityType<?>> tileType, List<IProperty<?>> extraProps, List<BlockRenderLayer> layers)
	{
		this(name, tileType, extraProps, layers, BlockItemIE::new);
	}

	public MiscConnectorBlock(String name, Supplier<TileEntityType<?>> tileType, List<IProperty<?>> extraProps,
							  List<BlockRenderLayer> layers, BiFunction<Block, Item.Properties, Item> itemClass)
	{
		super(name, itemClass, extraProps.toArray(new IProperty[0]));
		this.tileType = tileType;
		if(!layers.isEmpty())
			setBlockLayer(layers.toArray(new BlockRenderLayer[0]));
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return tileType.get().create();
	}
}
