/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.IETileTypes;

public class CapacitorMVTileEntity extends CapacitorLVTileEntity
{
	public CapacitorMVTileEntity()
	{
		super(IETileTypes.CAPACITOR_MV.get(), IEServerConfig.MACHINES.mvCapConfig);
	}
}