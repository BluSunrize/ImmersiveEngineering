/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;

public class PoweredMBCallbacks extends Callback<PoweredMultiblockTileEntity<?, ?>>
{
	public static final PoweredMBCallbacks INSTANCE = new PoweredMBCallbacks();

	public PoweredMBCallbacks()
	{
		addAdditional(EnergyCallbacks.INSTANCE);
		addAdditional(MultiblockCallbacks.INSTANCE);
	}
	//TODO general access to recipes?
}
