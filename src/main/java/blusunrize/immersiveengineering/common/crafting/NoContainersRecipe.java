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
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class NoContainersRecipe<T extends CraftingRecipe> implements CraftingRecipe
{
	public final T baseRecipe;

	public NoContainersRecipe(T baseRecipe)
	{
		this.baseRecipe = baseRecipe;
	}

	@Override
	public boolean matches(@Nonnull CraftingContainer pContainer, @Nonnull Level pLevel)
	{
		return baseRecipe.matches(pContainer, pLevel);
	}

	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull CraftingContainer pContainer, RegistryAccess access)
	{
		return baseRecipe.assemble(pContainer, access);
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight)
	{
		return baseRecipe.canCraftInDimensions(pWidth, pHeight);
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(RegistryAccess access)
	{
		return baseRecipe.getResultItem(access);
	}

	@Nonnull
	@Override
	public ResourceLocation getId()
	{
		return baseRecipe.getId();
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.NO_CONTAINER_SERIALIZER.get();
	}

	@Nonnull
	@Override
	public RecipeType<?> getType()
	{
		return baseRecipe.getType();
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingContainer pContainer)
	{
		return NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);
	}

	@Nonnull
	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return baseRecipe.getIngredients();
	}

	@Override
	public boolean isSpecial()
	{
		return baseRecipe.isSpecial();
	}

	@Nonnull
	@Override
	public String getGroup()
	{
		return baseRecipe.getGroup();
	}

	@Nonnull
	@Override
	public ItemStack getToastSymbol()
	{
		return baseRecipe.getToastSymbol();
	}

	@Override
	public boolean isIncomplete()
	{
		return baseRecipe.isIncomplete();
	}

	@Override
	public CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}
}
