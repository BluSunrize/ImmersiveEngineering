package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControlState;

public class PoweredMBCallbacks extends Callback<PoweredMultiblockTileEntity<?, ?>>
{
	public static final PoweredMBCallbacks INSTANCE = new PoweredMBCallbacks();

	public PoweredMBCallbacks()
	{
		addAdditional(EnergyCallbacks.INSTANCE);
	}

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<PoweredMultiblockTileEntity<?, ?>> env)
	{
		return env.getObject().shouldRenderAsActive();
	}

	@ComputerCallable
	public void setEnabled(CallbackEnvironment<PoweredMultiblockTileEntity<?, ?>> env, boolean enable)
	{
		env.getObject().computerControl = new ComputerControlState(env.getIsAttached(), enable);
	}
}
