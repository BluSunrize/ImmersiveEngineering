/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.IEApi;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class IERecipeSerializer<R extends Recipe<?>> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<R>
{
	public abstract ItemStack getIcon();

	@Override
	public final R fromJson(ResourceLocation recipeId, JsonObject json)
	{
		if(CraftingHelper.processConditions(json, "conditions"))
			return readFromJson(recipeId, json);
		return null;
	}

	protected ItemStack readOutput(JsonElement outputObject)
	{
		if(outputObject.isJsonObject()&&outputObject.getAsJsonObject().has("item"))
			return ShapedRecipe.itemFromJson(outputObject.getAsJsonObject());
		IngredientWithSize outgredient = IngredientWithSize.deserialize(outputObject);
		return IEApi.getPreferredStackbyMod(outgredient.getMatchingStacks());
	}

	public abstract R readFromJson(ResourceLocation recipeId, JsonObject json);
}
