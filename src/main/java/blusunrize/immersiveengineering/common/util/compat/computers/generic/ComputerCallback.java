package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ComputerCallback<T>
{
	private final List<Class<?>> userArguments;
	private final boolean wrapReturnValue;
	private final MethodHandle caller;
	private final String name;
	private final CallbackOwner<T> owner;

	private ComputerCallback(CallbackOwner<T> owner, Method method) throws IllegalAccessException
	{
		this.caller = MethodHandles.lookup().unreflect(method).bindTo(owner);
		this.wrapReturnValue = !Object[].class.equals(method.getReturnType());
		this.owner = owner;
		this.name = method.getName();
		this.userArguments = Lists.newArrayList(method.getParameterTypes());
		Preconditions.checkState(!this.userArguments.isEmpty());
		Preconditions.checkState(this.userArguments.get(0).equals(owner.getCallbackType()));
		userArguments.remove(0);
	}

	public String getName()
	{
		return name;
	}

	public static <T> List<ComputerCallback<T>>
	getInClass(CallbackOwner<T> provider) throws IllegalAccessException
	{
		List<ComputerCallback<T>> callbacks = new ArrayList<>();
		for(Method m : provider.getClass().getMethods())
			if(m.isAnnotationPresent(ComputerCallable.class))
				callbacks.add(new ComputerCallback<>(provider, m));
		return callbacks;
	}

	public Object[] invoke(Object[] arguments, T callbackObject)
	{
		if(arguments.length!=this.userArguments.size())
			throw new RuntimeException(
					"Unexpected number of arguments: Expected "+this.userArguments.size()+", got "+arguments.length
			);
		for(int i = 0; i < arguments.length; ++i)
			if(!arguments[i].getClass().equals(this.userArguments.get(i)))
				throw new RuntimeException(
						"Unexpected argument type at argument "+i+": Expected "+
								this.userArguments.get(i).getSimpleName()+", got "+arguments[i].getClass().getSimpleName()
				);
		try
		{
			Object[] realArguments = new Object[arguments.length+1];
			System.arraycopy(arguments, 0, realArguments, 1, arguments.length);
			realArguments[0] = owner.preprocess(callbackObject);
			Object result = caller.invokeWithArguments(realArguments);
			if(wrapReturnValue)
				return new Object[]{result};
			else
				return (Object[])result;
		} catch(Throwable throwable)
		{
			throwable.printStackTrace();
			throw new RuntimeException("Unexpected error, check server log!");
		}
	}
}
