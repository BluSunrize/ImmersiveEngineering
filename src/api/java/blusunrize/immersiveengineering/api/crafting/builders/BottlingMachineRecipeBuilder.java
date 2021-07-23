/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BottlingMachineRecipeBuilder extends IEFinishedRecipe<BottlingMachineRecipeBuilder>
{
	private BottlingMachineRecipeBuilder()
	{
		super(BottlingMachineRecipe.SERIALIZER.get());
	}

	public static BottlingMachineRecipeBuilder builder(Item result)
	{
		return new BottlingMachineRecipeBuilder().addResult(result);
	}

	public static BottlingMachineRecipeBuilder builder(ItemStack result)
	{
		return new BottlingMachineRecipeBuilder().addResult(result);
	}

	public static BottlingMachineRecipeBuilder builder(Tag<Item> result, int count)
	{
		return new BottlingMachineRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}
}
