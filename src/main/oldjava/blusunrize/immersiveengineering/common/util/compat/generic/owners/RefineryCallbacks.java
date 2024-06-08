/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.RefineryLogic.State;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MBEnergyCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.SingleItemCallback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class RefineryCallbacks extends Callback<State>
{
	public RefineryCallbacks()
	{
		addAdditional(MBEnergyCallbacks.INSTANCE, State::getEnergy);
		addAdditional(new TankCallbacks<>(te -> te.tanks.leftInput(), "left input"));
		addAdditional(new TankCallbacks<>(te -> te.tanks.rightInput(), "right input"));
		addAdditional(new TankCallbacks<>(te -> te.tanks.output(), "output"));
		addAdditional(SingleItemCallback.fromHandler(State::getInventory, 0, "left filled canisters"));
		addAdditional(SingleItemCallback.fromHandler(State::getInventory, 1, "left empty canisters"));
		addAdditional(SingleItemCallback.fromHandler(State::getInventory, 2, "right filled canisters"));
		addAdditional(SingleItemCallback.fromHandler(State::getInventory, 3, "right empty canisters"));
		addAdditional(SingleItemCallback.fromHandler(State::getInventory, 4, "empty output canisters"));
		addAdditional(SingleItemCallback.fromHandler(State::getInventory, 3, "filled output canisters"));
	}

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<State> env)
	{
		return env.object().active;
	}
}
