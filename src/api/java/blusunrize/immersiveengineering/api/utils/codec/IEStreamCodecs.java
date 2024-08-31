/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static net.minecraft.network.codec.ByteBufCodecs.collection;

public class IEStreamCodecs
{
	public static StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, Vec3::x,
			ByteBufCodecs.DOUBLE, Vec3::y,
			ByteBufCodecs.DOUBLE, Vec3::z,
			Vec3::new
	);

	public static final StreamCodec<FriendlyByteBuf, int[]> VAR_INT_ARRAY_STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public int[] decode(FriendlyByteBuf p_320376_)
		{
			return p_320376_.readVarIntArray();
		}

		@Override
		public void encode(FriendlyByteBuf p_320158_, int[] p_320396_)
		{
			p_320158_.writeVarIntArray(p_320396_);
		}
	};

	public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, NonNullList<V>> nonNullList()
	{
		return p_320272_ -> collection($ -> NonNullList.create(), p_320272_);
	}

	public static <T> StreamCodec<ByteBuf, TagKey<T>> tagCodec(ResourceKey<? extends Registry<T>> key)
	{
		return ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(key, rl), TagKey::location);
	}

	public static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumStreamCodec(E[] keys)
	{
		return ByteBufCodecs.VAR_INT.map(i -> keys[i], E::ordinal);
	}

	public static <K, B extends ByteBuf, T>
	StreamCodec<B, Map<K, T>> mapStreamCodec(StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, T> valueCodec)
	{
		return StreamCodec.<B, Pair<K, T>, K, T>composite(
						keyCodec, Pair::getFirst,
						valueCodec, Pair::getSecond,
						Pair::of
				).apply(ByteBufCodecs.list())
				.map(IECodecs::listToMap, IECodecs::mapToList);
	}

	public static <E extends Enum<E>, B extends ByteBuf, T>
	StreamCodec<B, EnumMap<E, T>> enumMapStreamCodec(E[] keys, StreamCodec<? super B, T> valueCodec)
	{
		final var keyCodec = enumStreamCodec(keys);
		return mapStreamCodec(keyCodec, valueCodec).map(EnumMap::new, Function.identity()).cast();
	}

	public static <B extends ByteBuf, C> StreamCodec<B, Set<C>> setOf(StreamCodec<B, C> codec)
	{
		return codec.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf);
	}
}
