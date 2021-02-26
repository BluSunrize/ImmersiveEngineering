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
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;

import javax.annotation.Nonnull;

public abstract class AbstractShapedRecipe<MatchLocation extends IMatchLocation>
		extends AbstractFluidAwareRecipe<MatchLocation> implements IShapedRecipe<CraftingInventory>
{
	private final int recipeWidth;
	private final int recipeHeight;

	public AbstractShapedRecipe(ResourceLocation idIn, String groupIn, int recipeWidth, int recipeHeight,
								NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn)
	{
		super(idIn, groupIn, recipeItemsIn, recipeOutputIn);
		this.recipeWidth = recipeWidth;
		this.recipeHeight = recipeHeight;
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
	public boolean canFit(int width, int height)
	{
		return width >= this.recipeWidth&&height >= this.recipeHeight;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.IE_SHAPED_SERIALIZER.get();
	}

	public ShapedRecipe toVanilla()
	{
		return new ShapedRecipe(getId(), getGroup(), getWidth(), getHeight(), getIngredients(), getRecipeOutput());
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
