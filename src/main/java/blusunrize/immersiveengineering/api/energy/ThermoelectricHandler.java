/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author BluSunrize - 08.05.2015
 * <p>
 * The temperature registry to allow additional blocks to work with the Thermoelectric Generator<br>
 * Registry uses IngredientStacks which may be based on OreDictionary names
 */
public class ThermoelectricHandler
{
	public static HashMap<IngredientStack, Integer> temperatureMap = new HashMap<IngredientStack, Integer>();

	public static void registerSourceInKelvin(IngredientStack source, int value)
	{
		temperatureMap.put(source, value);
	}

	public static void registerSourceInKelvin(Tag tag, int value)
	{
		registerSourceInKelvin(new IngredientStack(tag), value);
	}

	public static void registerSourceInCelsius(Tag tag, int value)
	{
		registerSourceInKelvin(new IngredientStack(tag), value+273);
	}

	public static int getTemperature(Block block)
	{
		ItemStack stack = new ItemStack(block, 1);
		if(!stack.isEmpty())
			for(Map.Entry<IngredientStack, Integer> entry : temperatureMap.entrySet())
				if(entry.getKey().matchesItemStackIgnoringSize(stack))
					return entry.getValue();
		return -1;
	}

	public static Map<String, Integer> getThermalValuesSorted(boolean inverse)
	{
		HashMap<String, Integer> existingMap = new HashMap<>();
		for(IngredientStack ingr : temperatureMap.keySet())
			if(ingr.isValid())
			{
				ItemStack example = ingr.getExampleStack();
				if(!example.isEmpty())
					existingMap.put(example.getDisplayName().getFormattedText(), temperatureMap.get(ingr));
			}
		return ApiUtils.sortMap(existingMap, inverse);
	}
}
