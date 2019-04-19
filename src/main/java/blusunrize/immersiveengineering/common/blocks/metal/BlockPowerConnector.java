/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.generic.BlockConnector;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockPowerConnector extends BlockConnector
{
	private final String voltage;
	private final boolean relay;

	public BlockPowerConnector(String voltage, boolean relay)
	{
		super("connector_"+voltage.toLowerCase()+(relay?"_relay": ""));
		this.voltage = voltage;
		this.relay = relay;
	}

	@Nullable
	@Override
	public TileEntity createBasicTE(IBlockReader worldIn)
	{
		return new TileEntityEnergyConnector(voltage, relay);
	}
}
