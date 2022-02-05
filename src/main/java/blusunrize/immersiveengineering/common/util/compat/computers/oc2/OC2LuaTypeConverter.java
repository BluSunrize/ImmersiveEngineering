/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.oc2;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.LuaTypeConverter;

import javax.annotation.Nullable;

public class OC2LuaTypeConverter extends LuaTypeConverter
{
	public static final LuaTypeConverter INSTANCE = new OC2LuaTypeConverter();

	private OC2LuaTypeConverter()
	{
	}

	@Nullable
	@Override
	protected <F> Converter<F, ?> getInternalConverter(Class<F> type)
	{
		return null;
	}
}
