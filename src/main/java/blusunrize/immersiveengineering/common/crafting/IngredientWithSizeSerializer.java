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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;

public class IngredientWithSizeSerializer implements IIngredientWithSizeSerializer
{
	public static final IngredientWithSizeSerializer INSTANCE = new IngredientWithSizeSerializer();

	private static final String COUNT_KEY = "count";
	private static final String BASE_KEY = "base_ingredient";

	@Nonnull
	@Override
	public IngredientWithSize parse(@Nonnull PacketBuffer buffer)
	{
		final int count = buffer.readInt();
		final Ingredient base = Ingredient.read(buffer);
		return new IngredientWithSize(base, count);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull IngredientWithSize ingredient)
	{
		buffer.writeInt(ingredient.getCount());
		CraftingHelper.write(buffer, ingredient.getBaseIngredient());
	}

	@Nonnull
	@Override
	public IngredientWithSize parse(@Nonnull JsonElement json)
	{
		if(json.isJsonObject()&&json.getAsJsonObject().has(BASE_KEY))
		{
			final int count = JSONUtils.getInt(json.getAsJsonObject(), COUNT_KEY, 1);
			final JsonElement baseJson = json.getAsJsonObject().get(BASE_KEY);
			final Ingredient base = Ingredient.deserialize(baseJson);
			return new IngredientWithSize(base, count);
		}
		else //fallback for normal ingredients
		{
			final Ingredient base = Ingredient.deserialize(json);
			return new IngredientWithSize(base, 1);
		}
	}

	@Override
	public JsonElement write(@Nonnull IngredientWithSize ingredient)
	{
		if(ingredient.getCount()==1)
			return ingredient.getBaseIngredient().serialize();
		JsonObject json = new JsonObject();
		json.addProperty(COUNT_KEY, ingredient.getCount());
		json.add(BASE_KEY, ingredient.getBaseIngredient().serialize());
		return json;
	}
}
