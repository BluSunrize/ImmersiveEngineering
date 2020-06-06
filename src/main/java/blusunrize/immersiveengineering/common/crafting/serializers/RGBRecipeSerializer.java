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
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RGBRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RGBColourationRecipe>
{
	@Nonnull
	@Override
	public RGBColourationRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		Ingredient target = Ingredient.deserialize(json.get("target"));
		String key = json.get("key").getAsString();
		return new RGBColourationRecipe(target, key, recipeId);
	}

	@Nonnull
	@Override
	public RGBColourationRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		Ingredient target = Ingredient.read(buffer);
		String key = buffer.readString(512);
		return new RGBColourationRecipe(target, key, recipeId);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull RGBColourationRecipe recipe)
	{
		CraftingHelper.write(buffer, recipe.getTarget());
		buffer.writeString(recipe.getColorKey());
	}
}
