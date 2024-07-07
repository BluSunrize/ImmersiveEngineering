/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public record DualCodec<S extends ByteBuf, T>(Codec<T> codec, StreamCodec<S, T> streamCodec)
{
	public <T1> DualCodec<S, T1> map(Function<T, T1> to, Function<T1, T> from)
	{
		return new DualCodec<>(codec.xmap(to, from), streamCodec.map(to, from));
	}

	public DualMapCodec<S, T> fieldOf(String name)
	{
		return new DualMapCodec<>(codec.fieldOf(name), streamCodec);
	}

	public <S1 extends S>
	DualCodec<S1, T> castStream()
	{
		return new DualCodec<>(codec, streamCodec.cast());
	}

	public <V>
	DualCodec<S, V> dispatch(Function<V, T> getKey, Function<T, DualMapCodec<? super S, ? extends V>> getCodec)
	{
		return new DualCodec<>(
				codec.dispatch(getKey, k -> getCodec.apply(k).mapCodec()),
				streamCodec.dispatch(getKey, k -> getCodec.apply(k).streamCodec())
		);
	}

	public DualCodec<S, Set<T>> setOf()
	{
		return new DualCodec<>(
				NeoForgeExtraCodecs.setOf(codec),
				streamCodec.apply(ByteBufCodecs.collection($ -> new HashSet<>()))
		);
	}

	public DualCodec<S, List<T>> listOf()
	{
		return new DualCodec<>(
				codec.listOf(),
				streamCodec.apply(ByteBufCodecs.list())
		);
	}

	public DualMapCodec<S, T> optionalFieldOf(String name, T fallback)
	{
		return new DualMapCodec<>(codec.optionalFieldOf(name, fallback), streamCodec);
	}

	public DualMapCodec<S, Optional<T>> optionalFieldOf(String name)
	{
		return new DualMapCodec<>(codec.optionalFieldOf(name), ByteBufCodecs.optional(streamCodec));
	}

	public Tag toNBT(T object)
	{
		return codec().encodeStart(NbtOps.INSTANCE, object).getOrThrow();
	}

	public T fromNBT(Tag nbt)
	{
		return codec().decode(NbtOps.INSTANCE, nbt).getOrThrow().getFirst();
	}
}
