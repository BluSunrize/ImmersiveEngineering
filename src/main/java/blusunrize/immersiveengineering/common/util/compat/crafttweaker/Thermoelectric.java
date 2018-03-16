/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersiveengineering.Thermoelectric")
public class Thermoelectric
{
	@ZenMethod
	public static void addTemperatureSource(IIngredient source, int temperature)
	{
		try
		{
			CraftTweakerAPI.apply(new Add(CraftTweakerHelper.toIEIngredientStack(source), temperature));
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static class Add implements IAction
	{
		private final IngredientStack ingredientStack;
		private final int temperature;

		public Add(IngredientStack ingredientStack, int temperature)
		{
			this.ingredientStack = ingredientStack;
			this.temperature = temperature;
		}

		@Override
		public void apply()
		{
			ThermoelectricHandler.registerSource(this.ingredientStack, this.temperature);
		}

		@Override
		public String describe()
		{
			return "Adding Thermoelectric temperature value for "+(ingredientStack.oreName!=null?ingredientStack.oreName: ingredientStack.getExampleStack().getDisplayName());
		}
	}

	@ZenMethod
	public static void removeTemperatureSource(IIngredient source)
	{
		try
		{
			CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toIEIngredientStack(source)));
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static class Remove implements IAction
	{
		private final IngredientStack ingredientStack;

		public Remove(IngredientStack ingredientStack)
		{
			this.ingredientStack = ingredientStack;
		}

		@Override
		public void apply()
		{
			if(ThermoelectricHandler.temperatureMap.containsKey(this.ingredientStack))
				ThermoelectricHandler.temperatureMap.remove(this.ingredientStack);
		}

		@Override
		public String describe()
		{
			return "Removing Thermoelectric temperature value for "+(ingredientStack.oreName!=null?ingredientStack.oreName: ingredientStack.getExampleStack().getDisplayName());
		}
	}
}
