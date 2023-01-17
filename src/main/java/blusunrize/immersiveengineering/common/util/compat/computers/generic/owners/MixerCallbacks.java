/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic.State;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MBEnergyCallbacks;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class MixerCallbacks extends Callback<State>
{
	public MixerCallbacks()
	{
		addAdditional(MBEnergyCallbacks.INSTANCE, State::getEnergy);
		addAdditional(InventoryCallbacks.fromHandler(State::getInventory, 0, 12, "input"));
	}

	@ComputerCallable
	public FluidStack getFluid(CallbackEnvironment<State> env, @IndexArgument int index)
	{
		List<FluidStack> fluids = env.object().tank.fluids;
		if(index >= 0&&index < fluids.size())
			return fluids.get(index);
		else
			throw new RuntimeException("Tank currently contains "+fluids.size()+" fluids!");
	}

	@ComputerCallable
	public int getCapacity(CallbackEnvironment<State> env)
	{
		return env.object().tank.getCapacity();
	}

	@ComputerCallable
	public int getNumFluids(CallbackEnvironment<State> env)
	{
		return env.object().tank.fluids.size();
	}

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<State> env)
	{
		return env.object().isActive;
	}
}
