/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;

public class ArcFurnaceRecipeBuilder extends IEFinishedRecipe<ArcFurnaceRecipeBuilder>
{
	private ArcFurnaceRecipeBuilder()
	{
		super(ArcFurnaceRecipe.SERIALIZER.get());
		setMultipleResults(6);
		setUseInputArray(4, "additives");
	}

	public static ArcFurnaceRecipeBuilder builder(Item result)
	{
		return new ArcFurnaceRecipeBuilder().addResult(result);
	}

	public static ArcFurnaceRecipeBuilder builder(ItemStack result)
	{
		return new ArcFurnaceRecipeBuilder().addResult(result);
	}

	public static ArcFurnaceRecipeBuilder builder(ITag<Item> result, int count)
	{
		return new ArcFurnaceRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}

	public ArcFurnaceRecipeBuilder addSlag(IItemProvider itemProvider)
	{
		return addItem("slag", new ItemStack(itemProvider));
	}

	public ArcFurnaceRecipeBuilder addSlag(ItemStack itemStack)
	{
		return addItem("slag", itemStack);
	}

	public ArcFurnaceRecipeBuilder addSlag(ITag<Item> tag, int count)
	{
		return addIngredient("slag", new IngredientWithSize(tag, count));
	}
}
