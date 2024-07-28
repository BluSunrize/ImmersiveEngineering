/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import blusunrize.immersiveengineering.api.utils.IECodecs;
import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.EnumMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DualCodecs
{
	public static final DualCodec<ByteBuf, Integer> INT = new DualCodec<>(Codec.INT, ByteBufCodecs.VAR_INT);
	public static final DualCodec<ByteBuf, Float> FLOAT = new DualCodec<>(Codec.FLOAT, ByteBufCodecs.FLOAT);
	public static final DualCodec<ByteBuf, String> STRING = new DualCodec<>(Codec.STRING, ByteBufCodecs.STRING_UTF8);
	public static final DualCodec<ByteBuf, Double> DOUBLE = new DualCodec<>(Codec.DOUBLE, ByteBufCodecs.DOUBLE);
	public static final DualCodec<ByteBuf, Boolean> BOOL = new DualCodec<>(Codec.BOOL, ByteBufCodecs.BOOL);
	public static final DualCodec<ByteBuf, Long> LONG = new DualCodec<>(Codec.LONG, ByteBufCodecs.VAR_LONG);
	public static final DualCodec<ByteBuf, Vec3> VEC3 = new DualCodec<>(Vec3.CODEC, IECodecs.VEC3_STREAM_CODEC);
	public static final DualCodec<ByteBuf, Direction> DIRECTION = forEnum(Direction.values());
	public static final DualCodec<ByteBuf, BlockPos> BLOCK_POS = new DualCodec<>(BlockPos.CODEC, BlockPos.STREAM_CODEC);
	public static final DualCodec<ByteBuf, ResourceLocation> RESOURCE_LOCATION = new DualCodec<>(
			ResourceLocation.CODEC, ResourceLocation.STREAM_CODEC
	);
	public static final DualCodec<RegistryFriendlyByteBuf, Component> CHAT_COMPONENT = new DualCodec<>(
			ComponentSerialization.CODEC, ComponentSerialization.STREAM_CODEC
	);
	public static final DualCodec<RegistryFriendlyByteBuf, PotionContents> POTION_CONTENTS = new DualCodec<>(
			PotionContents.CODEC, PotionContents.STREAM_CODEC
	);
	public static final DualCodec<RegistryFriendlyByteBuf, Ingredient> INGREDIENT = new DualCodec<>(
			Ingredient.CODEC, Ingredient.CONTENTS_STREAM_CODEC
	);
	public static final DualCodec<RegistryFriendlyByteBuf, ItemStack> ITEM_STACK = new DualCodec<>(
			ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC
	);
	public static final DualCodec<RegistryFriendlyByteBuf, FluidStack> FLUID_STACK = new DualCodec<>(
			FluidStack.OPTIONAL_CODEC, FluidStack.OPTIONAL_STREAM_CODEC
	);
	public static final DualCodec<RegistryFriendlyByteBuf, NonNullList<Ingredient>> NONNULL_INGREDIENTS = new DualCodec<>(
			IECodecs.NONNULL_INGREDIENTS, Ingredient.CONTENTS_STREAM_CODEC.apply(IECodecs.nonNullList())
	);
	public static final DualCodec<RegistryFriendlyByteBuf, Recipe<?>> RECIPE = new DualCodec<>(
			Recipe.CODEC, Recipe.STREAM_CODEC
	);

	public static <T, S extends ByteBuf> DualCodec<S, T> unit(T value)
	{
		return new DualCodec<>(Codec.unit(value), StreamCodec.unit(value));
	}

	public static <S extends ByteBuf, T, E1, E2> DualCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			BiFunction<E1, E2, T> make
	)
	{
		return DualMapCodec.<S, T, E1, E2>composite(field1, get1, field2, get2, make).codec();
	}

	public static <S extends ByteBuf, T, E1, E2, E3> DualCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			Function3<E1, E2, E3, T> make
	)
	{
		return DualMapCodec.<S, T, E1, E2, E3>composite(field1, get1, field2, get2, field3, get3, make).codec();
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4> DualCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			DualMapCodec<? super S, E4> field4, Function<T, E4> get4,
			Function4<E1, E2, E3, E4, T> make
	)
	{
		return DualMapCodec.<S, T, E1, E2, E3, E4>composite(field1, get1, field2, get2, field3, get3, field4, get4, make).codec();
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4, E5> DualCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			DualMapCodec<? super S, E4> field4, Function<T, E4> get4,
			DualMapCodec<? super S, E5> field5, Function<T, E5> get5,
			Function5<E1, E2, E3, E4, E5, T> make
	)
	{
		return DualMapCodec.<S, T, E1, E2, E3, E4, E5>composite(field1, get1, field2, get2, field3, get3, field4, get4, field5, get5, make).codec();
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4, E5, E6> DualCodec<S, T> composite(
			DualMapCodec<? super S, E1> field1, Function<T, E1> get1,
			DualMapCodec<? super S, E2> field2, Function<T, E2> get2,
			DualMapCodec<? super S, E3> field3, Function<T, E3> get3,
			DualMapCodec<? super S, E4> field4, Function<T, E4> get4,
			DualMapCodec<? super S, E5> field5, Function<T, E5> get5,
			DualMapCodec<? super S, E6> field6, Function<T, E6> get6,
			Function6<E1, E2, E3, E4, E5, E6, T> make
	)
	{
		return DualMapCodec.<S, T, E1, E2, E3, E4, E5, E6>composite(field1, get1, field2, get2, field3, get3, field4, get4, field5, get5, field6, get6, make).codec();
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4, E5, E6, E7> DualCodec<S, T> composite(
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
		return DualMapCodec.<S, T, E1, E2, E3, E4, E5, E6, E7>composite(field1, get1, field2, get2, field3, get3, field4, get4, field5, get5, field6, get6, field7, get7, make).codec();
	}

	public static <S extends ByteBuf, T, E1, E2, E3, E4, E5, E6, E7, E8> DualCodec<S, T> composite(
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
		return DualMapCodec.<S, T, E1, E2, E3, E4, E5, E6, E7, E8>composite(field1, get1, field2, get2, field3, get3, field4, get4, field5, get5, field6, get6, field7, get7, field8, get8, make).codec();
	}

	public static <E extends Enum<E>>
	DualCodec<ByteBuf, E> forEnum(E[] values)
	{
		return new DualCodec<>(IECodecs.enumCodec(values), IECodecs.enumStreamCodec(values));
	}

	public static <E extends Enum<E>, V, S extends ByteBuf>
	DualCodec<S, EnumMap<E, V>> forEnumMap(E[] keys, DualCodec<? super S, V> valueCodec)
	{
		return new DualCodec<>(
				IECodecs.enumMapCodec(keys, valueCodec.codec()),
				IECodecs.enumMapStreamCodec(keys, valueCodec.streamCodec())
		);
	}

	public static <T>
	DualCodec<ByteBuf, TagKey<T>> tag(ResourceKey<? extends Registry<T>> registry)
	{
		return new DualCodec<>(TagKey.codec(registry), IECodecs.tagCodec(registry));
	}

	public static <T>
	DualCodec<ByteBuf, ResourceKey<T>> resourceKey(ResourceKey<? extends Registry<T>> registry)
	{
		return new DualCodec<>(ResourceKey.codec(registry), ResourceKey.streamCodec(registry));
	}

	public static <T>
	DualCodec<ByteBuf, T> registry(Registry<T> registry)
	{

		return resourceKey(registry.key())
				.map(registry::getOrThrow, t -> registry.getResourceKey(t).orElseThrow());
	}
}
