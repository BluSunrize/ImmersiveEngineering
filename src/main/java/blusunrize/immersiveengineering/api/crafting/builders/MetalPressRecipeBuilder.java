/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;

public class MetalPressRecipeBuilder extends IEFinishedRecipe<MetalPressRecipeBuilder>
{
	private MetalPressRecipeBuilder()
	{
		super(MetalPressRecipe.SERIALIZER.get());
	}

	public static MetalPressRecipeBuilder builder(Item mold, Item result)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(result);
	}

	public static MetalPressRecipeBuilder builder(Item mold, ItemStack result)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(result);
	}

	public static MetalPressRecipeBuilder builder(Item mold, ITag<Item> result, int count)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(new IngredientWithSize(result, count));
	}

}
