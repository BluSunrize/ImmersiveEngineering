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
	protected abstract <F> Converter<F, ?> getInternalConverter(Class<F> type);

	public <F> Converter<F, ?> getConverter(Class<F> type)
	{
		Converter<F, ?> mainFunction = getInternalConverter(type);
		if(mainFunction!=null)
			return mainFunction;
		if(type.isArray())
		{
			Converter<?, ?> inner = getInternalConverter(type.getComponentType());
			if(inner!=null)
				return new Converter<>(o -> {
					Object[] input = (Object[])o;
					Object[] result = new Object[input.length];
					for(int i = 0; i < input.length; ++i)
						result[i] = inner.convertUnchecked(input[i]);
					return result;
				}, inner.outputType().arrayType());
		}
		return new Converter<>(Function.identity(), type);
	}

	public record Converter<F, T>(Function<F, T> convert, Class<? extends T> outputType)
	{
		@SuppressWarnings("unchecked")
		public Object convertUnchecked(Object in)
		{
			return convert().apply((F)in);
		}
	}
}
