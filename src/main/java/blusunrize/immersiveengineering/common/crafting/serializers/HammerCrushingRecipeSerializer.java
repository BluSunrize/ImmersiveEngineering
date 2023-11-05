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
import blusunrize.immersiveengineering.common.crafting.LazyShapelessRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;

public class HammerCrushingRecipeSerializer extends IERecipeSerializer<LazyShapelessRecipe>
{
	private final Codec<LazyShapelessRecipe> codec = RecordCodecBuilder.create(inst -> inst.group(
			LAZY_OUTPUT_CODEC.fieldOf("result").forGetter(LazyShapelessRecipe::getResult),
			Ingredient.CODEC.xmap(i -> NonNullList.withSize(1, i), l -> l.get(0))
					.fieldOf("input").forGetter(ShapelessRecipe::getIngredients)
	).apply(inst, (result, ingredients) -> new LazyShapelessRecipe("", result, ingredients, this)));

	@Override
	public Codec<LazyShapelessRecipe> codec()
	{
		return codec;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Blocks.CRAFTING_TABLE);
	}

	@Nonnull
	@Override
	public LazyShapelessRecipe fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		int count = buffer.readInt();
		NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
		for(int i = 0; i < count; i++)
			ingredients.set(i, Ingredient.fromNetwork(buffer));
		Lazy<ItemStack> output = readLazyStack(buffer);
		return new LazyShapelessRecipe("", output, ingredients, this);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull LazyShapelessRecipe recipe)
	{
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		buffer.writeInt(ingredients.size());
		for(Ingredient ingredient : ingredients)
			ingredient.toNetwork(buffer);
		buffer.writeItem(recipe.getResultItem(null));
	}
}
