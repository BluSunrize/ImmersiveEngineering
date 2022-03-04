/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.recipebuilder;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class HammerCrushingRecipeBuilder extends IEFinishedRecipe<HammerCrushingRecipeBuilder>
{
	private HammerCrushingRecipeBuilder()
	{
		super(RecipeSerializers.HAMMER_CRUSHING_SERIALIZER.get());
		this.maxResultCount = 1;
		this.maxInputCount = 1;
	}

	public static HammerCrushingRecipeBuilder builder(ItemLike result)
	{
		return new HammerCrushingRecipeBuilder().addResult(result);
	}

	public static HammerCrushingRecipeBuilder builder(ItemStack result)
	{
		return new HammerCrushingRecipeBuilder().addResult(result);
	}

	public static HammerCrushingRecipeBuilder builder(TagKey<Item> result)
	{
		return new HammerCrushingRecipeBuilder().addResult(new IngredientWithSize(result));
	}
}
