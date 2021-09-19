/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.recipebuilder;

import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class GeneratedListRecipeBuilder
{
	public GeneratedListRecipeBuilder()
	{
	}

	public static void build(Consumer<FinishedRecipe> consumerIn, final ResourceLocation id)
	{
		Preconditions.checkArgument(GeneratedListRecipe.LIST_GENERATORS.containsKey(id));
		consumerIn.accept(new FinishedRecipe()
		{
			public void serializeRecipeData(@Nonnull JsonObject json)
			{
			}

			@Nonnull
			public RecipeSerializer<?> getType()
			{
				return GeneratedListRecipe.SERIALIZER.get();
			}

			@Nonnull
			public ResourceLocation getId()
			{
				return id;
			}

			@Nullable
			public JsonObject serializeAdvancement()
			{
				return null;
			}

			public ResourceLocation getAdvancementId()
			{
				return new ResourceLocation("");
			}
		});
	}
}
