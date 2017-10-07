/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config.IEConfig;

public class TileEntityCapacitorMV extends TileEntityCapacitorLV
{
	@Override
	public int getMaxStorage()
	{
		return IEConfig.Machines.capacitorMV_storage;
	}
	@Override
	public int getMaxInput()
	{
		return IEConfig.Machines.capacitorMV_input;
	}
	@Override
	public int getMaxOutput()
	{
		return IEConfig.Machines.capacitorMV_output;
	}

}