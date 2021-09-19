/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class WrappingRecipeSerializer<WrappingType extends Recipe<?>, WrappedType extends Recipe<?>>
		extends ForgeRegistryEntry<RecipeSerializer<?>>
		implements RecipeSerializer<WrappingType>
{
	private final RecipeSerializer<WrappedType> inner;
	private final Function<WrappingType, WrappedType> unwrap;
	private final Function<WrappedType, WrappingType> wrap;

	public WrappingRecipeSerializer(
			RecipeSerializer<WrappedType> inner, Function<WrappingType, WrappedType> unwrap,
			Function<WrappedType, WrappingType> wrap
	)
	{
		this.inner = inner;
		this.unwrap = unwrap;
		this.wrap = wrap;
	}

	@Nonnull
	@Override
	public WrappingType fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		return wrap.apply(inner.fromJson(recipeId, json));
	}

	@Nullable
	@Override
	public WrappingType fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
	{
		WrappedType vanilla = inner.fromNetwork(recipeId, buffer);
		if(vanilla!=null)
			return wrap.apply(vanilla);
		else
			return null;
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull WrappingType recipe)
	{
		inner.toNetwork(buffer, unwrap.apply(recipe));
	}
}
