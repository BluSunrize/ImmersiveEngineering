/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.codec;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class IEDualCodecs
{
	public static final DualCodec<RegistryFriendlyByteBuf, FluidStack> FLUID_STACK = new DualCodec<>(
			FluidStack.OPTIONAL_CODEC, FluidStack.OPTIONAL_STREAM_CODEC
	);

	public static final DualCodec<RegistryFriendlyByteBuf, SizedFluidIngredient> SIZED_FLUID_INGREDIENT = new DualCodec<>(
			SizedFluidIngredient.FLAT_CODEC,
			SizedFluidIngredient.STREAM_CODEC
	);

	public static final DualCodec<ByteBuf, Vec3> VEC3 = new DualCodec<>(
			Vec3.CODEC, IEStreamCodecs.VEC3_STREAM_CODEC
	);

	public static final DualCodec<RegistryFriendlyByteBuf, NonNullList<Ingredient>> NONNULL_INGREDIENTS = new DualCodec<>(
			IECodecs.NONNULL_INGREDIENTS, Ingredient.CONTENTS_STREAM_CODEC.apply(IEStreamCodecs.nonNullList())
	);

	public static final DualCodec<ByteBuf, List<ICondition>> CONDITIONS = new DualCodec<>(
			ICondition.LIST_CODEC, IECodecs.lenientUnitStream(List.of())
	);

	public static <E extends Enum<E>> DualCodec<ByteBuf, E> forEnum(E[] values)
	{
		return new DualCodec<>(IECodecs.enumCodec(values), IEStreamCodecs.enumStreamCodec(values));
	}

	public static <B extends ByteBuf, K, V> DualCodec<B, Map<K, V>> forMap(
			DualCodec<? super B, K> keyCodec, DualCodec<? super B, V> valueCodec
	)
	{
		DualCodec<B, Map.Entry<K, V>> entryCodec = DualCompositeCodecs.composite(
				keyCodec.fieldOf("key"), Entry::getKey,
				valueCodec.fieldOf("value"), Entry::getValue,
				SimpleEntry::new
		);
		return entryCodec.listOf().map(
				l -> l.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue)),
				m -> ImmutableList.copyOf(m.entrySet())
		);
	}

	public static <T> DualCodec<ByteBuf, TagKey<T>> tag(ResourceKey<Registry<T>> registry)
	{
		return new DualCodec<>(TagKey.codec(registry), IEStreamCodecs.tagCodec(registry));
	}
}
