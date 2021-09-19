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
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MiscConnectableBlock<T extends BlockEntity & IImmersiveConnectable> extends ConnectorBlock
{
	public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;
	private final RegistryObject<BlockEntityType<T>> tileType;

	public MiscConnectableBlock(String name, RegistryObject<BlockEntityType<T>> tileType)
	{
		this(name, tileType, BlockItemIE::new);
	}

	public MiscConnectableBlock(String name, Consumer<Properties> extraSetup, RegistryObject<BlockEntityType<T>> tileType)
	{
		super(name, BlockItemIE::new, extraSetup);
		this.tileType = tileType;
	}

	public MiscConnectableBlock(String name, RegistryObject<BlockEntityType<T>> tileType,
								BiFunction<Block, Item.Properties, Item> itemClass)
	{
		super(name, itemClass);
		this.tileType = tileType;
	}

	@Nullable
	@Override
	public BlockEntity createTileEntity(@Nonnull BlockState state, @Nonnull BlockGetter world)
	{
		return tileType.get().create();
	}
}
