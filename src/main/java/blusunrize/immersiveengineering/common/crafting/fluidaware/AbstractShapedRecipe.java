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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import javax.annotation.Nonnull;

// TODO to follow Neo convention this needs to have ShapedRecipe as the superclass instead AFAR
public abstract class AbstractShapedRecipe<MatchLocation extends IMatchLocation>
		extends ShapedRecipe implements IFluidAwareRecipe<MatchLocation>
{
	private final int recipeWidth;
	private final int recipeHeight;
	private final CraftingBookCategory category;

	public AbstractShapedRecipe(ShapedRecipe vanilla)
	{
		this(
				vanilla.getGroup(),
				vanilla.getWidth(), vanilla.getHeight(),
				vanilla.getResultItem(null), vanilla.category(),
				vanilla.pattern
		);
	}

	public AbstractShapedRecipe(
			String groupIn, int recipeWidth, int recipeHeight, ItemStack recipeOutput,
			CraftingBookCategory category, ShapedRecipePattern pattern
	)
	{
		super(groupIn, category, pattern, recipeOutput);
		this.recipeWidth = recipeWidth;
		this.recipeHeight = recipeHeight;
		this.category = category;
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
		return new ShapedRecipe(getGroup(), category, pattern, getResultItem(null));
	}

	@Override
	public CraftingBookCategory category()
	{
		return category;
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
					.anyMatch(Ingredient::hasNoItems);
	}
}
