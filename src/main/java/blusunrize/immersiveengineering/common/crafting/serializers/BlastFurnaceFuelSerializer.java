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
import blusunrize.immersiveengineering.common.register.IEItems;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;

import javax.annotation.Nullable;

public class BlastFurnaceFuelSerializer extends IERecipeSerializer<BlastFurnaceFuel>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(IEItems.Ingredients.COAL_COKE);
	}

	@Override
	public BlastFurnaceFuel readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		Ingredient input = Ingredient.fromJson(json.getAsJsonObject("input"));
		int time = GsonHelper.getAsInt(json, "time", 1200);
		return new BlastFurnaceFuel(recipeId, input, time);
	}

	@Nullable
	@Override
	public BlastFurnaceFuel fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		Ingredient input = Ingredient.fromNetwork(buffer);
		int time = buffer.readInt();
		return new BlastFurnaceFuel(recipeId, input, time);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, BlastFurnaceFuel recipe)
	{
		recipe.input.toNetwork(buffer);
		buffer.writeInt(recipe.burnTime);
	}
}
