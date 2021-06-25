/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;

import java.util.function.Supplier;

public class FloodlightBlock extends MiscConnectableBlock<FloodlightTileEntity>
{
	public static final Supplier<Properties> PROPERTIES = () -> ConnectorBlock.PROPERTIES.get()
			.setLightLevel(state -> state.get(IEProperties.ACTIVE)?15: 0);

	public FloodlightBlock(Properties props)
	{
		super(props, IETileTypes.FLOODLIGHT);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.ACTIVE, IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
	}
}
