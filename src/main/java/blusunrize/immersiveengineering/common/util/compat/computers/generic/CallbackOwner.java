package blusunrize.immersiveengineering.common.util.compat.computers.generic;

public abstract class CallbackOwner<T> extends Callback<T>
{
	private final Class<T> type;
	private final String name;

	protected CallbackOwner(Class<T> type, String name)
	{
		this.type = type;
		this.name = name;
	}

	public final Class<T> getCallbackType()
	{
		return type;
	}

	public final String getName()
	{
		return name;
	}

	public boolean canAttachTo(T candidate)
	{
		return true;
	}

	public T preprocess(T arg)
	{
		return arg;
	}
}
