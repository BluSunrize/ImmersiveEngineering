package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ComputerCallback<T>
{
	private final List<Class<?>> userArguments;
	private final Function<Object, Object[]> wrapReturnValue;
	private final MethodHandle caller;
	private final String name;
	private final boolean isAsync;

	private ComputerCallback(
			Callback<T> owner, Method method, LuaTypeConverter converters
	) throws IllegalAccessException
	{
		this.caller = MethodHandles.lookup().unreflect(method).bindTo(owner);
		Function<Object, Object[]> wrapResult;
		Class<?> resultType = method.getReturnType();
		if(Object[].class.equals(resultType))
			wrapResult = o -> (Object[])o;
		else if(void.class.equals(resultType))
			wrapResult = $ -> new Object[0];
		else
			wrapResult = o -> new Object[]{o};
		this.wrapReturnValue = wrapResult.compose(converters.getSerializer(resultType));
		this.name = owner.renameMethod(method.getName());
		this.userArguments = Lists.newArrayList(method.getParameterTypes());
		for(int i = 0; i < this.userArguments.size(); i++)
			if(this.userArguments.get(i).isPrimitive())
				this.userArguments.set(i, Primitives.wrap(this.userArguments.get(i)));
		isAsync = method.getAnnotation(ComputerCallable.class).isAsync();
		Preconditions.checkState(!this.userArguments.isEmpty());
		Preconditions.checkState(this.userArguments.get(0).equals(CallbackEnvironment.class));
		Preconditions.checkState(isAsync||!resultType.equals(EventWaiterResult.class));
		userArguments.remove(0);
	}

	public String getName()
	{
		return name;
	}

	public static <T> List<ComputerCallback<? super T>>
	getInClass(Callback<T> provider, LuaTypeConverter converters) throws IllegalAccessException
	{
		List<ComputerCallback<? super T>> callbacks = new ArrayList<>();
		for(Method m : provider.getClass().getMethods())
			if(m.isAnnotationPresent(ComputerCallable.class))
				callbacks.add(new ComputerCallback<>(provider, m, converters));
		for(Callback<? super T> extra : provider.getAdditionalCallbacks())
			callbacks.addAll(getInClass(extra, converters));
		return callbacks;
	}

	public Object[] invoke(Object[] arguments, CallbackEnvironment<T> env) throws Throwable
	{
		if(arguments.length!=this.userArguments.size())
			throw new RuntimeException(
					"Unexpected number of arguments: Expected "+this.userArguments.size()+", got "+arguments.length
			);
		Object[] realArguments = new Object[arguments.length+1];
		System.arraycopy(arguments, 0, realArguments, 1, arguments.length);
		realArguments[0] = env;
		for(int i = 0; i < arguments.length; ++i)
		{
			int realIndex = i+1;
			Class<?> expectedType = this.userArguments.get(i);
			Object actual = realArguments[realIndex];
			if(!actual.getClass().equals(expectedType))
			{
				if(Number.class.isAssignableFrom(expectedType)&&actual instanceof Double)
					realArguments[realIndex] = fixNumber((Double)actual, expectedType);
				else
					throw new RuntimeException(
							"Unexpected argument type at argument "+i+": Expected "+
									this.userArguments.get(i).getSimpleName()+", got "+arguments[i].getClass().getSimpleName()
					);
			}
		}
		return wrapReturnValue.apply(caller.invokeWithArguments(realArguments));
	}

	public boolean isAsync()
	{
		return isAsync;
	}

	private static Number fixNumber(Double fromLua, Class<?> correctType)
	{
		if(correctType==Double.class)
			return fromLua;
		else if(correctType==Float.class)
			return fromLua.floatValue();
		else if(correctType==Byte.class)
			return fromLua.byteValue();
		else if(correctType==Short.class)
			return fromLua.shortValue();
		else if(correctType==Integer.class)
			return fromLua.intValue();
		else if(correctType==Long.class)
			return fromLua.longValue();
		else
			return fromLua;
	}
}
