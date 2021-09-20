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
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record NoContainerRecipeBuilder(BiConsumer<Consumer<FinishedRecipe>, ResourceLocation> baseRecipe)
{
	public void save(Consumer<FinishedRecipe> out, ResourceLocation id)
	{
		Mutable<FinishedRecipe> finishedBaseRecipe = new MutableObject<>();
		baseRecipe().accept(finishedBaseRecipe::setValue, id);
		out.accept(new FinishedRecipe()
		{
			@Override
			public void serializeRecipeData(@Nonnull JsonObject pJson)
			{
				pJson.add(NoContainerSerializer.BASE_RECIPE, finishedBaseRecipe.getValue().serializeRecipe());
			}

			@Nonnull
			@Override
			public ResourceLocation getId()
			{
				return id;
			}

			@Nonnull
			@Override
			public RecipeSerializer<?> getType()
			{
				return RecipeSerializers.NO_CONTAINER_SERIALIZER.get();
			}

			@Nullable
			@Override
			public JsonObject serializeAdvancement()
			{
				return finishedBaseRecipe.getValue().serializeAdvancement();
			}

			@Nullable
			@Override
			public ResourceLocation getAdvancementId()
			{
				return finishedBaseRecipe.getValue().getAdvancementId();
			}
		});
	}
}
