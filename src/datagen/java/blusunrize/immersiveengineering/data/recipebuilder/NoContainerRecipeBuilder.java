/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.recipebuilder;

import blusunrize.immersiveengineering.common.crafting.serializers.NoContainerSerializer;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.gson.JsonObject;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public record NoContainerRecipeBuilder(BiConsumer<RecipeOutput, ResourceLocation> baseRecipe)
{
	public void save(RecipeOutput out, ResourceLocation id)
	{
		Mutable<FinishedRecipe> finishedBaseRecipe = new MutableObject<>();
		baseRecipe().accept(new WrappingRecipeOutput(out, finishedBaseRecipe::setValue), id);
		out.accept(new FinishedRecipe()
		{
			@Override
			public void serializeRecipeData(@Nonnull JsonObject pJson)
			{
				pJson.add(NoContainerSerializer.BASE_RECIPE, finishedBaseRecipe.getValue().serializeRecipe());
			}

			@Nonnull
			@Override
			public ResourceLocation id()
			{
				return id;
			}

			@Nonnull
			@Override
			public RecipeSerializer<?> type()
			{
				return RecipeSerializers.NO_CONTAINER_SERIALIZER.get();
			}

			@Nullable
			@Override
			public JsonObject serializedAdvancement()
			{
				return finishedBaseRecipe.getValue().serializedAdvancement();
			}

			@Nullable
			@Override
			public AdvancementHolder advancement()
			{
				return finishedBaseRecipe.getValue().advancement();
			}
		});
	}
}
