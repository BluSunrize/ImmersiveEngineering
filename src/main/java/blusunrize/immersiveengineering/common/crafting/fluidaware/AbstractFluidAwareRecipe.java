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
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractFluidAwareRecipe<MatchLocation extends IMatchLocation> implements CraftingRecipe
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
	public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level worldIn)
	{
		return findMatch(inv)!=null;
	}

	@Nullable
	protected abstract MatchLocation findMatch(CraftingInput inv);

	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull CraftingInput inv, Provider access)
	{
		return this.getResultItem(access).copy();
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInput inv)
	{
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
		final MatchLocation offset = findMatch(inv);
		if(offset==null)
		{
			IELogger.logger.error("IRecipe#getRemainingItems was called with an inventory that does not match the recipe");
			IELogger.logger.error("according to IRecipe#matches. This is probably a bug in some mod in the following stacktrace,");
			IELogger.logger.error("if in doubt report it to Immersive Engineering", new IllegalArgumentException());
			return CraftingRecipe.super.getRemainingItems(inv);
		}

		for(int x = 0; x < inv.width(); ++x)
			for(int y = 0; y < inv.height(); ++y)
			{
				final int invIndex = getInventoryIndex(inv, x, y);
				final int ingrIndex = offset.getListIndex(x, y);
				if(ingrIndex >= 0&&ingrIndex < getIngredients().size())
				{
					Ingredient ingr = getIngredients().get(ingrIndex);
					final ItemStack item = inv.getItem(invIndex);
					ItemStack result = ItemStack.EMPTY;
					if(ingr.getCustomIngredient() instanceof IngredientFluidStack fluidIngred)
						result = fluidIngred.getExtractedStack(item.copy());
					else if(item.hasCraftingRemainingItem())
						result = item.getCraftingRemainingItem();
					if(result==item)
						result = result.copy();
					remaining.set(invIndex, result);
				}
			}

		return remaining;
	}

	private int getInventoryIndex(CraftingInput inv, int x, int y)
	{
		return x+y*inv.width();
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
