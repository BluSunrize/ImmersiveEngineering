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
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

//TODO new interface technically breaks binary compat?
public abstract class IERecipeSerializer<R extends IRecipe<?>> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IIEBufferRecipeSerializer<R>
{
	public abstract ItemStack getIcon();

	@Override
	public final R read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		if(CraftingHelper.processConditions(json, "conditions"))
			return readFromJson(recipeId, json);
		return null;
	}

	protected ItemStack readOutput(JsonElement outputObject)
	{
		if(outputObject.isJsonObject() && outputObject.getAsJsonObject().has("item"))
			return ShapedRecipe.deserializeItem(outputObject.getAsJsonObject());
		IngredientWithSize outgredient = IngredientWithSize.deserialize(outputObject);
		return IEApi.getPreferredStackbyMod(outgredient.getMatchingStacks());
	}

	public abstract R readFromJson(ResourceLocation recipeId, JsonObject json);
}
