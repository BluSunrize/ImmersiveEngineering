/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

public class NoContainersShapedRecipe<T extends CraftingRecipe & IShapedRecipe<CraftingInput>>
		extends NoContainersRecipe<T> implements IShapedRecipe<CraftingInput>
{
	public NoContainersShapedRecipe(T baseRecipe)
	{
		super(baseRecipe);
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
}
