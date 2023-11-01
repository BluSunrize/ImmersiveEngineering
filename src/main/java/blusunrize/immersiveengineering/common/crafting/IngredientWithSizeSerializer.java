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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CraftingHelper;

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
		CraftingHelper.write(buffer, ingredient.getBaseIngredient());
	}

	@Nonnull
	@Override
	public IngredientWithSize parse(@Nonnull JsonElement json)
	{
		if(json.isJsonObject()&&json.getAsJsonObject().has(BASE_KEY))
		{
			final int count = GsonHelper.getAsInt(json.getAsJsonObject(), COUNT_KEY, 1);
			final JsonElement baseJson = json.getAsJsonObject().get(BASE_KEY);
			final Ingredient base = Ingredient.fromJson(baseJson);
			return new IngredientWithSize(base, count);
		}
		else //fallback for normal ingredients
		{
			final Ingredient base = Ingredient.fromJson(json);
			return new IngredientWithSize(base, 1);
		}
	}

	@Override
	public JsonElement write(@Nonnull IngredientWithSize ingredient)
	{
		if(ingredient.getCount()==1)
			return ingredient.getBaseIngredient().toJson();
		JsonObject json = new JsonObject();
		json.addProperty(COUNT_KEY, ingredient.getCount());
		json.add(BASE_KEY, ingredient.getBaseIngredient().toJson());
		return json;
	}
}
