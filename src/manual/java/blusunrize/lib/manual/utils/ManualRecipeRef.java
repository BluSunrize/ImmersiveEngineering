/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.utils;

import blusunrize.lib.manual.PositionedItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ManualRecipeRef
{
	private final ItemStack output;
	private final PositionedItemStack[] layout;
	private final ResourceLocation recipeName;

	public ManualRecipeRef(ItemStack output)
	{
		this.output = Objects.requireNonNull(output);
		this.layout = null;
		this.recipeName = null;
	}

	public ManualRecipeRef(PositionedItemStack[] layout)
	{
		this.output = null;
		this.layout = Objects.requireNonNull(layout);
		this.recipeName = null;
	}

	public ManualRecipeRef(ResourceLocation recipeName)
	{
		this.output = null;
		this.layout = null;
		this.recipeName = Objects.requireNonNull(recipeName);
	}

	public boolean isLayout()
	{
		return layout!=null;
	}

	public PositionedItemStack[] getLayout()
	{
		return Objects.requireNonNull(layout);
	}

	public boolean isResult()
	{
		return output!=null;
	}

	public ItemStack getResult()
	{
		return Objects.requireNonNull(output);
	}

	public boolean isRecipeName()
	{
		return recipeName!=null;
	}

	public ResourceLocation getRecipeName()
	{
		return Objects.requireNonNull(recipeName);
	}
}
