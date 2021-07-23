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
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class MetalPressRecipeBuilder extends IEFinishedRecipe<MetalPressRecipeBuilder>
{
	private MetalPressRecipeBuilder()
	{
		super(MetalPressRecipe.SERIALIZER.get());
	}

	public static MetalPressRecipeBuilder builder(ItemLike mold, ItemLike result)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(result);
	}

	public static MetalPressRecipeBuilder builder(ItemLike mold, ItemStack result)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(result);
	}

	public static MetalPressRecipeBuilder builder(ItemLike mold, Tag<Item> result, int count)
	{
		return new MetalPressRecipeBuilder().addIngredient("mold", mold).addResult(new IngredientWithSize(result, count));
	}

}
