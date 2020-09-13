/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;

public class CapacitorHVTileEntity extends CapacitorLVTileEntity
{
	public CapacitorHVTileEntity()
	{
		super(IETileTypes.CAPACITOR_HV.get());
	}
	@Override
	public int getMaxStorage()
	{
		return IEConfig.MACHINES.capacitorHvStorage.get();
	}

	@Override
	public int getMaxInput()
	{
		return IEConfig.MACHINES.capacitorHvInput.get();
	}

	@Override
	public int getMaxOutput()
	{
		return IEConfig.MACHINES.capacitorHvOutput.get();
	}

}