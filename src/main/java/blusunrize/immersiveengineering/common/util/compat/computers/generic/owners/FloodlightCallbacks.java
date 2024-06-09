/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.FloodlightBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.EventWaiterResult;

public class FloodlightCallbacks extends CallbackOwner<FloodlightBlockEntity>
{
	public FloodlightCallbacks()
	{
		super(FloodlightBlockEntity.class, "floodlight");
	}

	@ComputerCallable
	public boolean isActive(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return env.object().getIsActive();
	}

	@ComputerCallable
	public void setEnabled(CallbackEnvironment<FloodlightBlockEntity> env, boolean enable)
	{
		env.object().computerControl.setEnabled(enable);
	}

	@ComputerCallable
	public boolean canTurn(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return env.object().canComputerTurn();
	}

	@ComputerCallable
	public void turnAroundXZ(CallbackEnvironment<FloodlightBlockEntity> env, boolean up)
	{
		env.object().turnX(up, true);
	}

	@ComputerCallable
	public void turnAroundY(CallbackEnvironment<FloodlightBlockEntity> env, boolean dir)
	{
		env.object().turnY(dir, true);
	}

	@ComputerCallable
	public int getMaxEnergyStored(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return env.object().maximumStorage;
	}

	@ComputerCallable
	public int getEnergyStored(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return env.object().energyStorage;
	}

	@ComputerCallable(isAsync = true)
	public EventWaiterResult waitUntilTurnable(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return new EventWaiterResult(() -> {
			try
			{
				Thread.sleep(env.object().turnCooldown*50L);
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}, "floodlight_turnable");
	}
}
