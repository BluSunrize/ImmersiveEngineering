/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectableBlock;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Supplier;

public class ElectricLanternBlock extends MiscConnectableBlock<ElectricLanternTileEntity>
{
	public static final Supplier<Properties> PROPERTIES = () -> ConnectorBlock.PROPERTIES.get()
			.lightLevel(state -> state.getValue(IEProperties.ACTIVE)?15: 0);

	public ElectricLanternBlock(Properties props)
	{
		super(props, IETileTypes.ELECTRIC_LANTERN);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_TOP_DOWN, IEProperties.ACTIVE, BlockStateProperties.WATERLOGGED);
	}
}
