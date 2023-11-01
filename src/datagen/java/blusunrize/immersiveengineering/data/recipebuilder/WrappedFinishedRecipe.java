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
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WrappedFinishedRecipe implements FinishedRecipe
{
	private final FinishedRecipe base;
	private final RecipeSerializer<?> serializer;

	public WrappedFinishedRecipe(
			FinishedRecipe base, RegistryObject<? extends RecipeSerializer<?>> serializer
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
	public ResourceLocation getId()
	{
		return base.getId();
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getType()
	{
		return serializer;
	}

	@Nullable
	@Override
	public JsonObject serializeAdvancement()
	{
		return base.serializeAdvancement();
	}

	@Nullable
	@Override
	public ResourceLocation getAdvancementId()
	{
		return base.getAdvancementId();
	}
}
