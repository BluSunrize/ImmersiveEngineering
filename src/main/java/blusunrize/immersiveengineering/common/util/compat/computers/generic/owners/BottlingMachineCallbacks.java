/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic.State;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MBEnergyCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class BottlingMachineCallbacks extends Callback<State>
{
	public BottlingMachineCallbacks()
	{
		addAdditional(MBEnergyCallbacks.INSTANCE, State::getEnergy);
		addAdditional(new TankCallbacks<>(te -> te.tank, ""));
	}

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<State> env)
	{
		return env.object().active;
	}
}
