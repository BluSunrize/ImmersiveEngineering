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
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractShapedRecipe<MatchLocation extends IMatchLocation>
		extends AbstractFluidAwareRecipe<MatchLocation> implements IShapedRecipe<CraftingContainer>
{
	private final int recipeWidth;
	private final int recipeHeight;
	private final CraftingBookCategory category;
	private final Optional<ShapedRecipePattern.Data> data;

	public AbstractShapedRecipe(
			String groupIn, int recipeWidth, int recipeHeight, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn,
			CraftingBookCategory category
	)
	{
		this(groupIn, recipeWidth, recipeHeight, recipeItemsIn, recipeOutputIn, category, Optional.empty());
	}

	public AbstractShapedRecipe(
			String groupIn, int recipeWidth, int recipeHeight, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn,
			CraftingBookCategory category, Optional<ShapedRecipePattern.Data> data
	)
	{
		super(groupIn, recipeItemsIn, recipeOutputIn);
		this.recipeWidth = recipeWidth;
		this.recipeHeight = recipeHeight;
		this.category = category;
		this.data = data;
	}

	public int getWidth()
	{
		return this.recipeWidth;
	}

	public int getHeight()
	{
		return this.recipeHeight;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width >= this.recipeWidth&&height >= this.recipeHeight;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.IE_SHAPED_SERIALIZER.get();
	}

	public ShapedRecipe toVanilla()
	{
		return new ShapedRecipe(
				getGroup(), category,
				new ShapedRecipePattern(getWidth(), getHeight(), getIngredients(), data),
				getResultItem(null)
		);
	}

	@Override
	public CraftingBookCategory category()
	{
		return category;
	}

	@Override
	public int getRecipeWidth()
	{
		return recipeWidth;
	}

	@Override
	public int getRecipeHeight()
	{
		return recipeHeight;
	}

	@Override
	public boolean isIncomplete()
	{
		// Copied from Forge patch to ShapedRecipe
		NonNullList<Ingredient> nonnulllist = getIngredients();
		if(nonnulllist.isEmpty())
			return true;
		else
			return nonnulllist.stream()
					.filter(ingredient -> !ingredient.isEmpty())
					.anyMatch(CommonHooks::hasNoElements);
	}
}
