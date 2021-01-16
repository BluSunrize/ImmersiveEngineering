/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.data.recipebuilder;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;

public class HammerCrushingRecipeBuilder extends IEFinishedRecipe<HammerCrushingRecipeBuilder>
{
	private HammerCrushingRecipeBuilder()
	{
		super(RecipeSerializers.HAMMER_CRUSHING_SERIALIZER.get());
		this.maxResultCount = 1;
		this.maxInputCount = 1;
	}

	public static HammerCrushingRecipeBuilder builder(IItemProvider result)
	{
		return new HammerCrushingRecipeBuilder().addResult(result);
	}

	public static HammerCrushingRecipeBuilder builder(ItemStack result)
	{
		return new HammerCrushingRecipeBuilder().addResult(result);
	}

	public static HammerCrushingRecipeBuilder builder(ITag<Item> result)
	{
		return new HammerCrushingRecipeBuilder().addResult(new IngredientWithSize(result));
	}
}
