/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;

public class RecipeSerializerRGB implements IRecipeSerializer<RecipeRGBColouration>
{
	public static final IRecipeSerializer<RecipeRGBColouration> INSTANCE = RecipeSerializers.register(
			new RecipeSerializerRGB()
	);

	@Nonnull
	@Override
	public RecipeRGBColouration read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		Ingredient target = Ingredient.fromJson(json.get("target"));
		String key = json.get("key").getAsString();
		return new RecipeRGBColouration(target, key, recipeId);
	}

	@Nonnull
	@Override
	public RecipeRGBColouration read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		Ingredient target = Ingredient.fromBuffer(buffer);
		String key = buffer.readString(512);
		return new RecipeRGBColouration(target, key, recipeId);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull RecipeRGBColouration recipe)
	{
		CraftingHelper.write(buffer, recipe.getTarget());
		buffer.writeString(recipe.getColorKey());
	}

	@Nonnull
	@Override
	public ResourceLocation getName()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, "rgb_colouration");
	}
}
