/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CokeOvenRecipeBuilder extends IEFinishedRecipe<CokeOvenRecipeBuilder>
{
	private CokeOvenRecipeBuilder()
	{
		super(CokeOvenRecipe.SERIALIZER.value());
	}

	public static CokeOvenRecipeBuilder builder(Item result)
	{
		return new CokeOvenRecipeBuilder().addResult(result);
	}

	public static CokeOvenRecipeBuilder builder(ItemStack result)
	{
		return new CokeOvenRecipeBuilder().addResult(result);
	}

	public static CokeOvenRecipeBuilder builder(TagKey<Item> result, int count)
	{
		return new CokeOvenRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}

	public CokeOvenRecipeBuilder setOil(int amount)
	{
		return addWriter(jsonObject -> jsonObject.addProperty("creosote", amount));
	}
}
