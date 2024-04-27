/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.common.crafting.LazyShapelessRecipe;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public class HammerCrushingRecipeSerializer extends IERecipeSerializer<LazyShapelessRecipe>
{
	private final MapCodec<LazyShapelessRecipe> codec = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(LazyShapelessRecipe::getResult),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.getIngredients().get(0))
	).apply(inst, (result, input) -> new LazyShapelessRecipe(
			"", result, NonNullList.of(Ingredient.EMPTY, input, Ingredient.of(IEItems.Tools.HAMMER)), this
	)));
	private final StreamCodec<RegistryFriendlyByteBuf, LazyShapelessRecipe> streamCodec = StreamCodec.composite(
			TagOutput.STREAM_CODEC, LazyShapelessRecipe::getResult,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.getIngredients().get(0),
			(result, input) -> new LazyShapelessRecipe(
					"", result, NonNullList.of(Ingredient.EMPTY, input, Ingredient.of(IEItems.Tools.HAMMER)), this
			)
	);

	@Override
	public MapCodec<LazyShapelessRecipe> codec()
	{
		return codec;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, LazyShapelessRecipe> streamCodec()
	{
		return streamCodec;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Blocks.CRAFTING_TABLE);
	}
}
