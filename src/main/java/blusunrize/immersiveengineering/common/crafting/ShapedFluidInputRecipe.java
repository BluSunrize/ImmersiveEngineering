/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class ShapedFluidInputRecipe extends ShapedRecipe
{
	public ShapedFluidInputRecipe(
			ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn,
			NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn
	)
	{
		super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInventory inv)
	{
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		final MatchLocation offset = getMatchLocation(inv);

		for(int x = 0; x < inv.getWidth(); ++x)
			for(int y = 0; y < inv.getHeight(); ++y)
			{
				final int invIndex = getInventoryIndex(inv, x, y);
				final int ingrIndex = offset.getListIndex(x, y);
				Ingredient ingr = getIngredients().get(ingrIndex);
				final ItemStack item = inv.getStackInSlot(invIndex);
				ItemStack result = ItemStack.EMPTY;
				if(ingr instanceof IngredientFluidStack)
					result = ((IngredientFluidStack)ingr).getExtractedStack(item);
				else if(item.hasContainerItem())
					result = item.getContainerItem();
				remaining.set(invIndex, result);
			}

		return remaining;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return super.getSerializer();
	}

	private int getInventoryIndex(CraftingInventory inv, int x, int y)
	{
		return x+y*inv.getWidth();
	}

	private MatchLocation getMatchLocation(CraftingInventory inv)
	{
		for(int x = 0; x <= inv.getWidth()-this.getRecipeWidth(); ++x)
			for(int y = 0; y <= inv.getHeight()-this.getRecipeHeight(); ++y)
			{
				if(this.checkMatch(inv, x, y, true))
					return new MatchLocation(x, y, true);

				if(this.checkMatch(inv, x, y, false))
					return new MatchLocation(x, y, false);
			}
		throw new IllegalArgumentException("No match found!");
	}

	private class MatchLocation
	{
		private final int xOffset;
		private final int yOffset;
		private final boolean mirrored;

		private MatchLocation(int x, int y, boolean mirrored)
		{
			this.xOffset = x;
			this.yOffset = y;
			this.mirrored = mirrored;
		}

		public int getListIndex(int globalX, int globalY)
		{
			int localX = globalX-xOffset;
			int localY = globalY-yOffset;
			if(mirrored)
				return getRecipeWidth()-localX-1+localY*getRecipeWidth();
			else
				return localX+localY*getRecipeWidth();
		}
	}
}
