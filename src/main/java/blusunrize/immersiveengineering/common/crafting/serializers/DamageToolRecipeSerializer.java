/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.utils.IECodecs;
import blusunrize.immersiveengineering.common.crafting.DamageToolRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class DamageToolRecipeSerializer implements RecipeSerializer<DamageToolRecipe>
{
	public static final MapCodec<DamageToolRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.STRING.fieldOf("group").forGetter(ShapelessRecipe::getGroup),
			ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
			Ingredient.CODEC.fieldOf("tool").forGetter(DamageToolRecipe::getTool),
			IECodecs.NONNULL_INGREDIENTS.fieldOf("ingredients").forGetter(ShapelessRecipe::getIngredients)
	).apply(inst, DamageToolRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, DamageToolRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.stringUtf8(512), ShapelessRecipe::getGroup,
			ItemStack.STREAM_CODEC, r -> r.getResultItem(null),
			Ingredient.CONTENTS_STREAM_CODEC, DamageToolRecipe::getTool,
			Ingredient.CONTENTS_STREAM_CODEC.apply(IECodecs.nonNullList()), ShapelessRecipe::getIngredients,
			DamageToolRecipe::new
	);

	@Override
	public MapCodec<DamageToolRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, DamageToolRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}