/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SqueezerLogic.State;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MBEnergyCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.SingleItemCallback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class SqueezerCallbacks extends Callback<State>
{
	public SqueezerCallbacks()
	{
		addAdditional(MBEnergyCallbacks.INSTANCE, State::getEnergy);
		addAdditional(new TankCallbacks<>(State::getTank, ""));
		addAdditional(InventoryCallbacks.fromHandler(State::getInventory, 0, 8, "input"));
		addAdditional(SingleItemCallback.fromHandler(State::getInventory, 9, "empty canisters"));
		addAdditional(SingleItemCallback.fromHandler(State::getInventory, 10, "filled canisters"));
	}

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<State> env)
	{
		return env.object().active;
	}
}
