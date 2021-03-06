package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallback;
import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;

public class PeripheralCreator<T>
{
	private final List<ComputerCallback<T>> methods;
	private final String[] methodNames;
	private final CallbackOwner<T> owner;

	public PeripheralCreator(CallbackOwner<T> owner) throws IllegalAccessException
	{
		this.methods = ComputerCallback.getInClass(owner, CCLuaTypeConverter.INSTANCE);
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
			ILuaContext ctx, int index, IArguments otherArgs, T mainArgument, BooleanSupplier isAttached
	) throws LuaException
	{
		// For now none of our callbacks are thread-safe. If some end up being safe in the future this should be changed
		// to handle those on the CC:Tweaked thread
		ComputerCallback<T> callback = methods.get(index);
		return TaskCallback.make(ctx, () -> {
					try
					{
						return callback.invoke(
								otherArgs.getAll(), new CallbackEnvironment<>(isAttached, owner.preprocess(mainArgument))
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
		);
	}

	public String getName()
	{
		return owner.getName();
	}
}
