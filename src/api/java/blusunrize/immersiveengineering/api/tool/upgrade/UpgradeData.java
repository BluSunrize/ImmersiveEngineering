/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.upgrade;

import blusunrize.immersiveengineering.api.utils.codec.IECodecs;
import com.mojang.datafixers.util.Unit;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public record UpgradeData(List<UpgradeEntry<?>> entries)
{
	public static final DualCodec<ByteBuf, UpgradeData> CODECS = UpgradeEntry.CODECS.listOf()
			.map(UpgradeData::new, UpgradeData::entries);
	public static final DualCodec<ByteBuf, UpgradeData> SPECIAL_REVOLVER_CODEC = new DualCodec<>(
			IECodecs.directDispatchMap(
					s -> UpgradeEffect.get(s).entryCodec().codec(),
					e -> e.type.name()
			).xmap(UpgradeData::new, UpgradeData::entries),
			CODECS.streamCodec()
	);
	public static final UpgradeData EMPTY = new UpgradeData(List.of());

	public UpgradeData
	{
		entries = List.copyOf(entries);
	}

	public <T> boolean has(UpgradeEffect<T> effect)
	{
		return getIndex(effect) >= 0;
	}

	public <T> T get(UpgradeEffect<T> effect)
	{
		int i = getIndex(effect);
		if(i >= 0)
			return (T)entries.get(i).value;
		else
			return effect.defaultValue();
	}

	public UpgradeData with(UpgradeEffect<Unit> effect)
	{
		return with(effect, Unit.INSTANCE);
	}

	public <T> UpgradeData with(UpgradeEffect<T> effect, T newValue)
	{
		return withModified(effect, $ -> newValue);
	}

	public <T> UpgradeData withModified(UpgradeEffect<T> effect, UnaryOperator<T> modify)
	{
		int i = getIndex(effect);
		final List<UpgradeEntry<?>> newEntries = new ArrayList<>(entries);
		if(i >= 0)
			newEntries.set(i, new UpgradeEntry<>(effect, modify.apply((T)entries.get(i).value)));
		else
			newEntries.add(new UpgradeEntry<>(effect, modify.apply(effect.defaultValue())));
		return new UpgradeData(newEntries);
	}

	private int getIndex(UpgradeEffect<?> effect)
	{
		for(int i = 0; i < entries.size(); ++i)
			if(entries.get(i).type==effect)
				return i;
		return -1;
	}

	public UpgradeData add(UpgradeEffect<Integer> effect, int i)
	{
		return withModified(effect, a -> a+i);
	}

	public UpgradeData add(UpgradeEffect<Float> effect, float i)
	{
		return withModified(effect, a -> a+i);
	}

	public record UpgradeEntry<T>(UpgradeEffect<T> type, T value)
	{
		public static final DualCodec<ByteBuf, UpgradeEntry<?>> CODECS = DualCodecs.STRING.dispatch(
				e -> e.type.name(),
				s -> UpgradeEffect.get(s).entryCodec().fieldOf("data")
		);
	}
}
