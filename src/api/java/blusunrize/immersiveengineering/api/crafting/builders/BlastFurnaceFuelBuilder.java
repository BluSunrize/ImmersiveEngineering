/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class BlastFurnaceFuelBuilder extends IEFinishedRecipe<BlastFurnaceFuelBuilder>
{
	private BlastFurnaceFuelBuilder()
	{
		super(BlastFurnaceFuel.SERIALIZER.get());
		this.maxResultCount = 0;
	}

	public static BlastFurnaceFuelBuilder builder(ItemLike input)
	{
		return new BlastFurnaceFuelBuilder().addInput(input);
	}

	public static BlastFurnaceFuelBuilder builder(ItemStack input)
	{
		return new BlastFurnaceFuelBuilder().addInput(input);
	}

	public static BlastFurnaceFuelBuilder builder(Tag<Item> input)
	{
		return new BlastFurnaceFuelBuilder().addInput(Ingredient.of(input));
	}
}
