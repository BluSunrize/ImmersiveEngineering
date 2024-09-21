/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting.cache;

import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public interface IListRecipe
{
	List<Recipe<?>> getSubRecipes();
}
