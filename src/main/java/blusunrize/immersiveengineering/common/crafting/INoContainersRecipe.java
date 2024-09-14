/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public interface INoContainersRecipe extends CraftingRecipe
{
	CraftingRecipe baseRecipe();

	@Override
	default boolean matches(@Nonnull CraftingInput pContainer, @Nonnull Level pLevel)
	{
		return baseRecipe().matches(pContainer, pLevel);
	}

	@Nonnull
	@Override
	default ItemStack assemble(@Nonnull CraftingInput pContainer, Provider access)
	{
		return baseRecipe().assemble(pContainer, access);
	}

	@Override
	default boolean canCraftInDimensions(int pWidth, int pHeight)
	{
		return baseRecipe().canCraftInDimensions(pWidth, pHeight);
	}

	@Nonnull
	@Override
	default ItemStack getResultItem(Provider access)
	{
		return baseRecipe().getResultItem(access);
	}

	@Nonnull
	@Override
	default RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.NO_CONTAINER_SERIALIZER.get();
	}

	@Nonnull
	@Override
	default RecipeType<?> getType()
	{
		return baseRecipe().getType();
	}

	@Nonnull
	@Override
	default NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInput pContainer)
	{
		return NonNullList.withSize(pContainer.size(), ItemStack.EMPTY);
	}

	@Nonnull
	@Override
	default NonNullList<Ingredient> getIngredients()
	{
		return baseRecipe().getIngredients();
	}

	@Override
	default boolean isSpecial()
	{
		return baseRecipe().isSpecial();
	}

	@Nonnull
	@Override
	default String getGroup()
	{
		return baseRecipe().getGroup();
	}

	@Nonnull
	@Override
	default ItemStack getToastSymbol()
	{
		return baseRecipe().getToastSymbol();
	}

	@Override
	default boolean isIncomplete()
	{
		return baseRecipe().isIncomplete();
	}

	@Override
	default CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}
}
