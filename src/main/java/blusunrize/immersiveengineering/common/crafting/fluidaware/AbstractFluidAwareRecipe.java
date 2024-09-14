/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.common.crafting.fluidaware.AbstractFluidAwareRecipe.IMatchLocation;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public abstract class AbstractFluidAwareRecipe<MatchLocation extends IMatchLocation> implements IFluidAwareRecipe<MatchLocation>
{
	protected final static boolean[] BOOLEANS = {true, false};

	private final NonNullList<Ingredient> recipeItems;
	private final ItemStack recipeOutput;
	private final String group;

	public AbstractFluidAwareRecipe(
			String groupIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn
	)
	{
		this.group = groupIn;
		this.recipeItems = recipeItemsIn;
		this.recipeOutput = recipeOutputIn;
	}

	@Nonnull
	@Override
	public String getGroup()
	{
		return this.group;
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(Provider access)
	{
		return this.recipeOutput;
	}

	@Nonnull
	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return this.recipeItems;
	}

	@Override
	public CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}

	public interface IMatchLocation
	{
		int getListIndex(int x, int y);
	}
}
