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
import net.minecraft.util.IItemProvider;

public class MetalPressRecipeBuilder extends IEFinishedRecipe<MetalPressRecipeBuilder>
{
	private MetalPressRecipeBuilder()
	{
		super(MetalPressRecipe.SERIALIZER.get());
	}

	public static MetalPressRecipeBuilder builder(IItemProvider mold, IItemProvider result)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(result);
	}

	public static MetalPressRecipeBuilder builder(IItemProvider mold, ItemStack result)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(result);
	}

	public static MetalPressRecipeBuilder builder(IItemProvider mold, ITag<Item> result, int count)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(new IngredientWithSize(result, count));
	}

}
