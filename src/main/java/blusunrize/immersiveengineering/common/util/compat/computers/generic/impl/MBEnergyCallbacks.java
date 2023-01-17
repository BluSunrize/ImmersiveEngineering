/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import net.minecraftforge.energy.IEnergyStorage;

public class MBEnergyCallbacks extends Callback<IEnergyStorage>
{
	public static final MBEnergyCallbacks INSTANCE = new MBEnergyCallbacks();

	@ComputerCallable
	public int getMaxEnergyStored(CallbackEnvironment<IEnergyStorage> env)
	{
		return env.object().getMaxEnergyStored();
	}

	@ComputerCallable
	public int getEnergyStored(CallbackEnvironment<IEnergyStorage> env)
	{
		return env.object().getEnergyStored();
	}
}
