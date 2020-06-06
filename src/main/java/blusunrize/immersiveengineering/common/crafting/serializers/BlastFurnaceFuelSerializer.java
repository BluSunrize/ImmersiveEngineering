/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.items.IEItems;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class BlastFurnaceFuelSerializer extends IERecipeSerializer<BlastFurnaceFuel>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(IEItems.Ingredients.coalCoke);
	}

	@Override
	public BlastFurnaceFuel readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		Ingredient input = Ingredient.deserialize(json.getAsJsonObject("input"));
		int time = JSONUtils.getInt(json, "time", 1200);
		return new BlastFurnaceFuel(recipeId, input, time);
	}

	@Nullable
	@Override
	public BlastFurnaceFuel read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		Ingredient input = Ingredient.read(buffer);
		int time = buffer.readInt();
		return new BlastFurnaceFuel(recipeId, input, time);
	}

	@Override
	public void write(PacketBuffer buffer, BlastFurnaceFuel recipe)
	{
		recipe.input.write(buffer);
		buffer.writeInt(recipe.burnTime);
	}
}
