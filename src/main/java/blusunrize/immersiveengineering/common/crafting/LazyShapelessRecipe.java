/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;

public class LazyShapelessRecipe extends ShapelessRecipe
{
	private final Lazy<ItemStack> result;
	private final RecipeSerializer<LazyShapelessRecipe> serializer;

	public LazyShapelessRecipe(
			String groups, Lazy<ItemStack> result, NonNullList<Ingredient> ingredients, RecipeSerializer<LazyShapelessRecipe> serializer
	)
	{
		super(groups, CraftingBookCategory.MISC, ItemStack.EMPTY, ingredients);
		this.result = result;
		this.serializer = serializer;
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(RegistryAccess access)
	{
		return result.get();
	}

	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull CraftingContainer p_44260_, RegistryAccess access)
	{
		return result.get().copy();
	}

	@Nonnull
	@Override
	public RecipeSerializer<LazyShapelessRecipe> getSerializer()
	{
		return serializer;
	}

	public Lazy<ItemStack> getResult()
	{
		return result;
	}
}
