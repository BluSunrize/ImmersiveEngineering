/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorBundledTileEntity;
import dan200.computercraft.api.ComputerCraftAPI;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

public class ComputerCraftCompatModule extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		ComputerCraftAPI.registerBundledRedstoneProvider((world, pos, direction) -> {
			final int doNotHandle = -1;
			BlockState state = world.getBlockState(pos);
			if(state.getBlock()!=Connectors.connectorBundled)
				return doNotHandle;
			TileEntity tile = world.getTileEntity(pos);
			if(!(tile instanceof ConnectorBundledTileEntity))
				return doNotHandle;
			int bits = 0;
			for(int color = 0; color < 16; ++color)
				if(((ConnectorBundledTileEntity)tile).getValue(color) > 0)
					bits |= 1<<color;
			return bits;
		});

		ConnectorBundledTileEntity.EXTRA_SOURCES.add((world, emittingBlock, emittingSide) -> {
			int output = ComputerCraftAPI.getBundledRedstoneOutput(world, emittingBlock, emittingSide);
			if(output==0||output==-1)
			{
				return null;
			}
			byte[] channelValues = new byte[16];
			for(int color = 0; color < 16; ++color)
				channelValues[color] = (byte)(15*((output >> color)&1));
			return channelValues;
		});
	}

	@Override
	public void postInit()
	{
	}
}
