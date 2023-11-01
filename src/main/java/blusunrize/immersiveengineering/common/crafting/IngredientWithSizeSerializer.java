/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IIngredientWithSizeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class IngredientWithSizeSerializer implements IIngredientWithSizeSerializer
{
	public static final IngredientWithSizeSerializer INSTANCE = new IngredientWithSizeSerializer();

	private static final String COUNT_KEY = "count";
	private static final String BASE_KEY = "base_ingredient";

	@Nonnull
	@Override
	public IngredientWithSize parse(@Nonnull FriendlyByteBuf buffer)
	{
		final int count = buffer.readInt();
		final Ingredient base = Ingredient.fromNetwork(buffer);
		return new IngredientWithSize(base, count);
	}

	@Override
	public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull IngredientWithSize ingredient)
	{
		buffer.writeInt(ingredient.getCount());
		ingredient.getBaseIngredient().toNetwork(buffer);
	}
}
