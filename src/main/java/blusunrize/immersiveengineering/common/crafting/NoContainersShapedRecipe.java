/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class NoContainersShapedRecipe<T extends ShapedRecipe> extends ShapedRecipe implements INoContainersRecipe
{
	private final T baseRecipe;

	public NoContainersShapedRecipe(T baseRecipe)
	{
		super(baseRecipe.getGroup(), baseRecipe.category(), baseRecipe.pattern, baseRecipe.getResultItem(null));
		this.baseRecipe = baseRecipe;
	}

	@Override
	public int getWidth()
	{
		return baseRecipe.getWidth();
	}

	@Override
	public int getHeight()
	{
		return baseRecipe.getHeight();
	}

	@Override
	public T baseRecipe()
	{
		return baseRecipe;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return INoContainersRecipe.super.getSerializer();
	}
}
