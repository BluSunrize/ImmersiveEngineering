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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;

public class BlueprintCraftingRecipeBuilder extends IEFinishedRecipe<BlueprintCraftingRecipeBuilder>
{
	private BlueprintCraftingRecipeBuilder()
	{
		super(BlueprintCraftingRecipe.SERIALIZER.get());
		setUseInputArray(6);
	}

	public static BlueprintCraftingRecipeBuilder builder(String category, IItemProvider result)
	{
		return new BlueprintCraftingRecipeBuilder().addWriter(jsonObject -> jsonObject.addProperty("category", category)).addResult(result);
	}

	public static BlueprintCraftingRecipeBuilder builder(String category, ItemStack result)
	{
		return new BlueprintCraftingRecipeBuilder().addWriter(jsonObject -> jsonObject.addProperty("category", category)).addResult(result);
	}

	public static BlueprintCraftingRecipeBuilder builder(String category, ITag<Item> result, int count)
	{
		return new BlueprintCraftingRecipeBuilder().addWriter(jsonObject -> jsonObject.addProperty("category", category)).addResult(new IngredientWithSize(result, count));
	}

}
