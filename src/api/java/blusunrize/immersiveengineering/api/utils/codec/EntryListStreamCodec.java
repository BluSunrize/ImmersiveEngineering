/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class EntryListStreamCodec<Stream, T> implements StreamCodec<Stream, T>
{
	private final List<StreamCodecEntry<T, ? super Stream, ?>> entries;
	private final Function<List<?>, T> make;

	public EntryListStreamCodec(List<StreamCodecEntry<T, ? super Stream, ?>> entries, Function<List<?>, T> make)
	{
		this.entries = entries;
		this.make = make;
	}

	@Override
	@NotNull
	public T decode(@NotNull Stream stream)
	{
		List<Object> entries = new ArrayList<>(this.entries.size());
		for(StreamCodecEntry<T, ? super Stream, ?> entry : this.entries)
			entries.add(entry.codec.decode(stream));
		return make.apply(entries);
	}

	@Override
	public void encode(@NotNull Stream stream, @NotNull T value)
	{
		for(var entry : this.entries)
		{
			entry.encodeInto(stream, value);
		}
	}

	public record StreamCodecEntry<MainType, Stream, EntryType>(
			StreamCodec<Stream, EntryType> codec, Function<MainType, EntryType> getter
	)
	{
		public void encodeInto(Stream stream, MainType input)
		{
			codec.encode(stream, getter.apply(input));
		}
	}
}
