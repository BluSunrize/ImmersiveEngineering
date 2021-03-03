package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;

public class PoweredMultiblockCallbacks<T extends PoweredMultiblockTileEntity<?, ?>>
		implements CallbackOwner<T>
{
	private final Class<T> callbackType;
	private final String name;

	public PoweredMultiblockCallbacks(Class<T> callbackType, String name)
	{
		this.callbackType = callbackType;
		this.name = name;
	}

	@Override
	public Class<T> getCallbackType()
	{
		return callbackType;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean canAttachTo(T candidate)
	{
		return candidate.isRedstonePos();
	}

	@Override
	public T preprocess(T arg)
	{
		T master = (T)arg.master();
		if(master!=null)
			return master;
		else
			return arg;
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
