/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

public class WrappingRecipeSerializer<WrappingType extends Recipe<?>, WrappedType extends Recipe<?>>
		implements RecipeSerializer<WrappingType>
{
	private final MapCodec<WrappingType> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, WrappingType> streamCodec;

	public WrappingRecipeSerializer(
			RecipeSerializer<WrappedType> inner,
			Function<WrappingType, WrappedType> unwrap,
			Function<WrappedType, WrappingType> wrap
	)
	{
		this.codec = inner.codec().xmap(wrap, unwrap);
		this.streamCodec = inner.streamCodec().map(wrap, unwrap);
	}

	@Override
	public MapCodec<WrappingType> codec()
	{
		return codec;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, WrappingType> streamCodec()
	{
		return streamCodec;
	}
}
