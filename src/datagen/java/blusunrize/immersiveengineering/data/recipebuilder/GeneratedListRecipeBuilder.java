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
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class GeneratedListRecipeBuilder
{
	public GeneratedListRecipeBuilder()
	{
	}

	public static void build(Consumer<IFinishedRecipe> consumerIn, final ResourceLocation id)
	{
		Preconditions.checkArgument(GeneratedListRecipe.LIST_GENERATORS.containsKey(id));
		consumerIn.accept(new IFinishedRecipe()
		{
			public void serialize(@Nonnull JsonObject json)
			{
			}

			@Nonnull
			public IRecipeSerializer<?> getSerializer()
			{
				return GeneratedListRecipe.SERIALIZER.get();
			}

			@Nonnull
			public ResourceLocation getID()
			{
				return id;
			}

			@Nullable
			public JsonObject getAdvancementJson()
			{
				return null;
			}

			public ResourceLocation getAdvancementID()
			{
				return new ResourceLocation("");
			}
		});
	}
}
