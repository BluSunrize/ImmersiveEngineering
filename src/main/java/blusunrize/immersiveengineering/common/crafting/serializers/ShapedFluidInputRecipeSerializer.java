/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.common.crafting.IngredientFluidStack;
import blusunrize.immersiveengineering.common.crafting.ShapedFluidInputRecipe;
import blusunrize.immersiveengineering.mixin.accessors.ShapedRecipeAccess;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;

public class ShapedFluidInputRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
		implements IRecipeSerializer<ShapedFluidInputRecipe>
{
	@Nonnull
	@Override
	public ShapedFluidInputRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		String group = JSONUtils.getString(json, "group", "");
		Map<String, Ingredient> ingredientsByName = ShapedRecipeAccess.invokeDeserializeKey(
				JSONUtils.getJsonObject(json, "key")
		);
		addFluidKeysToMap(JSONUtils.getJsonObject(json, "fluidKey"), ingredientsByName);
		String[] pattern = ShapedRecipeAccess.invokeShrink(
				ShapedRecipeAccess.invokePatternFromJson(JSONUtils.getJsonArray(json, "pattern"))
		);
		int width = pattern[0].length();
		int height = pattern.length;
		NonNullList<Ingredient> ingredients = ShapedRecipeAccess.invokeDeserializeIngredients(
				pattern, ingredientsByName, width, height
		);
		ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
		return new ShapedFluidInputRecipe(recipeId, group, width, height, ingredients, itemstack);
	}

	private void addFluidKeysToMap(JsonObject json, Map<String, Ingredient> output)
	{
		for(Entry<String, JsonElement> entry : json.entrySet())
		{
			Preconditions.checkState(!output.containsKey(entry.getKey()));
			FluidTagInput fluid = FluidTagInput.deserialize(entry.getValue());
			output.put(entry.getKey(), new IngredientFluidStack(fluid));
		}
	}

	//TODO
	@Nullable
	@Override
	public ShapedFluidInputRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		return null;
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull ShapedFluidInputRecipe recipe)
	{

	}
}
