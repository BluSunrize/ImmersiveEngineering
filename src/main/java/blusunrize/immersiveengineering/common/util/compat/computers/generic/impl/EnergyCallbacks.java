package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;

public class EnergyCallbacks extends Callback<IIEInternalFluxHandler>
{
	public static final EnergyCallbacks INSTANCE = new EnergyCallbacks();

	@ComputerCallable
	public int getMaxEnergyStored(CallbackEnvironment<IIEInternalFluxHandler> env)
	{
		return env.getObject().getMaxEnergyStored(null);
	}

	@ComputerCallable
	public int getEnergyStored(CallbackEnvironment<IIEInternalFluxHandler> env)
	{
		return env.getObject().getEnergyStored(null);
	}
}
