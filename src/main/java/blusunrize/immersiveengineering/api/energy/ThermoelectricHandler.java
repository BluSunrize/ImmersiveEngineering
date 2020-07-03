/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author BluSunrize - 08.05.2015
 * <p>
 * The temperature registry to allow additional blocks to work with the Thermoelectric Generator
 */
public class ThermoelectricHandler
{
	public static List<ThermoelectricSource> temperatureMap = new ArrayList<>();

	public static void registerSourceInKelvin(ThermoelectricSource source)
	{
		temperatureMap.add(source);
	}

	public static void registerSourceInKelvin(Block b, int value)
	{
		temperatureMap.add(new ThermoelectricSource(b, value, TemperatureScale.KELVIN));
	}

	public static void registerSourceInKelvin(ITag<Block> tag, int value)
	{
		temperatureMap.add(new ThermoelectricSource(tag, value, TemperatureScale.KELVIN));
	}

	public static void registerSourceInCelsius(ITag<Block> tag, int value)
	{
		temperatureMap.add(new ThermoelectricSource(tag, value, TemperatureScale.CELSIUS));
	}

	public static int getTemperature(Block block)
	{
		for(ThermoelectricSource entry : temperatureMap)
			if(entry.matches.test(block))
				return entry.temperature;
		return -1;
	}

	public static SortedMap<String, Integer> getThermalValuesSorted(boolean inverse)
	{
		SortedMap<String, Integer> existingMap = new TreeMap<>(
				inverse?Comparator.<String>reverseOrder(): Comparator.<String>reverseOrder()
		);
		for(ThermoelectricSource ingr : temperatureMap)
		{
			Optional<Block> exampleOpt = ingr.getExample.get();
			exampleOpt.ifPresent(example ->
					existingMap.put(new ItemStack(example).getDisplayName().getFormattedText(), ingr.temperature));
		}
		return existingMap;
	}

	public static class ThermoelectricSource
	{
		public final Predicate<Block> matches;
		public final int temperature;
		public final Supplier<Optional<Block>> getExample;

		public ThermoelectricSource(Block b, int temperature, TemperatureScale s)
		{
			this(b2 -> b2==b, temperature, () -> Optional.of(b), s);
		}

		public ThermoelectricSource(ITag<Block> tag, int temperature, TemperatureScale scale)
		{
			this(b -> b.isIn(tag), temperature, () -> {
				Collection<Block> allMatching = tag.getAllElements();
				return allMatching.stream().findAny();
			}, scale);
		}

		public ThermoelectricSource(Predicate<Block> matches, int temperature, Supplier<Optional<Block>> getExample, TemperatureScale s)
		{
			this.matches = matches;
			this.temperature = s.convertToKelvin.applyAsInt(temperature);
			this.getExample = getExample;
		}
	}

	public enum TemperatureScale
	{
		KELVIN(s -> s),
		CELSIUS(s -> s+273),
		FAHRENHEIT(value -> (int)Math.round((value-32)/1.8D+273));
		public final Int2IntFunction convertToKelvin;

		TemperatureScale(Int2IntFunction convertToKelvin)
		{
			this.convertToKelvin = convertToKelvin;
		}
	}
}
