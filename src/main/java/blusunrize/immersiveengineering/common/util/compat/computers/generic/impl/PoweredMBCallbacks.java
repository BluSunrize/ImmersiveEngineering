/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;

public class PoweredMBCallbacks extends Callback<PoweredMultiblockBlockEntity<?, ?>>
{
	public static final PoweredMBCallbacks INSTANCE = new PoweredMBCallbacks();

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<PoweredMultiblockBlockEntity<?, ?>> env)
	{
		return env.object().shouldRenderAsActive();
	}

	public PoweredMBCallbacks()
	{
		addAdditional(EnergyCallbacks.INSTANCE);
		addAdditional(MultiblockCallbacks.INSTANCE);
	}
	//TODO general access to recipes?
}
