/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IECodecs
{
	public static final Codec<ItemStack> ITEM_STACK_COUNT_OPTIONAL = Codec.either(ItemStack.CODEC, ItemStack.SINGLE_ITEM_CODEC).xmap(
			either -> either.map(Function.identity(), Function.identity()),
			stack -> stack.getCount()==1?Either.right(stack):Either.left(stack)
	);

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
		return Codec.dispatchedMap(Codec.STRING, valueCodec::apply)
				.xmap(
						m -> List.copyOf(m.values()),
						s -> {
							Map<String, V> map = new HashMap<>();
							for(var v : s)
								map.put(getKey.apply(v), v);
							return map;
						}
				);
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

	public static <T> T fromNbtOrThrow(Codec<T> codec, Tag data)
	{
		return codec.decode(NbtOps.INSTANCE, data).getOrThrow().getFirst();
	}

	public static <T> Tag toNbtOrThrow(Codec<T> codec, T object)
	{
		return codec.encodeStart(NbtOps.INSTANCE, object).getOrThrow();
	}

	public static <B, T> StreamCodec<B, T> lenientUnitStream(T value)
	{
		return new StreamCodec<B, T>()
		{
			@Override
			public T decode(B buffer)
			{
				return value;
			}

			@Override
			public void encode(B buffer, T value)
			{
				// Unlike StreamCodec.unit, do not check that the values match here
			}
		};
	}
}
