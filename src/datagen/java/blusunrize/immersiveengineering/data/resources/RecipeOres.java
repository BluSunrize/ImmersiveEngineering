/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.resources;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag.INamedTag;

import static blusunrize.immersiveengineering.api.utils.TagUtils.createItemWrapper;


/**
 * An Enum of  non-metal ores from Vanilla, IE and other mods. Used for generating Crusher recipes
 */
public enum RecipeOres
{
	// Vanilla
	COAL("coal", true, Items.COAL, 4, new SecondaryOutput(IETags.sulfurDust, .15f)),
	DIAMOND("diamond", true, Items.DIAMOND, 2),
	EMERALD("emerald", true, Items.EMERALD, 2),
	LAPIS("lapis", true, Items.LAPIS_LAZULI, 9, new SecondaryOutput(IETags.sulfurDust, .20f)),
	QUARTZ("quartz", true, Items.QUARTZ, 3, new SecondaryOutput(IETags.sulfurDust, .25f)),
	REDSTONE("redstone", true, Items.REDSTONE, 6), // is cinnabar still a thing?

	FLUORITE("fluorite", false, new IngredientWithSize(createItemWrapper(IETags.getGem("fluorite")), 6));

	private final String name;
	private final boolean isNative;
	private final INamedTag<Item> ore;
	private final IngredientWithSize output;
	private final SecondaryOutput[] secondaryOutputs;

	RecipeOres(String name, boolean isNative, IngredientWithSize output, SecondaryOutput... secondaryOutputs)
	{
		this.name = name;
		this.ore = createItemWrapper(IETags.getOre(name));
		this.isNative = isNative;
		this.output = output;
		this.secondaryOutputs = secondaryOutputs;
	}

	RecipeOres(String name, boolean isNative, Item output, int outputSize, SecondaryOutput... secondaryOutputs)
	{
		this(name, isNative, new IngredientWithSize(Ingredient.fromItems(output), outputSize), secondaryOutputs);
	}


	public String getName()
	{
		return name;
	}

	public boolean isNative()
	{
		return isNative;
	}

	public INamedTag<Item> getOre()
	{
		return ore;
	}

	public IngredientWithSize getOutput()
	{
		return output;
	}

	public SecondaryOutput[] getSecondaryOutputs()
	{
		return secondaryOutputs;
	}

}
