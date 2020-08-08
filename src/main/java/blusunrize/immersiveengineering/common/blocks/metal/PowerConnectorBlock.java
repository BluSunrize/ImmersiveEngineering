/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PowerConnectorBlock extends ConnectorBlock
{
	private final String voltage;
	private final boolean relay;

	public PowerConnectorBlock(String voltage, boolean relay, BlockRenderLayer... layers)
	{
		super("connector_"+voltage.toLowerCase()+(relay?"_relay": ""), IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
		this.voltage = voltage;
		this.relay = relay;
		if(layers.length > 0)
			setBlockLayer(layers);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return new EnergyConnectorTileEntity(voltage, relay);
	}
}
