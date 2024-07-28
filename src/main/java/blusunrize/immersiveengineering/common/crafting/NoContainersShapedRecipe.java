/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.world.item.crafting.ShapedRecipe;

// TODO extend from ShapedRecipe(?)
public class NoContainersShapedRecipe<T extends ShapedRecipe> extends NoContainersRecipe<T>
{
	public NoContainersShapedRecipe(T baseRecipe)
	{
		super(baseRecipe);
	}

	public int getWidth()
	{
		return baseRecipe.getWidth();
	}

	public int getHeight()
	{
		return baseRecipe.getHeight();
	}
}
