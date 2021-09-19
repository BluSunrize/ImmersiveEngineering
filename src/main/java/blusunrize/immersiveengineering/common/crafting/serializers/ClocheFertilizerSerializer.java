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
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

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
		Ingredient input = Ingredient.fromJson(json.get("input"));
		float growthModifier = GsonHelper.getAsFloat(json, "growthModifier");
		return new ClocheFertilizer(recipeId, input, growthModifier);
	}

	@Nullable
	@Override
	public ClocheFertilizer fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		Ingredient input = Ingredient.fromNetwork(buffer);
		float growthModifier = buffer.readFloat();
		return new ClocheFertilizer(recipeId, input, growthModifier);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ClocheFertilizer recipe)
	{
		recipe.input.toNetwork(buffer);
		buffer.writeFloat(recipe.growthModifier);
	}
}
