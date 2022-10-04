/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.DieselGeneratorBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MultiblockCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class DieselGenCallbacks extends MultiblockCallbackOwner<DieselGeneratorBlockEntity>
{
	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<DieselGeneratorBlockEntity> env)
	{
		return env.object().active;
	}

	public DieselGenCallbacks()
	{
		super(DieselGeneratorBlockEntity.class, "diesel_generator");
		addAdditional(new TankCallbacks<>(te -> te.tanks[0], ""));
		addAdditional(MultiblockCallbacks.INSTANCE);
	}
}
