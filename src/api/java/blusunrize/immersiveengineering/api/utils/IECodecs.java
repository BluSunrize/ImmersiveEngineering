/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.network.codec.ByteBufCodecs.collection;

public class IECodecs
{
	public static StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, Vec3::x,
			ByteBufCodecs.DOUBLE, Vec3::y,
			ByteBufCodecs.DOUBLE, Vec3::z,
			Vec3::new
	);

	public static final Codec<NonNullList<Ingredient>> NONNULL_INGREDIENTS = Ingredient.LIST_CODEC.xmap(
			l -> {
				NonNullList<Ingredient> result = NonNullList.create();
				result.addAll(l);
				return result;
			},
			Function.identity()
	);

	public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, NonNullList<V>> nonNullList()
	{
		return p_320272_ -> collection($ -> NonNullList.create(), p_320272_);
	}

	public static <T> StreamCodec<ByteBuf, TagKey<T>> tagCodec(ResourceKey<Registry<T>> key)
	{
		return ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(key, rl), TagKey::location);
	}

	public static <E extends Enum<E>> Codec<E> enumCodec(E[] keys)
	{
		return Codec.intRange(0, keys.length-1).xmap(i -> keys[i], E::ordinal);
	}

	public static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumStreamCodec(E[] keys)
	{
		return ByteBufCodecs.VAR_INT.map(i -> keys[i], E::ordinal);
	}

	public static <E extends Enum<E>, B extends ByteBuf, T>
	StreamCodec<B, EnumMap<E, T>> enumMapStreamCodec(E[] keys, StreamCodec<B, T> valueCodec)
	{
		final var keyCodec = enumStreamCodec(keys);
		return StreamCodec.<B, Pair<E, T>, E, T>composite(
						keyCodec, Pair::getFirst,
						valueCodec, Pair::getSecond,
						Pair::of
				).apply(ByteBufCodecs.list())
				.map(IECodecs::listToEnumMap, IECodecs::mapToList);
	}

	public static <E extends Enum<E>, T> Codec<EnumMap<E, T>> enumMapCodec(E[] keys, Codec<T> valueCodec)
	{
		final var keyCodec = enumCodec(keys);
		return Codec.pair(keyCodec, valueCodec)
				.listOf()
				.xmap(IECodecs::listToEnumMap, IECodecs::mapToList);
	}

	public static <C> Codec<Set<C>> setOf(Codec<C> codec)
	{
		return codec.listOf().xmap(Set::copyOf, List::copyOf);
	}

	public static <B extends ByteBuf, C> StreamCodec<B, Set<C>> setOf(StreamCodec<B, C> codec)
	{
		return codec.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf);
	}

	private static <E extends Enum<E>, T>
	EnumMap<E, T> listToEnumMap(List<Pair<E, T>> l)
	{
		return new EnumMap<E, T>(l.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
	}

	private static <E extends Enum<E>, T>
	List<Pair<E, T>> mapToList(Map<E, T> m)
	{
		return m.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList();
	}
}
