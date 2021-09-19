/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.Function;
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

	public static void registerSourceInKelvin(Tag<Block> tag, int value)
	{
		temperatureMap.add(new ThermoelectricSource(tag, value, TemperatureScale.KELVIN));
	}

	public static void registerSourceInCelsius(Tag<Block> tag, int value)
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

	public static SortedMap<Component, Integer> getThermalValuesSorted(boolean inverse)
	{
		SortedMap<Component, Integer> existingMap = new TreeMap<>(
				Comparator.comparing(
						(Function<Component, String>)Component::getString,
						inverse?Comparator.reverseOrder(): Comparator.naturalOrder()
				)
		);
		for(ThermoelectricSource ingr : temperatureMap)
		{
			Optional<Block> exampleOpt = ingr.getExample.get();
			exampleOpt.ifPresent(example ->
					existingMap.put(new ItemStack(example).getHoverName(), ingr.temperature));
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

		public ThermoelectricSource(Tag<Block> tag, int temperature, TemperatureScale scale)
		{
			this(b -> b.is(tag), temperature, () -> {
				Collection<Block> allMatching = tag.getValues();
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
