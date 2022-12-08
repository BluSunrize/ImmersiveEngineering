/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ThermoelectricSourceBuilder extends IEFinishedRecipe<ThermoelectricSourceBuilder>
{
	public static final String SINGLE_BLOCK_KEY = "singleBlock";
	public static final String BLOCK_TAG_KEY = "blockTag";
	public static final String TEMPERATURE_KEY = "tempKelvin";

	private ThermoelectricSourceBuilder(Block matching)
	{
		super(ThermoelectricSource.SERIALIZER.get());
		addWriter(obj -> obj.addProperty(SINGLE_BLOCK_KEY, BuiltInRegistries.BLOCK.getKey(matching).toString()));
	}

	private ThermoelectricSourceBuilder(TagKey<Block> matching)
	{
		super(ThermoelectricSource.SERIALIZER.get());
		addWriter(obj -> obj.addProperty(BLOCK_TAG_KEY, matching.location().toString()));
	}

	public static ThermoelectricSourceBuilder builder(TagKey<Block> tag)
	{
		return new ThermoelectricSourceBuilder(tag);
	}

	public static ThermoelectricSourceBuilder builder(Block blocks)
	{
		return new ThermoelectricSourceBuilder(blocks);
	}

	public ThermoelectricSourceBuilder kelvin(int temperature)
	{
		addWriter(obj -> obj.addProperty(TEMPERATURE_KEY, temperature));
		return this;
	}

	public ThermoelectricSourceBuilder celsius(int temperature)
	{
		return kelvin(TemperatureScale.CELSIUS.toKelvin(temperature));
	}

	public ThermoelectricSourceBuilder fahrenheit(int temperature)
	{
		return kelvin(TemperatureScale.FAHRENHEIT.toKelvin(temperature));
	}

	public enum TemperatureScale
	{
		KELVIN, CELSIUS, FAHRENHEIT;

		public int toKelvin(int in)
		{
			return switch(this)
					{
						case KELVIN -> in;
						case CELSIUS -> in+273;
						case FAHRENHEIT -> (int)Math.round((in-32)/1.8D+273);
					};
		}
	}
}
