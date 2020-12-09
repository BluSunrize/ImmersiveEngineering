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
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class WrappingRecipeSerializer<WrappingType extends IRecipe<?>, WrappedType extends IRecipe<?>>
		extends ForgeRegistryEntry<IRecipeSerializer<?>>
		implements IRecipeSerializer<WrappingType>
{
	private final IRecipeSerializer<WrappedType> inner;
	private final Function<WrappingType, WrappedType> unwrap;
	private final Function<WrappedType, WrappingType> wrap;

	public WrappingRecipeSerializer(
			IRecipeSerializer<WrappedType> inner, Function<WrappingType, WrappedType> unwrap,
			Function<WrappedType, WrappingType> wrap
	)
	{
		this.inner = inner;
		this.unwrap = unwrap;
		this.wrap = wrap;
	}

	@Nonnull
	@Override
	public WrappingType read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		return wrap.apply(inner.read(recipeId, json));
	}

	@Nullable
	@Override
	public WrappingType read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		WrappedType vanilla = inner.read(recipeId, buffer);
		if(vanilla!=null)
			return wrap.apply(vanilla);
		else
			return null;
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull WrappingType recipe)
	{
		inner.write(buffer, unwrap.apply(recipe));
	}
}
