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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;

public class RGBRecipeSerializer implements RecipeSerializer<RGBColourationRecipe>
{
	public static final Codec<RGBColourationRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Ingredient.CODEC.fieldOf("target").forGetter(RGBColourationRecipe::getTarget),
			Codec.STRING.fieldOf("key").forGetter(RGBColourationRecipe::getColorKey)
	).apply(inst, RGBColourationRecipe::new));

	@Override
	public Codec<RGBColourationRecipe> codec()
	{
		return CODEC;
	}

	@Nonnull
	@Override
	public RGBColourationRecipe fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		Ingredient target = Ingredient.fromNetwork(buffer);
		String key = buffer.readUtf(512);
		return new RGBColourationRecipe(target, key);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RGBColourationRecipe recipe)
	{
		recipe.getTarget().toNetwork(buffer);
		buffer.writeUtf(recipe.getColorKey());
	}
}
