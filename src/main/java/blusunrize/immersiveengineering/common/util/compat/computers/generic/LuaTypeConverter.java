package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import java.util.function.Function;

public interface LuaTypeConverter
{
	Function<Object, Object> getSerializer(Class<?> type);
}
