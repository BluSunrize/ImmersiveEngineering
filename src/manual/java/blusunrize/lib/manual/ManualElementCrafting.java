/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.ManualScreen;
import blusunrize.lib.manual.utils.ManualRecipeRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.IShapedRecipe;

import java.util.ArrayList;
import java.util.List;

public class ManualElementCrafting extends SpecialManualElements
{
	private final ManualRecipeRef[][] recipeRows;
	private final List<PositionedItemStack[]>[] recipeLayout;
	private final int[] recipePage;
	private final int[] heightPixels;

	public ManualElementCrafting(ManualInstance manual, ManualRecipeRef[][] recipeRows)
	{
		super(manual);
		this.recipeRows = recipeRows;
		this.recipePage = new int[recipeRows.length];
		this.heightPixels = new int[recipeRows.length];
		this.recipeLayout = (List<PositionedItemStack[]>[])new List[recipeRows.length];
		for(int i = 0; i < recipeRows.length; ++i)
			recipeLayout[i] = new ArrayList<>();
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		if(Minecraft.getInstance().level==null)
			return;
		this.providedItems.clear();
		for(int iStack = 0; iStack < recipeRows.length; iStack++)
		{
			this.recipeLayout[iStack].clear();
			ManualRecipeRef[] row = recipeRows[iStack];
			for(ManualRecipeRef recipe : row)
			{
				if(recipe.isLayout())
					addFixedRecipe(iStack, recipe.getLayout());
				else
				{
					int finalIStack = iStack;
					recipe.forEachMatchingRecipe(RecipeType.CRAFTING, r -> addRecipe(r, finalIStack));
				}
			}
		}
	}

	private void addRecipe(Recipe<CraftingContainer> rec, int recipeIndex)
	{
		NonNullList<Ingredient> ingredientsPre = rec.getIngredients();
		int recipeWidth;
		int recipeHeight;
		if(rec instanceof IShapedRecipe<?> shaped)
		{
			recipeWidth = shaped.getRecipeWidth();
			recipeHeight = shaped.getRecipeHeight();
		}
		else
		{
			recipeWidth = Mth.clamp(ingredientsPre.size(), 1, 3);
			recipeHeight = (ingredientsPre.size()-1)/3+1;
		}

		int yOffset = (this.heightPixels[recipeIndex]-18*recipeHeight)/2;
		if(yOffset < 0)
			yOffset = 0;
		PositionedItemStack[] pIngredients = new PositionedItemStack[ingredientsPre.size()+1];
		int xBase = (120-(recipeWidth+2)*18)/2;
		for(int heightPos = 0; heightPos < recipeHeight; heightPos++)
			for(int widthPos = 0; widthPos < recipeWidth; widthPos++)
			{
				int index = heightPos*recipeWidth+widthPos;
				if(index < ingredientsPre.size())
					pIngredients[index] = new PositionedItemStack(ingredientsPre.get(index),
							xBase+widthPos*18, heightPos*18+yOffset);
			}
		final RegistryAccess regAccess = Minecraft.getInstance().level.registryAccess();
		pIngredients[pIngredients.length-1] = new PositionedItemStack(
				rec.getResultItem(regAccess), xBase+recipeWidth*18+18, recipeHeight*9-8+yOffset
		);
		if(this.heightPixels[recipeIndex] < recipeHeight*18)
		{
			this.heightPixels[recipeIndex] = recipeHeight*18;
			for(int prevId = 0; prevId <= recipeIndex; ++prevId)
				for(PositionedItemStack[] oldStacks : recipeLayout[prevId])
					moveBy(oldStacks, yOffset);
		}
		this.recipeLayout[recipeIndex].add(pIngredients);
		addProvidedItem(rec.getResultItem(regAccess));
	}

	private void addFixedRecipe(int index, PositionedItemStack[] recipe)
	{
		int height = 0;
		for(PositionedItemStack stack : recipe)
			if(stack.y() > height)
				height = stack.y();
		height += 18;
		if(this.heightPixels[index] < height)
		{
			int offset = (height-heightPixels[index])/2;
			this.heightPixels[index] = height;
			for(int prevId = 0; prevId <= index; ++prevId)
				for(PositionedItemStack[] oldStacks : recipeLayout[prevId])
					moveBy(oldStacks, offset);
		}
		else
		{
			int offset = (heightPixels[index]-height)/2;
			moveBy(recipe, offset);
		}
		recipeLayout[index].add(recipe);
	}

