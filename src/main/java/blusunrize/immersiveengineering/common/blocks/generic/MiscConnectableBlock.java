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
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MiscConnectableBlock<T extends TileEntity & IImmersiveConnectable> extends ConnectorBlock
{
	public static final EnumProperty<Direction> DEFAULT_FACING_PROP = IEProperties.FACING_ALL;
	private final RegistryObject<TileEntityType<T>> tileType;

	public MiscConnectableBlock(Properties props, RegistryObject<TileEntityType<T>> tileType)
	{
		super(props);
		this.tileType = tileType;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return tileType.get().create();
	}
}
