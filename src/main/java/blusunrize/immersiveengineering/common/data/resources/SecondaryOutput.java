/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.data.resources;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class SecondaryOutput
{
	private final IngredientWithSize item;
	private final float chance;

	public SecondaryOutput(IngredientWithSize item, float chance)
	{
		this.item = item;
		this.chance = chance;
	}

	public SecondaryOutput(Tag<Item> tag, float chance)
	{
		this(new IngredientWithSize(tag), chance);
	}

	public SecondaryOutput(ResourceLocation tag, float chance)
	{
		this(new ItemTags.Wrapper(tag), chance);
	}

	public IngredientWithSize getItem()
	{
		return item;
	}

	public float getChance()
	{
		return chance;
	}
}