	private static void moveBy(PositionedItemStack[] in, int offY)
	{
		for(int i = 0; i < in.length; ++i)
			in[i] = new PositionedItemStack(in[i].displayList(), in[i].x(), in[i].y()+offY);
	}

	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons)
	{
		int recipeYOffset = 0;
		for(int i = 0; i < this.recipeRows.length; i++)
		{
			if(this.recipeLayout[i].size() > 1)
			{
				final int iFinal = i;
				pageButtons.add(new GuiButtonManualNavigation(gui, x-2, y+recipeYOffset+heightPixels[i]/2-5, 8, 10, 0, btn -> {
					recipePage[iFinal]--;
					if(recipePage[iFinal] < 0)
						recipePage[iFinal] = recipeLayout[iFinal].size()-1;
				}));
				pageButtons.add(new GuiButtonManualNavigation(gui, x+122-16, y+recipeYOffset+heightPixels[i]/2-5, 8, 10, 1, btn -> {
					recipePage[iFinal]++;
					if(recipePage[iFinal] >= recipeLayout[iFinal].size())
						recipePage[iFinal] = 0;
				}));
			}
			if(this.recipeLayout[i].size() > 0)
				recipeYOffset += heightPixels[i]+8;
		}
		super.onOpened(gui, x, y+recipeYOffset-2, pageButtons);
	}

	@Override
	public void render(GuiGraphics graphics, ManualScreen gui, int x, int y, int mx, int my)
	{
		int totalYOff = 0;
		highlighted = ItemStack.EMPTY;
		for(int i = 0; i < recipeRows.length; i++)
		{
			List<PositionedItemStack[]> rList = this.recipeLayout[i];
			if(!rList.isEmpty()&&recipePage[i] >= 0&&recipePage[i] < rList.size())
			{
				int maxX = 0;
				for(PositionedItemStack pstack : rList.get(recipePage[i]))
					if(pstack!=null)
					{
						if(pstack.x() > maxX)
							maxX = pstack.x();
						graphics.fill(x+pstack.x(), y+totalYOff+pstack.y(), x+pstack.x()+16, y+totalYOff+pstack.y()+16, 0x33666666);
					}

				ManualUtils.drawTexturedRect(graphics, manual.texture, x+maxX-17,
						y+totalYOff+heightPixels[i]/2-5, 16, 10, 0/256f,
						16/256f, 226/256f, 236/256f);

				totalYOff += heightPixels[i]+8;
			}
		}

		totalYOff = 0;
		for(int i = 0; i < recipeLayout.length; i++)
		{
			List<PositionedItemStack[]> rList = this.recipeLayout[i];
			if(!rList.isEmpty()&&recipePage[i] >= 0&&recipePage[i] < rList.size())
			{
				for(PositionedItemStack pstack : rList.get(recipePage[i]))
					if(pstack!=null)
						if(!pstack.getStackAtCurrentTime().isEmpty())
						{
							ManualUtils.renderItemStack(graphics, pstack.getStackAtCurrentTime(), x+pstack.x(), y+totalYOff+pstack.y(), true);
							if(mx >= x+pstack.x()&&mx < x+pstack.x()+16&&my >= y+totalYOff+pstack.y()&&my < y+totalYOff+pstack.y()+16)
								highlighted = pstack.getStackAtCurrentTime();
						}
				totalYOff += heightPixels[i]+8;
			}
		}

		this.renderHighlightedTooltip(graphics, mx, my);
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		for(ManualRecipeRef[] row : recipeRows)
			for(ManualRecipeRef recipe : row)
				if(recipe.isResult()&&ManualUtils.listStack(searchTag, recipe.getResult()))
					return true;
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		int yOff = 0;
		for(int heightPixel : this.heightPixels)
			yOff += heightPixel+8;
		return yOff;
	}
}
