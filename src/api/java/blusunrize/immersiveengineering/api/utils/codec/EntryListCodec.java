/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import com.mojang.serialization.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public final class EntryListCodec<T> extends MapCodec<T>
{
	private final List<CodecEntry<T, ?>> entries;
	private final Function<List<?>, T> make;

	public EntryListCodec(List<CodecEntry<T, ?>> entries, Function<List<?>, T> make)
	{
		this.entries = entries;
		this.make = make;
	}

	@Override
	public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input)
	{
		List<Object> entries = new ArrayList<>(this.entries.size());
		Lifecycle lifecycle = Lifecycle.stable();
		for(CodecEntry<T, ?> entry : this.entries)
		{
			var partData = entry.codec.decode(ops, input);
			if(partData.result().isPresent())
			{
				entries.add(partData.result().get());
				lifecycle = lifecycle.add(partData.lifecycle());
			}
			else
				return partData.map(t -> null);
		}
		return DataResult.success(make.apply(entries), lifecycle);
	}

	@Override
	public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix)
	{
		for(var entry : this.entries)
		{
			prefix = entry.encodeInto(input, ops, prefix);
		}
		return prefix;
	}

	@Override
	public <T1> Stream<T1> keys(DynamicOps<T1> ops)
	{
		return this.entries.stream().flatMap(e -> e.codec.keys(ops));
	}

	public record CodecEntry<MainType, EntryType>(
			MapCodec<EntryType> codec, Function<MainType, EntryType> getter
	)
	{
		public <Data> RecordBuilder<Data> encodeInto(
				MainType input, DynamicOps<Data> ops, RecordBuilder<Data> prefix
		)
		{
			return codec.encode(getter.apply(input), ops, prefix);
		}
	}
}
