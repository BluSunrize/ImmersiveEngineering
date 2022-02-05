/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.oc2;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControlState;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControllable;
import li.cil.oc2.api.bus.device.rpc.RPCDevice;
import li.cil.oc2.api.bus.device.rpc.RPCMethodGroup;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class IEDevice<T> implements RPCDevice
{
	private final WrappedOwner<T> owner;
	private final T value;
	private final List<RPCMethodGroup> methods;

	public IEDevice(WrappedOwner<T> owner, T value)
	{
		this.owner = owner;
		this.value = value;
		List<RPCMethodGroup> list = new ArrayList<>();
		for(ComputerCallback<? super T> cb : owner.getMethods())
			list.add(new IERPCMethod<>(cb, value));
		this.methods = list;
	}

	@Override
	@Nonnull
	public List<String> getTypeNames()
	{
		return List.of(owner.getOwner().getName());
	}

	@Nonnull
	@Override
	public List<RPCMethodGroup> getMethodGroups()
	{
		return methods;
	}

	@Override
	public void mount()
	{
		if(value instanceof ComputerControllable controllable)
			controllable.getAllComputerControlStates().forEach(ComputerControlState::addReference);
	}

	@Override
	public void unmount()
	{
		if(value instanceof ComputerControllable controllable)
			controllable.getAllComputerControlStates().forEach(ComputerControlState::removeReference);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj==this) return true;
		if(obj==null||obj.getClass()!=this.getClass()) return false;
		IEDevice<?> that = (IEDevice<?>)obj;
		return Objects.equals(this.owner, that.owner)&&
				Objects.equals(this.value, that.value);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(owner, value);
	}
}
