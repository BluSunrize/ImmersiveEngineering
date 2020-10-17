/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.utils.IEPacketBuffer;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClocheFertilizerSerializer extends IERecipeSerializer<ClocheFertilizer>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Items.BONE_MEAL);
	}

	@Override
	public ClocheFertilizer readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		Ingredient input = Ingredient.deserialize(json.get("input"));
		float growthModifier = JSONUtils.getFloat(json, "growthModifier");
		return new ClocheFertilizer(recipeId, input, growthModifier);
	}

	@Nullable
	@Override
	public ClocheFertilizer read(ResourceLocation recipeId, @Nonnull IEPacketBuffer buffer)
	{
		Ingredient input = Ingredient.read(buffer);
		float growthModifier = buffer.readFloat();
		return new ClocheFertilizer(recipeId, input, growthModifier);
	}

	@Override
	public void write(@Nonnull IEPacketBuffer buffer, ClocheFertilizer recipe)
	{
		recipe.input.write(buffer);
		buffer.writeFloat(recipe.growthModifier);
	}
}
