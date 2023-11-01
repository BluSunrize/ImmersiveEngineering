/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

public class NoContainersShapedRecipe<T extends CraftingRecipe & IShapedRecipe<CraftingContainer>>
		extends NoContainersRecipe<T> implements IShapedRecipe<CraftingContainer>
{
	public NoContainersShapedRecipe(T baseRecipe)
	{
		super(baseRecipe);
	}

	@Override
	public int getRecipeWidth()
	{
		return baseRecipe.getRecipeWidth();
	}

	@Override
	public int getRecipeHeight()
	{
		return baseRecipe.getRecipeHeight();
	}
}
