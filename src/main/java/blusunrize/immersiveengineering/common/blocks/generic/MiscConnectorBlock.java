/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

//TODO the constructors are a mess, maybe add a builder or something?
public class MiscConnectorBlock<T extends TileEntity & IImmersiveConnectable> extends ConnectorBlock
{
	public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;
	private final RegistryObject<TileEntityType<T>> tileType;

	public MiscConnectorBlock(String name, RegistryObject<TileEntityType<T>> tileType)
	{
		this(name, tileType, ImmutableList.of(IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED), BlockItemIE::new);
	}

	public MiscConnectorBlock(String name, RegistryObject<TileEntityType<T>> tileType, Property<?>... extraProperties)
	{
		this(name, tileType, ImmutableList.copyOf(extraProperties), BlockItemIE::new);
	}

	public MiscConnectorBlock(String name, RegistryObject<TileEntityType<T>> tileType, List<Property<?>> extraProps,
							  BiFunction<Block, Item.Properties, Item> itemClass)
	{
		super(name, itemClass, extraProps.toArray(new Property[0]));
		this.tileType = tileType;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return tileType.get().create();
	}
}
