/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.oc2;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallback;

import java.util.List;
import java.util.Objects;

public class WrappedOwner<T>
{
	private final CallbackOwner<T> owner;
	private final List<ComputerCallback<? super T>> methods;

	public WrappedOwner(CallbackOwner<T> owner) throws IllegalAccessException
	{
		this.owner = Objects.requireNonNull(owner);
		this.methods = ComputerCallback.getInClass(owner, OC2LuaTypeConverter.INSTANCE);
	}

	public CallbackOwner<T> getOwner()
	{
		return owner;
	}

	public List<ComputerCallback<? super T>> getMethods()
	{
		return methods;
	}
}
