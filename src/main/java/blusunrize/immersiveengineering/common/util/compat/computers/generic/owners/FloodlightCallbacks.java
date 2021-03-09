package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.FloodlightTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.*;

public class FloodlightCallbacks extends CallbackOwner<FloodlightTileEntity>
{
	public FloodlightCallbacks()
	{
		super(FloodlightTileEntity.class, "floodlight");
	}

	@ComputerCallable
	public boolean isActive(CallbackEnvironment<FloodlightTileEntity> env)
	{
		return env.getObject().getIsActive();
	}

	@ComputerCallable
	public void setEnabled(CallbackEnvironment<FloodlightTileEntity> env, boolean enable)
	{
		env.getObject().computerControl = new ComputerControlState(env.getIsAttached(), enable);
	}

	@ComputerCallable
	public boolean canTurn(CallbackEnvironment<FloodlightTileEntity> env)
	{
		return env.getObject().canComputerTurn();
	}

	@ComputerCallable
	public void turnAroundXZ(CallbackEnvironment<FloodlightTileEntity> env, boolean up)
	{
		env.getObject().turnX(up, true);
	}

	@ComputerCallable
	public void turnAroundY(CallbackEnvironment<FloodlightTileEntity> env, boolean dir)
	{
		env.getObject().turnY(dir, true);
	}

	@ComputerCallable
	public int getMaxEnergyStored(CallbackEnvironment<FloodlightTileEntity> env)
	{
		return env.getObject().maximumStorage;
	}

	@ComputerCallable
	public int getEnergyStored(CallbackEnvironment<FloodlightTileEntity> env)
	{
		return env.getObject().energyStorage;
	}

	@ComputerCallable(isAsync = true)
	public EventWaiterResult waitUntilTurnable(CallbackEnvironment<FloodlightTileEntity> env)
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
