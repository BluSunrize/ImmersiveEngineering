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
import blusunrize.immersiveengineering.mixin.accessors.ShapedPatternAccess;
import blusunrize.immersiveengineering.mixin.accessors.ShapedRecipeAccess;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;

public class BasicShapedRecipe extends AbstractShapedRecipe<MatchLocation>
{
	public BasicShapedRecipe(ShapedRecipe vanillaBase)
	{
		super(
				vanillaBase.getGroup(), vanillaBase.getWidth(), vanillaBase.getHeight(),
				vanillaBase.getIngredients(), vanillaBase.getResultItem(null), vanillaBase.category(),
				((ShapedPatternAccess)(Object)((ShapedRecipeAccess)vanillaBase).getPattern()).getData()
		);
	}

	protected boolean checkMatch(CraftingInput craftingInventory, MatchLocation loc)
	{
		for(int invX = 0; invX < craftingInventory.width(); ++invX)
			for(int invY = 0; invY < craftingInventory.height(); ++invY)
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

				if(!expectedContent.test(craftingInventory.getItem(invX+invY*craftingInventory.width())))
					return false;
			}

		return true;
	}

	@Nullable
	@Override
	protected MatchLocation findMatch(CraftingInput inv)
	{
		for(int xOffset = 0; xOffset <= inv.width()-this.getWidth(); ++xOffset)
			for(int yOffset = 0; yOffset <= inv.height()-this.getHeight(); ++yOffset)
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
