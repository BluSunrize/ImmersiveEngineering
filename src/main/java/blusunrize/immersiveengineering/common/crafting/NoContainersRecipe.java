/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.world.item.crafting.CraftingRecipe;

public record NoContainersRecipe<T extends CraftingRecipe>(T baseRecipe) implements INoContainersRecipe
{
}
