/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilBlockEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;

public class TeslaCoilCallbacks extends CallbackOwner<TeslaCoilBlockEntity>
{
	public TeslaCoilCallbacks()
	{
		super(TeslaCoilBlockEntity.class, "tesla_coil");
	}

	@ComputerCallable
	public boolean isActive(CallbackEnvironment<TeslaCoilBlockEntity> env)
	{
		int energyDrain = IEServerConfig.MACHINES.teslacoil_consumption.get();
		if(env.getObject().lowPower)
			energyDrain /= 2;
		return env.getObject().canRun(energyDrain);
	}

	@ComputerCallable
	public void setRSMode(CallbackEnvironment<TeslaCoilBlockEntity> env, boolean inverted)
	{
		env.getObject().redstoneControlInverted = inverted;
	}

	@ComputerCallable
	public void setPowerMode(CallbackEnvironment<TeslaCoilBlockEntity> env, boolean high)
	{
		if(isActive(env))
			throw new RuntimeException("Can't switch power mode on an active coil");
		env.getObject().lowPower = high;
	}
}
