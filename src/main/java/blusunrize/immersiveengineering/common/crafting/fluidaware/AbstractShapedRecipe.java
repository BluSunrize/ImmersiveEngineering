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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.IShapedRecipe;

import javax.annotation.Nonnull;

public abstract class AbstractShapedRecipe<MatchLocation extends IMatchLocation>
		extends AbstractFluidAwareRecipe<MatchLocation> implements IShapedRecipe<CraftingContainer>
{
	private final int recipeWidth;
	private final int recipeHeight;
	private final CraftingBookCategory category;

	public AbstractShapedRecipe(
			ResourceLocation idIn, String groupIn, int recipeWidth, int recipeHeight, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn,
			CraftingBookCategory category
	)
	{
		super(idIn, groupIn, recipeItemsIn, recipeOutputIn);
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
		return new ShapedRecipe(getId(), getGroup(), category, getWidth(), getHeight(), getIngredients(), getResultItem(null));
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
}
