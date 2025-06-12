/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class IERecipeSerializer<R extends Recipe<?>> implements RecipeSerializer<R>
{
	public static final Codec<List<StackWithChance>> CHANCE_LIST_CODEC = makeChanceOutputCodec();
	public static final DualCodec<RegistryFriendlyByteBuf, List<StackWithChance>> CHANCE_LIST_CODECS = new DualCodec<>(
			CHANCE_LIST_CODEC, StackWithChance.STREAM_LIST
	);

	public static DualMapCodec<RegistryFriendlyByteBuf, TagOutput> optionalItemOutput(String name)
	{
		return TagOutput.CODECS.optionalFieldOf(name, TagOutput.EMPTY);
	}

	public static DualMapCodec<RegistryFriendlyByteBuf, FluidStack> optionalFluidOutput(String name)
	{
		return IEDualCodecs.FLUID_STACK.optionalFieldOf(name, FluidStack.EMPTY);
	}

	public abstract ItemStack getIcon();

	protected abstract DualMapCodec<RegistryFriendlyByteBuf, R> codecs();

	@Override
	public final MapCodec<R> codec()
	{
		return codecs().mapCodec();
	}

	@Override
	public final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec()
	{
		return codecs().streamCodec();
	}

	protected static <S extends ByteBuf, T> DualMapCodec<S, Optional<List<T>>> maybeListOrSingle(DualCodec<S, T> singleCodec, String key)
	{
		var listOrSingle = listOrSingle(singleCodec, key);
		return new DualMapCodec<S, Optional<List<T>>>(
				Codec.mapEither(listOrSingle.mapCodec(), Codec.EMPTY).<Optional<List<T>>>xmap(
						e -> e.map(Optional::of, $ -> Optional.empty()),
						o -> o.isPresent()?Either.left(o.get()): Either.right(Unit.INSTANCE)
				),
				listOrSingle.streamCodec().map(Optional::of, Optional::orElseThrow)
		);
	}

	protected static <S extends ByteBuf, T> DualMapCodec<S, List<T>> listOrSingle(DualCodec<S, T> singleCodec, String key)
	{
		return listOrSingle(singleCodec, key, key);
	}

	protected static <S extends ByteBuf, T> DualMapCodec<S, List<T>> listOrSingle(DualCodec<S, T> singleCodec, String singleKey, String listKey)
	{
		return new DualMapCodec<>(
				Codec.mapEither(
						singleCodec.codec().fieldOf(singleKey), singleCodec.codec().listOf().fieldOf(listKey)
				).xmap(
						e -> e.map(List::of, Function.identity()),
						l -> l.size()==1?Either.left(l.get(0)): Either.right(l)
				),
				singleCodec.listOf().streamCodec()
		);
	}

	private static Codec<List<StackWithChance>> makeChanceOutputCodec()
	{
		Codec<List<StackWithChance>> baseCodec = StackWithChance.OPTIONAL_BASIC_CODEC.listOf();
		Decoder<List<StackWithChance>> conditionalDecoder = new Decoder<>()
		{
			@Override
			public <T> DataResult<Pair<List<StackWithChance>, T>> decode(DynamicOps<T> ops, T input)
			{
				Codec<IContext> contextCodec = ConditionalOps.retrieveContext().codec();
				return baseCodec.decode(ops, input).flatMap(
						listAndData -> contextCodec.decode(ops, ops.emptyMap()).map(ctxAndData -> {
							List<StackWithChance> filtered = new ArrayList<>();
							for(StackWithChance stack : listAndData.getFirst())
							{
								boolean matches = true;
								for(ICondition condition : stack.conditions())
									matches &= condition.test(ctxAndData.getFirst());
								if(matches)
									filtered.add(stack);
							}
							return Pair.of(filtered, ctxAndData.getSecond());
						}));
			}
		};
		return Codec.of(baseCodec, conditionalDecoder);
	}
}
