/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import blusunrize.immersiveengineering.api.utils.codec.EntryListCodec.CodecEntry;
import blusunrize.immersiveengineering.api.utils.codec.EntryListStreamCodec.StreamCodecEntry;
import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class EntryListCodecs
{
	// 2 fields
	public static <T, E1, E2> Codec<T> makeCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			BiFunction<E1, E2, T> make
	)
	{
		return makeMapCodec(field1, get1, field2, get2, make).codec();
	}

	public static <T, E1, E2> MapCodec<T> makeMapCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			BiFunction<E1, E2, T> make
	)
	{
		return new EntryListCodec<>(
				List.of(new CodecEntry<>(field1, get1), new CodecEntry<>(field2, get2)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1))
		);
	}

	public static <T, S, E1, E2> StreamCodec<S, T> makeStreamCodec(
			StreamCodec<? super S, E1> field1, Function<T, E1> get1,
			StreamCodec<? super S, E2> field2, Function<T, E2> get2,
			BiFunction<E1, E2, T> make
	)
	{
		return new EntryListStreamCodec<>(
				List.of(new StreamCodecEntry<>(field1, get1), new StreamCodecEntry<>(field2, get2)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1))
		);
	}

	// 3 fields
	public static <T, E1, E2, E3> Codec<T> makeCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			Function3<E1, E2, E3, T> make
	)
	{
		return makeMapCodec(field1, get1, field2, get2, field3, get3, make).codec();
	}

	public static <T, E1, E2, E3> MapCodec<T> makeMapCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			Function3<E1, E2, E3, T> make
	)
	{
		return new EntryListCodec<>(
				List.of(new CodecEntry<>(field1, get1), new CodecEntry<>(field2, get2), new CodecEntry<>(field3, get3)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2))
		);
	}

	public static <T, S, E1, E2, E3> StreamCodec<S, T> makeStreamCodec(
			StreamCodec<? super S, E1> field1, Function<T, E1> get1,
			StreamCodec<? super S, E2> field2, Function<T, E2> get2,
			StreamCodec<? super S, E3> field3, Function<T, E3> get3,
			Function3<E1, E2, E3, T> make
	)
	{
		return new EntryListStreamCodec<>(
				List.of(new StreamCodecEntry<>(field1, get1), new StreamCodecEntry<>(field2, get2), new StreamCodecEntry<>(field3, get3)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2))
		);
	}

	// 4 fields
	public static <T, E1, E2, E3, E4> Codec<T> makeCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			Function4<E1, E2, E3, E4, T> make
	)
	{
		return makeMapCodec(field1, get1, field2, get2, field3, get3, field4, get4, make).codec();
	}

	public static <T, E1, E2, E3, E4> MapCodec<T> makeMapCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			Function4<E1, E2, E3, E4, T> make
	)
	{
		return new EntryListCodec<>(
				List.of(new CodecEntry<>(field1, get1), new CodecEntry<>(field2, get2), new CodecEntry<>(field3, get3), new CodecEntry<>(field4, get4)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3))
		);
	}

	public static <T, S, E1, E2, E3, E4> StreamCodec<S, T> makeStreamCodec(
			StreamCodec<? super S, E1> field1, Function<T, E1> get1,
			StreamCodec<? super S, E2> field2, Function<T, E2> get2,
			StreamCodec<? super S, E3> field3, Function<T, E3> get3,
			StreamCodec<? super S, E4> field4, Function<T, E4> get4,
			Function4<E1, E2, E3, E4, T> make
	)
	{
		return new EntryListStreamCodec<>(
				List.of(new StreamCodecEntry<>(field1, get1), new StreamCodecEntry<>(field2, get2), new StreamCodecEntry<>(field3, get3), new StreamCodecEntry<>(field4, get4)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3))
		);
	}

	// 5 fields
	public static <T, E1, E2, E3, E4, E5> Codec<T> makeCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			MapCodec<E5> field5, Function<T, E5> get5,
			Function5<E1, E2, E3, E4, E5, T> make
	)
	{
		return makeMapCodec(field1, get1, field2, get2, field3, get3, field4, get4, field5, get5, make).codec();
	}

	public static <T, E1, E2, E3, E4, E5> MapCodec<T> makeMapCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			MapCodec<E5> field5, Function<T, E5> get5,
			Function5<E1, E2, E3, E4, E5, T> make
	)
	{
		return new EntryListCodec<>(
				List.of(new CodecEntry<>(field1, get1), new CodecEntry<>(field2, get2), new CodecEntry<>(field3, get3), new CodecEntry<>(field4, get4), new CodecEntry<>(field5, get5)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3), (E5)entryValues.get(4))
		);
	}

	public static <T, S, E1, E2, E3, E4, E5> StreamCodec<S, T> makeStreamCodec(
			StreamCodec<? super S, E1> field1, Function<T, E1> get1,
			StreamCodec<? super S, E2> field2, Function<T, E2> get2,
			StreamCodec<? super S, E3> field3, Function<T, E3> get3,
			StreamCodec<? super S, E4> field4, Function<T, E4> get4,
			StreamCodec<? super S, E5> field5, Function<T, E5> get5,
			Function5<E1, E2, E3, E4, E5, T> make
	)
	{
		return new EntryListStreamCodec<>(
				List.of(new StreamCodecEntry<>(field1, get1), new StreamCodecEntry<>(field2, get2), new StreamCodecEntry<>(field3, get3), new StreamCodecEntry<>(field4, get4), new StreamCodecEntry<>(field5, get5)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3), (E5)entryValues.get(4))
		);
	}

	// 6 fields
	public static <T, E1, E2, E3, E4, E5, E6> Codec<T> makeCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			MapCodec<E5> field5, Function<T, E5> get5,
			MapCodec<E6> field6, Function<T, E6> get6,
			Function6<E1, E2, E3, E4, E5, E6, T> make
	)
	{
		return makeMapCodec(field1, get1, field2, get2, field3, get3, field4, get4, field5, get5, field6, get6, make).codec();
	}

	public static <T, E1, E2, E3, E4, E5, E6> MapCodec<T> makeMapCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			MapCodec<E5> field5, Function<T, E5> get5,
			MapCodec<E6> field6, Function<T, E6> get6,
			Function6<E1, E2, E3, E4, E5, E6, T> make
	)
	{
		return new EntryListCodec<>(
				List.of(new CodecEntry<>(field1, get1), new CodecEntry<>(field2, get2), new CodecEntry<>(field3, get3), new CodecEntry<>(field4, get4), new CodecEntry<>(field5, get5), new CodecEntry<>(field6, get6)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3), (E5)entryValues.get(4), (E6)entryValues.get(5))
		);
	}

	public static <T, S, E1, E2, E3, E4, E5, E6> StreamCodec<S, T> makeStreamCodec(
			StreamCodec<? super S, E1> field1, Function<T, E1> get1,
			StreamCodec<? super S, E2> field2, Function<T, E2> get2,
			StreamCodec<? super S, E3> field3, Function<T, E3> get3,
			StreamCodec<? super S, E4> field4, Function<T, E4> get4,
			StreamCodec<? super S, E5> field5, Function<T, E5> get5,
			StreamCodec<? super S, E6> field6, Function<T, E6> get6,
			Function6<E1, E2, E3, E4, E5, E6, T> make
	)
	{
		return new EntryListStreamCodec<>(
				List.of(new StreamCodecEntry<>(field1, get1), new StreamCodecEntry<>(field2, get2), new StreamCodecEntry<>(field3, get3), new StreamCodecEntry<>(field4, get4), new StreamCodecEntry<>(field5, get5), new StreamCodecEntry<>(field6, get6)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3), (E5)entryValues.get(4), (E6)entryValues.get(5))
		);
	}

	// 7 fields
	public static <T, E1, E2, E3, E4, E5, E6, E7> Codec<T> makeCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			MapCodec<E5> field5, Function<T, E5> get5,
			MapCodec<E6> field6, Function<T, E6> get6,
			MapCodec<E7> field7, Function<T, E7> get7,
			Function7<E1, E2, E3, E4, E5, E6, E7, T> make
	)
	{
		return makeMapCodec(field1, get1, field2, get2, field3, get3, field4, get4, field5, get5, field6, get6, field7, get7, make).codec();
	}

	public static <T, E1, E2, E3, E4, E5, E6, E7> MapCodec<T> makeMapCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			MapCodec<E5> field5, Function<T, E5> get5,
			MapCodec<E6> field6, Function<T, E6> get6,
			MapCodec<E7> field7, Function<T, E7> get7,
			Function7<E1, E2, E3, E4, E5, E6, E7, T> make
	)
	{
		return new EntryListCodec<>(
				List.of(new CodecEntry<>(field1, get1), new CodecEntry<>(field2, get2), new CodecEntry<>(field3, get3), new CodecEntry<>(field4, get4), new CodecEntry<>(field5, get5), new CodecEntry<>(field6, get6), new CodecEntry<>(field7, get7)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3), (E5)entryValues.get(4), (E6)entryValues.get(5), (E7)entryValues.get(6))
		);
	}

	public static <T, S, E1, E2, E3, E4, E5, E6, E7> StreamCodec<S, T> makeStreamCodec(
			StreamCodec<? super S, E1> field1, Function<T, E1> get1,
			StreamCodec<? super S, E2> field2, Function<T, E2> get2,
			StreamCodec<? super S, E3> field3, Function<T, E3> get3,
			StreamCodec<? super S, E4> field4, Function<T, E4> get4,
			StreamCodec<? super S, E5> field5, Function<T, E5> get5,
			StreamCodec<? super S, E6> field6, Function<T, E6> get6,
			StreamCodec<? super S, E7> field7, Function<T, E7> get7,
			Function7<E1, E2, E3, E4, E5, E6, E7, T> make
	)
	{
		return new EntryListStreamCodec<>(
				List.of(new StreamCodecEntry<>(field1, get1), new StreamCodecEntry<>(field2, get2), new StreamCodecEntry<>(field3, get3), new StreamCodecEntry<>(field4, get4), new StreamCodecEntry<>(field5, get5), new StreamCodecEntry<>(field6, get6), new StreamCodecEntry<>(field7, get7)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3), (E5)entryValues.get(4), (E6)entryValues.get(5), (E7)entryValues.get(6))
		);
	}

	// 8 fields
	public static <T, E1, E2, E3, E4, E5, E6, E7, E8> Codec<T> makeCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			MapCodec<E5> field5, Function<T, E5> get5,
			MapCodec<E6> field6, Function<T, E6> get6,
			MapCodec<E7> field7, Function<T, E7> get7,
			MapCodec<E8> field8, Function<T, E8> get8,
			Function8<E1, E2, E3, E4, E5, E6, E7, E8, T> make
	)
	{
		return makeMapCodec(field1, get1, field2, get2, field3, get3, field4, get4, field5, get5, field6, get6, field7, get7, field8, get8, make).codec();
	}

	public static <T, E1, E2, E3, E4, E5, E6, E7, E8> MapCodec<T> makeMapCodec(
			MapCodec<E1> field1, Function<T, E1> get1,
			MapCodec<E2> field2, Function<T, E2> get2,
			MapCodec<E3> field3, Function<T, E3> get3,
			MapCodec<E4> field4, Function<T, E4> get4,
			MapCodec<E5> field5, Function<T, E5> get5,
			MapCodec<E6> field6, Function<T, E6> get6,
			MapCodec<E7> field7, Function<T, E7> get7,
			MapCodec<E8> field8, Function<T, E8> get8,
			Function8<E1, E2, E3, E4, E5, E6, E7, E8, T> make
	)
	{
		return new EntryListCodec<>(
				List.of(new CodecEntry<>(field1, get1), new CodecEntry<>(field2, get2), new CodecEntry<>(field3, get3), new CodecEntry<>(field4, get4), new CodecEntry<>(field5, get5), new CodecEntry<>(field6, get6), new CodecEntry<>(field7, get7), new CodecEntry<>(field8, get8)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3), (E5)entryValues.get(4), (E6)entryValues.get(5), (E7)entryValues.get(6), (E8)entryValues.get(7))
		);
	}

	public static <T, S, E1, E2, E3, E4, E5, E6, E7, E8> StreamCodec<S, T> makeStreamCodec(
			StreamCodec<? super S, E1> field1, Function<T, E1> get1,
			StreamCodec<? super S, E2> field2, Function<T, E2> get2,
			StreamCodec<? super S, E3> field3, Function<T, E3> get3,
			StreamCodec<? super S, E4> field4, Function<T, E4> get4,
			StreamCodec<? super S, E5> field5, Function<T, E5> get5,
			StreamCodec<? super S, E6> field6, Function<T, E6> get6,
			StreamCodec<? super S, E7> field7, Function<T, E7> get7,
			StreamCodec<? super S, E8> field8, Function<T, E8> get8,
			Function8<E1, E2, E3, E4, E5, E6, E7, E8, T> make
	)
	{
		return new EntryListStreamCodec<>(
				List.of(new StreamCodecEntry<>(field1, get1), new StreamCodecEntry<>(field2, get2), new StreamCodecEntry<>(field3, get3), new StreamCodecEntry<>(field4, get4), new StreamCodecEntry<>(field5, get5), new StreamCodecEntry<>(field6, get6), new StreamCodecEntry<>(field7, get7), new StreamCodecEntry<>(field8, get8)),
				entryValues -> make.apply((E1)entryValues.getFirst(), (E2)entryValues.get(1), (E3)entryValues.get(2), (E4)entryValues.get(3), (E5)entryValues.get(4), (E6)entryValues.get(5), (E7)entryValues.get(6), (E8)entryValues.get(7))
		);
	}
}
