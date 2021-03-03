package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import java.util.function.BooleanSupplier;

public class CallbackEnvironment<T>
{
	private final BooleanSupplier isAttached;
	private final T object;

	public CallbackEnvironment(BooleanSupplier isAttached, T object)
	{
		this.isAttached = isAttached;
		this.object = object;
	}

	public BooleanSupplier getIsAttached()
	{
		return isAttached;
	}

	public T getObject()
	{
		return object;
	}
}
