/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.config;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CachedConfig
{
	private final List<ConfigValue<?>> value;
	private final ForgeConfigSpec spec;

	private CachedConfig(List<ConfigValue<?>> value, ForgeConfigSpec spec)
	{
		this.value = value;
		this.spec = spec;
	}

	public void refreshCached()
	{
		value.forEach(ConfigValue::refresh);
	}

	public boolean reloadIfMatched(ModConfigEvent ev, Type configType)
	{
		if(ev.getConfig().getModId().equals(ImmersiveEngineering.MODID)&&ev.getConfig().getType()==configType)
		{
			refreshCached();
			return true;
		}
		return false;
	}

	public ForgeConfigSpec getBaseSpec()
	{
		return spec;
	}

	public static class ConfigValue<T> implements Supplier<T>
	{
		private final ForgeConfigSpec.ConfigValue<T> baseValue;
		private T cached;

		public ConfigValue(CachedConfig.Builder builder, ForgeConfigSpec.ConfigValue<T> baseValue)
		{
			this.baseValue = baseValue;
			builder.values.add(this);
		}

		@Nonnull
		public T get()
		{
			return Objects.requireNonNull(cached);
		}

		public void refresh()
		{
			cached = baseValue.get();
		}

		public ForgeConfigSpec.ConfigValue<T> getBase()
		{
			return baseValue;
		}

		public T getOr(T valueDuringStartup)
		{
			if(cached!=null)
				return cached;
			else
				return valueDuringStartup;
		}

		public T getOrDefault()
		{
			return getOr(baseValue.get());
		}
	}

	public static class IntValue extends ConfigValue<Integer>
	{
		IntValue(Builder builder, ForgeConfigSpec.IntValue base)
		{
			super(builder, base);
		}
	}

	public static class DoubleValue extends ConfigValue<Double> implements DoubleSupplier
	{
		DoubleValue(Builder builder, ForgeConfigSpec.DoubleValue base)
		{
			super(builder, base);
		}

		@Override
		public double getAsDouble()
		{
			return get();
		}
	}

	public static class BooleanValue extends ConfigValue<Boolean>
	{
		BooleanValue(Builder builder, ForgeConfigSpec.BooleanValue base)
		{
			super(builder, base);
		}
	}

	public static class Builder
	{
		private final ForgeConfigSpec.Builder inner;
		private final List<ConfigValue<?>> values = new ArrayList<>();

		public Builder()
		{
			this.inner = new ForgeConfigSpec.Builder();
		}

		public Builder comment(String... comment)
		{
			inner.comment(comment);
			return this;
		}

		public Builder worldRestart()
		{
			inner.worldRestart();
			return this;
		}

		public Builder push(String... names)
		{
			inner.push(ImmutableList.copyOf(names));
			return this;
		}

		public Builder pop()
		{
			inner.pop();
			return this;
		}

		public Builder pop(int count)
		{
			inner.pop(count);
			return this;
		}

		public <T> ConfigValue<T> define(String name, T defValue)
		{
			return new ConfigValue<>(this, inner.define(name, defValue));
		}

		public BooleanValue define(String name, boolean def)
		{
			return new BooleanValue(this, inner.define(name, def));
		}

		public DoubleValue defineInRange(String name, double def, double min, double max)
		{
			return new DoubleValue(this, inner.defineInRange(name, def, min, max));
		}

		public IntValue defineInRange(String name, int def, int min, int max)
		{
			return new IntValue(this, inner.defineInRange(name, def, min, max));
		}

		public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator)
		{
			return new ConfigValue<>(this, inner.defineList(path, defaultValue, elementValidator));
		}

		public CachedConfig build()
		{
			return new CachedConfig(values, inner.build());
		}

		public Builder push(ImmutableList<String> of)
		{
			inner.push(of);
			return this;
		}
	}
}
