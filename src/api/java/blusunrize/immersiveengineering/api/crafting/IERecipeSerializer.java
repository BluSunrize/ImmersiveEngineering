/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import net.minecraft.network.FriendlyByteBuf;
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
	public static final Codec<List<StackWithChance>> CHANCE_LIST = makeChanceOutputCodec();

	public static MapCodec<TagOutput> optionalItemOutput(String name)
	{
		return TagOutput.CODEC.optionalFieldOf(name, TagOutput.EMPTY);
	}

	public static MapCodec<FluidStack> optionalFluidOutput(String name)
	{
		return FluidStack.CODEC.optionalFieldOf(name, FluidStack.EMPTY);
	}

	public abstract ItemStack getIcon();

	protected static TagOutput readLazyStack(FriendlyByteBuf buf)
	{
		return new TagOutput(buf.readItem());
	}

	protected static void writeLazyStack(FriendlyByteBuf buf, TagOutput stack)
	{
		buf.writeItem(stack.get());
	}

	protected static <T> MapCodec<Optional<List<T>>> maybeListOrSingle(Codec<T> singleCodec, String key)
	{
		return Codec.mapEither(listOrSingle(singleCodec, key), Codec.EMPTY).xmap(
				e -> e.map(Optional::of, $ -> Optional.empty()),
				o -> o.isPresent()?Either.left(o.get()): Either.right(Unit.INSTANCE)
		);
	}

	protected static <T> MapCodec<List<T>> listOrSingle(Codec<T> singleCodec, String key)
	{
		return listOrSingle(singleCodec, key, key);
	}

	protected static <T> MapCodec<List<T>> listOrSingle(Codec<T> singleCodec, String singleKey, String listKey)
	{
		return Codec.mapEither(
				singleCodec.fieldOf(singleKey), singleCodec.listOf().fieldOf(listKey)
		).xmap(
				e -> e.map(List::of, Function.identity()),
				l -> l.size()==1?Either.left(l.get(0)): Either.right(l)
		);
	}

	private static Codec<List<StackWithChance>> makeChanceOutputCodec()
	{
		Codec<List<StackWithChance>> baseCodec = StackWithChance.CODEC.listOf();
		Decoder<List<StackWithChance>> conditionalDecoder = new Decoder<>()
		{
			@Override
			public <T> DataResult<Pair<List<StackWithChance>, T>> decode(DynamicOps<T> ops, T input)
			{
				Codec<IContext> contextCodec = ConditionalOps.retrieveContext().codec();
				return baseCodec.decode(ops, input).flatMap(
						listAndData -> contextCodec.decode(ops, input).map(ctxAndData -> {
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
