/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class IERecipeSerializer<R extends IRecipe<?>> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<R>
{
	public abstract ItemStack getIcon();
}
