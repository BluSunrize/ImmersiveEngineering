/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IECodecs
{
	public static final Codec<NonNullList<Ingredient>> NONNULL_INGREDIENTS = Ingredient.LIST_CODEC.xmap(
			l -> {
				NonNullList<Ingredient> result = NonNullList.create();
				result.addAll(l);
				return result;
			},
			Function.identity()
	);

	public static <E extends Enum<E>> Codec<E> enumCodec(E[] keys)
	{
		Map<String, E> reverseLookup = Arrays.stream(keys).collect(Collectors.toMap(E::name, Function.identity()));
		return Codec.STRING.xmap(reverseLookup::get, E::name);
	}

	public static <K, T> Codec<Map<K, T>> listBasedMap(Codec<K> keyCodec, Codec<T> valueCodec)
	{
		Codec<Pair<K, T>> entryCodec = RecordCodecBuilder.create(inst -> inst.group(
				keyCodec.fieldOf("key").forGetter(Pair::getFirst),
				valueCodec.fieldOf("value").forGetter(Pair::getSecond)
		).apply(inst, Pair::of));
		return entryCodec.listOf().xmap(IECodecs::listToMap, IECodecs::mapToList);
	}

	public static <E extends Enum<E>, T> Codec<EnumMap<E, T>> listBasedEnumMap(E[] keys, Codec<T> valueCodec)
	{
		final var keyCodec = enumCodec(keys);
		return listBasedMap(keyCodec, valueCodec).xmap(EnumMap::new, Function.identity());
	}

	public static <V> Codec<List<V>> directDispatchMap(
			Function<String, Codec<V>> valueCodec, Function<V, String> getKey
	)
	{
		return new Codec<>()
		{
			@Override
			public <T> DataResult<Pair<List<V>, T>> decode(DynamicOps<T> ops, T input)
			{
				return ops.getMapValues(input).flatMap(s -> {
					DataResult<List<V>> result = DataResult.success(new ArrayList<>());
					for(var entry : s.toList())
						result = result.flatMap(
								current -> ops.getStringValue(entry.getFirst()).flatMap(
										key -> valueCodec.apply(key).decode(ops, entry.getSecond()).map(
												value -> {
													var newMap = new ArrayList<>(current);
													newMap.add(value.getFirst());
													return newMap;
												}
										)
								)
						);
					return result.map(m -> new Pair<>(m, ops.empty()));
				});
			}

			@Override
			public <T> DataResult<T> encode(List<V> input, DynamicOps<T> ops, T prefix)
			{
				DataResult<T> result = DataResult.success(prefix);
				for(var entry : input)
					result = result.flatMap(oldT -> {
						var type = getKey.apply(entry);
						var maybeValue = valueCodec.apply(type).encodeStart(ops, entry);
						var key = ops.createString(type);
						return maybeValue.flatMap(valueT -> ops.mergeToMap(oldT, key, valueT));
					});
				return result;
			}
		};
	}

	public static <C> Codec<Set<C>> setOf(Codec<C> codec)
	{
		return codec.listOf().xmap(Set::copyOf, List::copyOf);
	}

	static <K, T>
	Map<K, T> listToMap(List<Pair<K, T>> l)
	{
		return l.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	static <K, T>
	List<Pair<K, T>> mapToList(Map<K, T> m)
	{
		return m.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList();
	}
}
