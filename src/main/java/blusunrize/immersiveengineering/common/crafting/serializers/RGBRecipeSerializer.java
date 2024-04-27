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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RGBRecipeSerializer implements RecipeSerializer<RGBColourationRecipe>
{
	public static final MapCodec<RGBColourationRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Ingredient.CODEC.fieldOf("target").forGetter(RGBColourationRecipe::getTarget),
			Codec.STRING.fieldOf("key").forGetter(RGBColourationRecipe::getColorKey)
	).apply(inst, RGBColourationRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, RGBColourationRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, RGBColourationRecipe::getTarget,
			ByteBufCodecs.stringUtf8(512), RGBColourationRecipe::getColorKey,
			RGBColourationRecipe::new
	);

	@Override
	public MapCodec<RGBColourationRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RGBColourationRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
