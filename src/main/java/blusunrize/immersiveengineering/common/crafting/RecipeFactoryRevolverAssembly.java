/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;

public class RecipeFactoryRevolverAssembly extends RecipeFactoryShapedIngredient
{
	@Override
	protected RecipeShapedIngredient constructRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer)
	{
		return new RecipeRevolverAssembly(group, result, primer);
	}
}