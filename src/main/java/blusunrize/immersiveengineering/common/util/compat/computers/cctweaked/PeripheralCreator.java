/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.EventWaiterResult;
import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class PeripheralCreator<T extends BlockEntity>
{
	private final List<ComputerCallback<? super T>> methods;
	private final String[] methodNames;
	private final CallbackOwner<T> owner;

	public PeripheralCreator(CallbackOwner<T> owner) throws IllegalAccessException
	{
		this.methods = ComputerCallback.getInClass(owner, CCLuaTypeConverter.INSTANCE, Function.identity());
		this.methodNames = this.methods.stream()
				.map(ComputerCallback::getName)
				.toArray(String[]::new);
		this.owner = owner;
	}

	@Nullable
	public GenericPeripheral<T> make(Object instanceObj)
	{
		Preconditions.checkArgument(this.owner.getCallbackType().isAssignableFrom(instanceObj.getClass()));
		T instance = (T)instanceObj;
		if(owner.canAttachTo(instance))
			return new GenericPeripheral<>(this, instance);
		else
			return null;
	}

	public String[] getMethodNames()
	{
		return methodNames;
	}

	public MethodResult call(
			IComputerAccess computerAccess, ILuaContext ctx, int index, IArguments otherArgs, T mainArgument
	) throws LuaException
	{
		ComputerCallback<? super T> callback = methods.get(index);

		if(callback.isAsync())
		{
			Object[] result = callInner(callback, otherArgs, mainArgument);
			if(result[0] instanceof EventWaiterResult eventResult)
			{
				eventResult.startAsync(() -> computerAccess.queueEvent(eventResult.name()));
				return MethodResult.pullEvent(eventResult.name(), args -> MethodResult.of());
			}
			else
				return MethodResult.of(result);
		}
		else
			return TaskCallback.make(ctx, () -> callInner(callback, otherArgs, mainArgument));
	}

	private Object[] callInner(
			ComputerCallback<? super T> callback, IArguments otherArgs, T mainArgument
	) throws LuaException
	{
		try
		{
			return callback.invoke(
					otherArgs.getAll(),
					new CallbackEnvironment<>(owner.preprocess(mainArgument), mainArgument.getLevel())
			);
		} catch(RuntimeException x)
		{
			throw new LuaException(x.getMessage());
		} catch(Throwable throwable)
		{
			throwable.printStackTrace();
			throw new RuntimeException("Unexpected error, check server log!");
		}
	}

	public String getName()
	{
		return owner.getName();
	}
}
