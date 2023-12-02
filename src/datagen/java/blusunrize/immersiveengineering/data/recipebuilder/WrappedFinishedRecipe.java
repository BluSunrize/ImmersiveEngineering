/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.recipebuilder;

import com.google.gson.JsonObject;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.core.Holder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class WrappedFinishedRecipe implements FinishedRecipe
{
	private final FinishedRecipe base;
	private final RecipeSerializer<?> serializer;

	public WrappedFinishedRecipe(
			FinishedRecipe base, Supplier<? extends RecipeSerializer<?>> serializer
	)
	{
		this.base = base;
		this.serializer = serializer.get();
	}

	@Override
	public void serializeRecipeData(@Nonnull JsonObject json)
	{
		base.serializeRecipeData(json);
	}

	@Nonnull
	@Override
	public ResourceLocation id()
	{
		return base.id();
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> type()
	{
		return serializer;
	}

	@Nullable
	@Override
	public AdvancementHolder advancement()
	{
		return base.advancement();
	}
}
