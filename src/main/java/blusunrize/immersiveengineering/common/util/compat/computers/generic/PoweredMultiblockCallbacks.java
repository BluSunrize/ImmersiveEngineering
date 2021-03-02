package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;

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
	public boolean isRunning(CrusherTileEntity tile)
	{
		return tile.shouldRenderAsActive();
	}
}
