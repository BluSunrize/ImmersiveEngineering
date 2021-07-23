/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class BlastFurnaceRecipeBuilder extends IEFinishedRecipe<BlastFurnaceRecipeBuilder>
{
	private BlastFurnaceRecipeBuilder()
	{
		super(BlastFurnaceRecipe.SERIALIZER.get());
	}

	public static BlastFurnaceRecipeBuilder builder(Item result)
	{
		return new BlastFurnaceRecipeBuilder().addResult(result);
	}

	public static BlastFurnaceRecipeBuilder builder(ItemStack result)
	{
		return new BlastFurnaceRecipeBuilder().addResult(result);
	}

	public static BlastFurnaceRecipeBuilder builder(Tag<Item> result, int count)
	{
		return new BlastFurnaceRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}


	public BlastFurnaceRecipeBuilder addSlag(ItemLike itemProvider)
	{
		return addItem("slag", new ItemStack(itemProvider));
	}

	public BlastFurnaceRecipeBuilder addSlag(ItemStack itemStack)
	{
		return addItem("slag", itemStack);
	}

	public BlastFurnaceRecipeBuilder addSlag(Tag<Item> tag, int count)
	{
		return addIngredient("slag", new IngredientWithSize(tag, count));
	}
}
