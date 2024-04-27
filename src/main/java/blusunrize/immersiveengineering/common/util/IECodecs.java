/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

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

import java.util.function.Function;

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
}
