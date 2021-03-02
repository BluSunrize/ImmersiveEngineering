package blusunrize.immersiveengineering.common.util.compat.computers.generic;

public interface CallbackOwner<T>
{
	Class<T> getCallbackType();

	String getName();

	boolean canAttachTo(T candidate);

	default T preprocess(T arg)
	{
		return arg;
	}
}
