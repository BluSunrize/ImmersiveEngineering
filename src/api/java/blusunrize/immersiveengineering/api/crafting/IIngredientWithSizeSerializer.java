/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public interface IIngredientWithSizeSerializer
{
	IngredientWithSize parse(@Nonnull FriendlyByteBuf buffer);

	void write(@Nonnull FriendlyByteBuf buffer, @Nonnull IngredientWithSize ingredient);

	IngredientWithSize parse(@Nonnull JsonElement json);

	JsonElement write(@Nonnull IngredientWithSize ingredient);
}
