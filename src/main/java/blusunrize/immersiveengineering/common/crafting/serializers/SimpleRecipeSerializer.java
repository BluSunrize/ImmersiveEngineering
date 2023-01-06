/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record SimpleRecipeSerializer<R extends Recipe<?>>(
		Function<ResourceLocation, R> create
) implements RecipeSerializer<R>
{
	@Override
	public R fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe)
	{
		return create.apply(pRecipeId);
	}

	@Override
	public @Nullable R fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer)
	{
		return create.apply(pRecipeId);
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, R pRecipe)
	{
	}
}
