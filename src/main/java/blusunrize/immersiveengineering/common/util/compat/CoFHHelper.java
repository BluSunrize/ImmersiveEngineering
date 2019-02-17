/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;

import java.lang.reflect.Field;
import java.util.function.Function;

public class CoFHHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
		try
		{
			final Class c_ingredientClass = Class.forName("cofh.core.util.crafting.FluidIngredientFactory$FluidIngredient");
			final Field f_fluid = c_ingredientClass.getDeclaredField("fluid");
			f_fluid.setAccessible(true);
			AssemblerHandler.registerSpecialQueryConverters(new Function<Object, RecipeQuery>()
			{
				@Override
				public RecipeQuery apply(Object o)
				{
					if(c_ingredientClass.isAssignableFrom(o.getClass()))
						try
						{
							return new RecipeQuery(f_fluid.get(o), 10000);
						} catch(Exception e)
						{
						}
					return null;
				}
			});
		} catch(Exception e)
		{
		}
	}

	@Override
	public void postInit()
	{
	}
}