/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.oc2;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallback.ArgumentType;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.EventWaiterResult;
import com.google.gson.JsonArray;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import li.cil.oc2.api.bus.device.rpc.RPCMethod;
import li.cil.oc2.api.bus.device.rpc.RPCParameter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class IERPCMethod<T> implements RPCMethod
{
	private final ComputerCallback<? super T> callback;
	private final T preprocessed;
	private final RPCParameter[] parameters;
	private final T rawObject;

	public IERPCMethod(ComputerCallback<? super T> callback, T rawObject, T preprocessed)
	{
		this.callback = callback;
		this.preprocessed = preprocessed;
		this.rawObject = rawObject;
		this.parameters = callback.getUserArguments().stream()
				.map(ArgumentType::getActualType)
				.map(IERPCParameter::new)
				.toArray(RPCParameter[]::new);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return callback.getName();
	}

	@Override
	public boolean isSynchronized()
	{
		return !callback.isAsync();
	}

	@Nonnull
	@Override
	public Class<?> getReturnType()
	{
		return callback.getLuaReturnType();
	}

	@Nonnull
	@Override
	public RPCParameter[] getParameters()
	{
		return parameters;
	}

	@Nullable
	@Override
	public Object invoke(@Nonnull RPCInvocation rpcInvocation) throws Throwable
	{
		JsonArray jsonParms = rpcInvocation.getParameters();
		Object[] parameters = new Object[jsonParms.size()];
		for(int i = 0; i < jsonParms.size(); ++i)
			parameters[i] = rpcInvocation.getGson().fromJson(
					jsonParms.get(i), getParameters()[i].getType()
			);
		Object[] internalResult = callback.invoke(parameters, new CallbackEnvironment<>(preprocessed, rawObject));
		if(internalResult.length==0)
			return null;
		else if(internalResult[0] instanceof EventWaiterResult waiter)
		{
			waiter.runSync();
			return null;
		}
		return internalResult[0];
	}
}
