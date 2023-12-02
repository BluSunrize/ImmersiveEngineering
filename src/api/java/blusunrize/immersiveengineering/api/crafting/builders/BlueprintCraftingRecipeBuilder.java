/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class BlueprintCraftingRecipeBuilder extends IEFinishedRecipe<BlueprintCraftingRecipeBuilder>
{
	private BlueprintCraftingRecipeBuilder()
	{
		super(BlueprintCraftingRecipe.SERIALIZER.value());
		setUseInputArray(6);
	}

	public static BlueprintCraftingRecipeBuilder builder(String category, ItemLike result)
	{
		return new BlueprintCraftingRecipeBuilder().addWriter(jsonObject -> jsonObject.addProperty("category", category)).addResult(result);
	}

	public static BlueprintCraftingRecipeBuilder builder(String category, ItemStack result)
	{
		return new BlueprintCraftingRecipeBuilder().addWriter(jsonObject -> jsonObject.addProperty("category", category)).addResult(result);
	}

	public static BlueprintCraftingRecipeBuilder builder(String category, TagKey<Item> result, int count)
	{
		return new BlueprintCraftingRecipeBuilder().addWriter(jsonObject -> jsonObject.addProperty("category", category)).addResult(new IngredientWithSize(result, count));
	}

}
