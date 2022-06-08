/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.RGBColourationRecipe;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;

public class RGBRecipeSerializer implements RecipeSerializer<RGBColourationRecipe>
{
	@Nonnull
	@Override
	public RGBColourationRecipe fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		Ingredient target = Ingredient.fromJson(json.get("target"));
		String key = json.get("key").getAsString();
		return new RGBColourationRecipe(target, key, recipeId);
	}

	@Nonnull
	@Override
	public RGBColourationRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
	{
		Ingredient target = Ingredient.fromNetwork(buffer);
		String key = buffer.readUtf(512);
		return new RGBColourationRecipe(target, key, recipeId);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RGBColourationRecipe recipe)
	{
		CraftingHelper.write(buffer, recipe.getTarget());
		buffer.writeUtf(recipe.getColorKey());
	}
}
