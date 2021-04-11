/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;

public class AlloyRecipeBuilder extends IEFinishedRecipe<AlloyRecipeBuilder>
{
	private AlloyRecipeBuilder()
	{
		super(AlloyRecipe.SERIALIZER.get());
		this.maxInputCount = 2;
		// default time for alloys is 200
		this.setTime(200);
	}

	public static AlloyRecipeBuilder builder(Item result)
	{
		return new AlloyRecipeBuilder().addResult(result);
	}

	public static AlloyRecipeBuilder builder(ItemStack result)
	{
		return new AlloyRecipeBuilder().addResult(result);
	}

	public static AlloyRecipeBuilder builder(ITag<Item> result, int count)
	{
		return new AlloyRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}

}
