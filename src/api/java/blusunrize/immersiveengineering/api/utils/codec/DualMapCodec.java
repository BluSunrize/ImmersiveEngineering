/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import com.mojang.datafixers.util.*;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiFunction;
import java.util.function.Function;

public record DualMapCodec<S extends ByteBuf, T>(MapCodec<T> mapCodec, StreamCodec<S, T> streamCodec)
{
	public static <S extends ByteBuf, T, E1, E2> DualMapCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			BiFunction<E1, E2, T> make
	)
	{
		return new DualMapCodec<>(
				EntryListCodecs.makeMapCodec(field1.mapCodec, get1, field2.mapCodec, get2, make),
				EntryListCodecs.makeStreamCodec(field1.streamCodec, get1, field2.streamCodec, get2, make)
		);
	}

	public static <S extends ByteBuf, T, E1, E2, E3> DualMapCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			Function3<E1, E2, E3, T> make
	)
	{
		return new DualMapCodec<>(
				EntryListCodecs.makeMapCodec(field1.mapCodec, get1, field2.mapCodec, get2, field3.mapCodec, get3, make),
				EntryListCodecs.makeStreamCodec(field1.streamCodec, get1, field2.streamCodec, get2, field3.streamCodec, get3, make)
		);
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4> DualMapCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			DualMapCodec<? super S, E4> field4, Function<T, E4> get4,
			Function4<E1, E2, E3, E4, T> make
	)
	{
		return new DualMapCodec<>(
				EntryListCodecs.makeMapCodec(field1.mapCodec, get1, field2.mapCodec, get2, field3.mapCodec, get3, field4.mapCodec, get4, make),
				EntryListCodecs.makeStreamCodec(field1.streamCodec, get1, field2.streamCodec, get2, field3.streamCodec, get3, field4.streamCodec, get4, make)
		);
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4, E5> DualMapCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			DualMapCodec<? super S, E4> field4, Function<T, E4> get4,
			DualMapCodec<? super S, E5> field5, Function<T, E5> get5,
			Function5<E1, E2, E3, E4, E5, T> make
	)
	{
		return new DualMapCodec<>(
				EntryListCodecs.makeMapCodec(field1.mapCodec, get1, field2.mapCodec, get2, field3.mapCodec, get3, field4.mapCodec, get4, field5.mapCodec, get5, make),
				EntryListCodecs.makeStreamCodec(field1.streamCodec, get1, field2.streamCodec, get2, field3.streamCodec, get3, field4.streamCodec, get4, field5.streamCodec, get5, make)
		);
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4, E5, E6> DualMapCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			DualMapCodec<? super S, E4> field4, Function<T, E4> get4,
			DualMapCodec<? super S, E5> field5, Function<T, E5> get5,
			DualMapCodec<? super S, E6> field6, Function<T, E6> get6,
			Function6<E1, E2, E3, E4, E5, E6, T> make
	)
	{
		return new DualMapCodec<>(
				EntryListCodecs.makeMapCodec(field1.mapCodec, get1, field2.mapCodec, get2, field3.mapCodec, get3, field4.mapCodec, get4, field5.mapCodec, get5, field6.mapCodec, get6, make),
				EntryListCodecs.makeStreamCodec(field1.streamCodec, get1, field2.streamCodec, get2, field3.streamCodec, get3, field4.streamCodec, get4, field5.streamCodec, get5, field6.streamCodec, get6, make)
		);
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4, E5, E6, E7> DualMapCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			DualMapCodec<? super S, E4> field4, Function<T, E4> get4,
			DualMapCodec<? super S, E5> field5, Function<T, E5> get5,
			DualMapCodec<? super S, E6> field6, Function<T, E6> get6,
			DualMapCodec<? super S, E7> field7, Function<T, E7> get7,
			Function7<E1, E2, E3, E4, E5, E6, E7, T> make
	)
	{
		return new DualMapCodec<>(
				EntryListCodecs.makeMapCodec(field1.mapCodec, get1, field2.mapCodec, get2, field3.mapCodec, get3, field4.mapCodec, get4, field5.mapCodec, get5, field6.mapCodec, get6, field7.mapCodec, get7, make),
				EntryListCodecs.makeStreamCodec(field1.streamCodec, get1, field2.streamCodec, get2, field3.streamCodec, get3, field4.streamCodec, get4, field5.streamCodec, get5, field6.streamCodec, get6, field7.streamCodec, get7, make)
		);
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4, E5, E6, E7, E8> DualMapCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			DualMapCodec<? super S, E4> field4, Function<T, E4> get4,
			DualMapCodec<? super S, E5> field5, Function<T, E5> get5,
			DualMapCodec<? super S, E6> field6, Function<T, E6> get6,
			DualMapCodec<? super S, E7> field7, Function<T, E7> get7,
			DualMapCodec<? super S, E8> field8, Function<T, E8> get8,
			Function8<E1, E2, E3, E4, E5, E6, E7, E8, T> make
	)
	{
		return new DualMapCodec<>(
				EntryListCodecs.makeMapCodec(field1.mapCodec, get1, field2.mapCodec, get2, field3.mapCodec, get3, field4.mapCodec, get4, field5.mapCodec, get5, field6.mapCodec, get6, field7.mapCodec, get7, field8.mapCodec, get8, make),
				EntryListCodecs.makeStreamCodec(field1.streamCodec, get1, field2.streamCodec, get2, field3.streamCodec, get3, field4.streamCodec, get4, field5.streamCodec, get5, field6.streamCodec, get6, field7.streamCodec, get7, field8.streamCodec, get8, make)
		);
	}

	public static <S extends ByteBuf, T> DualMapCodec<S, T> unit(T value)
	{
		return new DualMapCodec<>(MapCodec.unit(value), StreamCodec.unit(value));
	}

	public DualCodec<S, T> codec()
	{
		return new DualCodec<>(mapCodec.codec(), streamCodec);
	}

	public <T1> DualMapCodec<S, T1> map(Function<T, T1> to, Function<T1, T> from)
	{
		return new DualMapCodec<>(mapCodec.xmap(to, from), streamCodec.map(to, from));
	}
}
