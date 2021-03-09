package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;

public abstract class MultiblockCallbackOwner<T extends MultiblockPartTileEntity<T>> extends CallbackOwner<T>
{
	public MultiblockCallbackOwner(Class<T> callbackType, String name)
	{
		super(callbackType, name);
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
}
