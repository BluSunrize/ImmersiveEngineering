/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import javax.annotation.Nullable;
import java.util.function.Function;

public abstract class LuaTypeConverter
{
	@Nullable
	protected abstract Function<Object, Object> getInternalConverter(Class<?> type);

	public Function<Object, Object> getConverter(Class<?> type)
	{
		Function<Object, Object> mainFunction = getInternalConverter(type);
		if(mainFunction!=null)
			return mainFunction;
		if(type.isArray())
		{
			Function<Object, Object> inner = getInternalConverter(type.getComponentType());
			if(inner!=null)
				return o -> {
					Object[] input = (Object[])o;
					Object[] result = new Object[input.length];
					for(int i = 0; i < input.length; ++i)
						result[i] = inner.apply(input[i]);
					return result;
				};
		}
		return Function.identity();
	}
}
