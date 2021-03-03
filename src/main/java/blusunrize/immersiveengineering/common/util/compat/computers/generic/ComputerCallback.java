package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ComputerCallback<T>
{
	private final List<Class<?>> userArguments;
	private final Function<Object, Object[]> wrapReturnValue;
	private final MethodHandle caller;
	private final String name;

	private ComputerCallback(CallbackOwner<T> owner, Method method) throws IllegalAccessException
	{
		this.caller = MethodHandles.lookup().unreflect(method).bindTo(owner);
		if(Object[].class.equals(method.getReturnType()))
			this.wrapReturnValue = o -> (Object[])o;
		else if(void.class.equals(method.getReturnType()))
			this.wrapReturnValue = $ -> new Object[0];
		else
			this.wrapReturnValue = o -> new Object[]{o};
		this.name = method.getName();
		this.userArguments = Lists.newArrayList(method.getParameterTypes());
		for(int i = 0; i < this.userArguments.size(); i++)
			if(this.userArguments.get(i).isPrimitive())
				this.userArguments.set(i, Primitives.wrap(this.userArguments.get(i)));
		Preconditions.checkState(!this.userArguments.isEmpty());
		Preconditions.checkState(this.userArguments.get(0).equals(CallbackEnvironment.class));
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

	public Object[] invoke(Object[] arguments, CallbackEnvironment<T> env)
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
			realArguments[0] = env;
			return wrapReturnValue.apply(caller.invokeWithArguments(realArguments));
		} catch(Throwable throwable)
		{
			throwable.printStackTrace();
			throw new RuntimeException("Unexpected error, check server log!");
		}
	}
}
