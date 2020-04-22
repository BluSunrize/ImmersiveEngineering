/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;

public class IngredientWithSizeSerializer
{
	public static final IngredientWithSizeSerializer INSTANCE = new IngredientWithSizeSerializer();

	private static final String COUNT_KEY = "count";
	private static final String BASE_KEY = "base_ingredient";

	@Nonnull
	public IngredientWithSize parse(@Nonnull PacketBuffer buffer)
	{
		final int count = buffer.readInt();
		final Ingredient base = Ingredient.read(buffer);
		return new IngredientWithSize(base, count);
	}

	public void write(@Nonnull PacketBuffer buffer, @Nonnull IngredientWithSize ingredient)
	{
		buffer.writeInt(ingredient.getCount());
		CraftingHelper.write(buffer, ingredient.getBaseIngredient());
	}

	@Nonnull
	public IngredientWithSize parse(@Nonnull JsonObject json)
	{
		final int count = JSONUtils.getInt(json, COUNT_KEY, 1);
		final JsonObject baseJson = json.getAsJsonObject(BASE_KEY);
		final Ingredient base = Ingredient.deserialize(baseJson);
		return new IngredientWithSize(base, count);
	}

	public void write(@Nonnull JsonObject json, @Nonnull IngredientWithSize ingredient)
	{
		json.addProperty(COUNT_KEY, ingredient.getCount());
		json.add(BASE_KEY, ingredient.getBaseIngredient().serialize());
	}
}
