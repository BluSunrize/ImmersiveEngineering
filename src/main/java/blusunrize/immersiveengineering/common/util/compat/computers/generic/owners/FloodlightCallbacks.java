/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.FloodlightBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.*;

public class FloodlightCallbacks extends CallbackOwner<FloodlightBlockEntity>
{
	public FloodlightCallbacks()
	{
		super(FloodlightBlockEntity.class, "floodlight");
	}

	@ComputerCallable
	public boolean isActive(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return env.getObject().getIsActive();
	}

	@ComputerCallable
	public void setEnabled(CallbackEnvironment<FloodlightBlockEntity> env, boolean enable)
	{
		env.getObject().computerControl = new ComputerControlState(env.getIsAttached(), enable);
	}

	@ComputerCallable
	public boolean canTurn(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return env.getObject().canComputerTurn();
	}

	@ComputerCallable
	public void turnAroundXZ(CallbackEnvironment<FloodlightBlockEntity> env, boolean up)
	{
		env.getObject().turnX(up, true);
	}

	@ComputerCallable
	public void turnAroundY(CallbackEnvironment<FloodlightBlockEntity> env, boolean dir)
	{
		env.getObject().turnY(dir, true);
	}

	@ComputerCallable
	public int getMaxEnergyStored(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return env.getObject().maximumStorage;
	}

	@ComputerCallable
	public int getEnergyStored(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return env.getObject().energyStorage;
	}

	@ComputerCallable(isAsync = true)
	public EventWaiterResult waitUntilTurnable(CallbackEnvironment<FloodlightBlockEntity> env)
	{
		return new EventWaiterResult(callback -> {
			new Thread(() -> {
				try
				{
					Thread.sleep(env.getObject().turnCooldown*50L);
				} catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				callback.run();
			}).start();
		}, "floodlight_turnable");
	}
}
