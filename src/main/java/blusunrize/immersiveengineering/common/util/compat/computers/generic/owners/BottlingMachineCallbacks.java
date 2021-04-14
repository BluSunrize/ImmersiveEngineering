/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.BottlingMachineTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class BottlingMachineCallbacks extends MultiblockCallbackOwner<BottlingMachineTileEntity>
{
	public BottlingMachineCallbacks()
	{
		super(BottlingMachineTileEntity.class, "bottling_machine");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new TankCallbacks<>(te -> te.tanks[0], ""));
	}
}
