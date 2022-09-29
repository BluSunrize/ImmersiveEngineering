/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;

public abstract class MultiblockCallbackOwner<T extends MultiblockPartBlockEntity<T>> extends CallbackOwner<T>
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
		return arg;
	}
}
