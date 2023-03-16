/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.common.crafting.fluidaware.BasicShapedRecipe.MatchLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;

public class BasicShapedRecipe extends AbstractShapedRecipe<MatchLocation>
{
	public BasicShapedRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn, CraftingBookCategory category)
	{
		super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn, category);
	}

	public BasicShapedRecipe(ShapedRecipe vanillaBase)
	{
		this(vanillaBase.getId(), vanillaBase.getGroup(), vanillaBase.getWidth(), vanillaBase.getHeight(),
				vanillaBase.getIngredients(), vanillaBase.getResultItem(null), vanillaBase.category());
	}

	protected boolean checkMatch(CraftingContainer craftingInventory, MatchLocation loc)
	{
		for(int invX = 0; invX < craftingInventory.getWidth(); ++invX)
			for(int invY = 0; invY < craftingInventory.getHeight(); ++invY)
			{
				int recX = invX-loc.xOffset;
				int recY = invY-loc.yOffset;
				Ingredient expectedContent = Ingredient.EMPTY;
				if(recX >= 0&&recY >= 0&&recX < this.getWidth()&&recY < this.getHeight())
				{
					int recipeSlot;
					if(loc.mirrored)
						recipeSlot = this.getWidth()-recX-1+recY*this.getWidth();
					else
						recipeSlot = recX+recY*this.getWidth();
					expectedContent = getIngredients().get(recipeSlot);
				}

				if(!expectedContent.test(craftingInventory.getItem(invX+invY*craftingInventory.getWidth())))
					return false;
			}

		return true;
	}

	@Nullable
	@Override
	protected MatchLocation findMatch(CraftingContainer inv)
	{
		for(int xOffset = 0; xOffset <= inv.getWidth()-this.getWidth(); ++xOffset)
			for(int yOffset = 0; yOffset <= inv.getHeight()-this.getHeight(); ++yOffset)
				for(boolean mirror : BOOLEANS)
				{
					MatchLocation loc = new MatchLocation(xOffset, yOffset, mirror, getWidth());
					if(this.checkMatch(inv, loc))
						return loc;
				}

		return null;
	}

	public static class MatchLocation implements AbstractFluidAwareRecipe.IMatchLocation
	{
		private final int xOffset;
		private final int yOffset;
		private final boolean mirrored;
		private final int recipeWidth;

		private MatchLocation(int x, int y, boolean mirrored, int recipeWidth)
		{
			this.xOffset = x;
			this.yOffset = y;
			this.mirrored = mirrored;
			this.recipeWidth = recipeWidth;
		}

		@Override
		public int getListIndex(int x, int y)
		{
			int localX = x-xOffset;
			int localY = y-yOffset;
			if(mirrored)
				return recipeWidth-localX-1+localY*recipeWidth;
			else
				return localX+localY*recipeWidth;
		}
	}
}
