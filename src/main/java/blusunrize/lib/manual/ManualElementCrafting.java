/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.GuiManual;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ManualElementCrafting extends SpecialManualElements
{
	Object[] stacks;
	ArrayListMultimap<Object, PositionedItemStack[]> recipes = ArrayListMultimap.create();
	int recipePage[];
	int yOff[];

	public ManualElementCrafting(ManualInstance manual, Object... stacks)
	{
		super(manual);
		this.stacks = stacks;
		this.recipePage = new int[stacks.length];
		this.yOff = new int[stacks.length];
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		this.recipes.clear();
		Iterator<IRecipe> itRecipes = CraftingManager.REGISTRY.iterator();
		while(itRecipes.hasNext())
		{
			IRecipe recipe = itRecipes.next();
			for(int iStack = 0; iStack < stacks.length; iStack++)
			{
				Object stack = stacks[iStack];
				if(stack instanceof ItemStack[])
					for(ItemStack subStack : (ItemStack[])stack)
						checkRecipe(recipe, stack, subStack, iStack);
				else
					checkRecipe(recipe, stack, stack, iStack);
			}
		}

		if(providedItems!=null)
			this.providedItems.clear();
		for(Object stack : stacks)
			if(stack instanceof ItemStack)
				this.addProvidedItem((ItemStack)stack);
			else if(stack instanceof ItemStack[])
				for(ItemStack subStack : (ItemStack[])stack)
					this.addProvidedItem(subStack);
	}

	void checkRecipe(IRecipe rec, Object key, Object stack, int iStack)
	{
		boolean matches = !rec.getRecipeOutput().isEmpty()&&ManualUtils.stackMatchesObject(rec.getRecipeOutput(), stack);
		if (!matches&&key instanceof ResourceLocation&&key.equals(rec.getRegistryName()))
			matches = true;
		if(matches)
		{
			NonNullList<Ingredient> ingredientsPre = rec.getIngredients();
			int w;
			int h;
			if(rec instanceof ShapelessRecipes||rec instanceof ShapelessOreRecipe)
			{
				w = ingredientsPre.size() > 6?3: ingredientsPre.size() > 1?2: 1;
				h = ingredientsPre.size() > 4?3: ingredientsPre.size() > 2?2: 1;
			}
			else if(rec instanceof ShapedOreRecipe)
			{
				w = ((ShapedOreRecipe)rec).getWidth();
				h = ((ShapedOreRecipe)rec).getHeight();
			}
			else if(rec instanceof ShapedRecipes)
			{
				w = ((ShapedRecipes)rec).recipeWidth;
				h = ((ShapedRecipes)rec).recipeHeight;
			}
			else
				return;

			PositionedItemStack[] pIngredients = new PositionedItemStack[ingredientsPre.size()+1];
			int xBase = (120-(w+2)*18)/2;
			for(int hh = 0; hh < h; hh++)
				for(int ww = 0; ww < w; ww++)
					if(hh*w+ww < ingredientsPre.size())
						pIngredients[hh*w+ww] = new PositionedItemStack(ingredientsPre.get(hh*w+ww), xBase+ww*18, hh*18);
			pIngredients[pIngredients.length-1] = new PositionedItemStack(rec.getRecipeOutput(), xBase+w*18+18, (int)(h/2f*18)-8);
			this.recipes.put(key, pIngredients);
			if(h*18 > yOff[iStack])
				yOff[iStack] = h*18;
		}
	}

	@Override
	public void onOpened(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		int i = 1;
		int yyOff = 0;
		for(Object stack : this.stacks)
		{
			if(this.recipes.get(stack).size() > 1)
			{
				pageButtons.add(new GuiButtonManualNavigation(gui, 100*i+0, x-2, y+yyOff+yOff[i-1]/2-3, 8, 10, 0));
				pageButtons.add(new GuiButtonManualNavigation(gui, 100*i+1, x+122-16, y+yyOff+yOff[i-1]/2-3, 8, 10, 1));
			}
			if(this.recipes.get(stack).size() > 0)
				yyOff += yOff[i-1]+8;
			i++;
		}
		super.onOpened(gui, x, y+yyOff-2, pageButtons);
	}

	@Override
	public void render(GuiManual gui, int x, int y, int mx, int my)
	{
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();

		int totalYOff = 0;
		highlighted = ItemStack.EMPTY;
		for(int i = 0; i < stacks.length; i++)
		{
			Object stack = stacks[i];
			List<PositionedItemStack[]> rList = this.recipes.get(stack);
			if(!rList.isEmpty()&&recipePage[i] >= 0&&recipePage[i] < this.recipes.size())
			{
				int maxX = 0;
				for(PositionedItemStack pstack : rList.get(recipePage[i]))
					if(pstack!=null)
					{
						if(pstack.x > maxX)
							maxX = pstack.x;
						gui.drawGradientRect(x+pstack.x, y+totalYOff+pstack.y, x+pstack.x+16, y+totalYOff+pstack.y+16, 0x33666666, 0x33666666);
					}
				ManualUtils.bindTexture(manual.texture);
				ManualUtils.drawTexturedRect(x+maxX-17, y+totalYOff+yOff[i]/2-5, 16, 10, 0/256f, 16/256f, 226/256f, 236/256f);

				totalYOff += yOff[i]+8;
			}
		}

		totalYOff = 0;
		GlStateManager.translate(0, 0, 300);
		boolean uni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(false);
		/*
		 RenderItem.getInstance().renderWithColor=true;*/
		for(int i = 0; i < stacks.length; i++)
		{
			Object stack = stacks[i];
			List<PositionedItemStack[]> rList = this.recipes.get(stack);
			if(!rList.isEmpty()&&recipePage[i] >= 0&&recipePage[i] < this.recipes.size())
			{
				for(PositionedItemStack pstack : rList.get(recipePage[i]))
					if(pstack!=null)
						if(!pstack.getStack().isEmpty())
						{
							ManualUtils.renderItem().renderItemAndEffectIntoGUI(pstack.getStack(), x+pstack.x, y+totalYOff+pstack.y);
							ManualUtils.renderItem().renderItemOverlayIntoGUI(manual.fontRenderer, pstack.getStack(), x+pstack.x, y+totalYOff+pstack.y, null);
							if(mx >= x+pstack.x&&mx < x+pstack.x+16&&my >= y+totalYOff+pstack.y&&my < y+totalYOff+pstack.y+16)
								highlighted = pstack.getStack();
						}
				totalYOff += yOff[i]+8;
			}
		}

		GlStateManager.translate(0, 0, -300);
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableBlend();
		RenderHelper.disableStandardItemLighting();

		manual.fontRenderer.setUnicodeFlag(uni);

		manual.fontRenderer.setUnicodeFlag(false);
		if(!highlighted.isEmpty())
			gui.renderToolTip(highlighted, mx, my);
		GlStateManager.enableBlend();
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		super.buttonPressed(gui, button);
		int r = button.id/100-1;
		if(r >= 0&&r < stacks.length)
		{
			if(button.id%100==0)
				recipePage[r]--;
			else
				recipePage[r]++;

			if(recipePage[r] >= this.recipes.get(stacks[r]).size())
				recipePage[r] = 0;
			if(recipePage[r] < 0)
				recipePage[r] = this.recipes.get(stacks[r]).size()-1;
		}
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		for(Object stack : stacks)
		{
			if(stack instanceof ItemStack[])
			{
				for(ItemStack subStack : (ItemStack[])stack)
					if(subStack.getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
						return true;
			}
			else if(stack instanceof ItemStack)
			{
				if(((ItemStack)stack).getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
					return true;
			}
			else if(stack instanceof String)
			{
				if(ManualUtils.isExistingOreName((String)stack))
					for(ItemStack subStack : OreDictionary.getOres((String)stack))
						if(subStack.getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
							return true;
			}
		}
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		int yOff = 0;
		for(int i = 0; i < this.yOff.length; i++)
		{
			yOff += this.yOff[i]+8;
		}
		return yOff;
	}
}
