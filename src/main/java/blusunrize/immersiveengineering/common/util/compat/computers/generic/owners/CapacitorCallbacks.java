/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.CapacitorTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.EnergyCallbacks;

public class CapacitorCallbacks extends CallbackOwner<CapacitorTileEntity>
{
	public CapacitorCallbacks(String voltage)
	{
		super(CapacitorTileEntity.class, "capacitor_"+voltage);
		addAdditional(EnergyCallbacks.INSTANCE);
	}
}
