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

import java.util.*;

public class ManualElementCraftingMulti extends SpecialManualElements
{
	Object[] stacks;
	ArrayList<PositionedItemStack[]> recipes = new ArrayList<>();
	int recipePage;
	int yOff;

	// Passing an Object[] for Object... is hard, and Object... and Object[] collide
	public static ManualElementCraftingMulti create(ManualInstance manual, Object... stacks)
	{
		return new ManualElementCraftingMulti(manual, stacks);
	}

	public ManualElementCraftingMulti(ManualInstance manual, Object[] stacks)
	{
		super(manual);
		this.stacks = stacks;
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		this.recipes.clear();
		Set<Integer> searchCrafting = new HashSet<>();

		if(providedItems!=null)
			this.providedItems.clear();
		for(int iStack = 0; iStack < stacks.length; iStack++)
			if(stacks[iStack] instanceof PositionedItemStack[])
			{
				for(PositionedItemStack[] pisA : (PositionedItemStack[][])stacks)
				{
					for(PositionedItemStack pis : pisA)
						if(pis!=null&&pis.y+18 > yOff)
							yOff = pis.y+18;
					this.recipes.add(pisA);
				}
			}
			else if(stacks[iStack] instanceof ResourceLocation)
			{
				IRecipe recipe = CraftingManager.getRecipe((ResourceLocation)stacks[iStack]);
				if(recipe!=null)
					handleRecipe(recipe, iStack);
			}
			else
			{
				searchCrafting.add(iStack);
				if(stacks[iStack] instanceof ItemStack)
					this.addProvidedItem((ItemStack)stacks[iStack]);
			}
		if(!searchCrafting.isEmpty())
		{
			Iterator<IRecipe> itRecipes = CraftingManager.REGISTRY.iterator();
			while(itRecipes.hasNext())
			{
				IRecipe recipe = itRecipes.next();
				for(int iStack : searchCrafting)
					if(!recipe.getRecipeOutput().isEmpty()&&ManualUtils.stackMatchesObject(recipe.getRecipeOutput(), stacks[iStack]))
						handleRecipe(recipe, iStack);
			}
		}
	}

	private void handleRecipe(IRecipe recipe, int iStack)
	{
		NonNullList<Ingredient> ingredientsPre = recipe.getIngredients();
		int w;
		int h;
		if(recipe instanceof ShapelessRecipes||recipe instanceof ShapelessOreRecipe)
		{
			w = ingredientsPre.size() > 6?3: ingredientsPre.size() > 1?2: 1;
			h = ingredientsPre.size() > 4?3: ingredientsPre.size() > 2?2: 1;
		}
		else if(recipe instanceof ShapedOreRecipe)
		{
			w = ((ShapedOreRecipe)recipe).getWidth();
			h = ((ShapedOreRecipe)recipe).getHeight();
		}
		else if(recipe instanceof ShapedRecipes)
		{
			w = ((ShapedRecipes)recipe).getWidth();
			h = ((ShapedRecipes)recipe).getHeight();
		}
		else
			return;

		PositionedItemStack[] pIngredients = new PositionedItemStack[ingredientsPre.size()+1];
		int xBase = (120-(w+2)*18)/2;
		for(int hh = 0; hh < h; hh++)
			for(int ww = 0; ww < w; ww++)
				if(hh*w+ww < ingredientsPre.size())
					pIngredients[hh*w+ww] = new PositionedItemStack(ingredientsPre.get(hh*w+ww), xBase+ww*18, hh*18);
		pIngredients[pIngredients.length-1] = new PositionedItemStack(recipe.getRecipeOutput(), xBase+w*18+18, (int)(h/2f*18)-8);
		if(iStack < this.recipes.size())
			this.recipes.add(iStack, pIngredients);
		else
			this.recipes.add(pIngredients);
		if(h*18 > yOff)
			yOff = h*18;
	}

	@Override
	public void onOpened(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		if(this.recipes.size() > 1)
		{
			pageButtons.add(new GuiButtonManualNavigation(gui, 100+0, x-2, y+yOff/2-3, 8, 10, 0));
			pageButtons.add(new GuiButtonManualNavigation(gui, 100+1, x+122-16, y+yOff/2-3, 8, 10, 1));
		}
		super.onOpened(gui, x, y+yOff+2, pageButtons);
	}

	@Override
	public void render(GuiManual gui, int x, int y, int mx, int my)
	{
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();

		highlighted = ItemStack.EMPTY;

		if(!recipes.isEmpty()&&recipePage >= 0&&recipePage < this.recipes.size())
		{
			int maxX = 0;
			for(PositionedItemStack pstack : recipes.get(recipePage))
				if(pstack!=null)
				{
					if(pstack.x > maxX)
						maxX = pstack.x;
					gui.drawGradientRect(x+pstack.x, y+pstack.y, x+pstack.x+16, y+pstack.y+16, 0x33666666, 0x33666666);
				}
			ManualUtils.bindTexture(manual.texture);
			ManualUtils.drawTexturedRect(x+maxX-17, y+yOff/2-5, 16, 10, 0/256f, 16/256f, 226/256f, 236/256f);

		}

		GlStateManager.translate(0, 0, 300);
		boolean uni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(false);
		/**RenderItem.getInstance().renderWithColor=true;*/
		if(!recipes.isEmpty()&&recipePage >= 0&&recipePage < this.recipes.size())
		{
			for(PositionedItemStack pstack : recipes.get(recipePage))
				if(pstack!=null)
					if(!pstack.getStack().isEmpty())
					{
						ManualUtils.renderItem().renderItemAndEffectIntoGUI(pstack.getStack(), x+pstack.x, y+pstack.y);
						ManualUtils.renderItem().renderItemOverlayIntoGUI(manual.fontRenderer, pstack.getStack(), x+pstack.x, y+pstack.y, null);

						if(mx >= x+pstack.x&&mx < x+pstack.x+16&&my >= y+pstack.y&&my < y+pstack.y+16)
							highlighted = pstack.getStack();
					}
		}

		GlStateManager.translate(0, 0, -300);
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableBlend();
		RenderHelper.disableStandardItemLighting();

		if(!highlighted.isEmpty())
			gui.renderToolTip(highlighted, mx, my);
		GlStateManager.enableBlend();
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		super.buttonPressed(gui, button);
		if(button.id%100==0)
			recipePage--;
		else
			recipePage++;

		if(recipePage >= this.recipes.size())
			recipePage = 0;
		if(recipePage < 0)
			recipePage = this.recipes.size()-1;
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		for(PositionedItemStack[] recipe : this.recipes)
			for(PositionedItemStack stack : recipe)
			{
				if(stack.stack instanceof ItemStack[])
				{
					for(ItemStack subStack : (ItemStack[])stack.stack)
						if(subStack.getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
							return true;
				}
				else if(stack.stack instanceof List)
					for(ItemStack subStack : (List<ItemStack>)stack.stack)
					{
						if(subStack.getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
							return true;
					}
				else if(stack.stack instanceof ItemStack)
				{
					if(((ItemStack)stack.stack).getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
						return true;
				}
				else if(stack.stack instanceof String)
				{
					if(ManualUtils.isExistingOreName((String)stack.stack))
						for(ItemStack subStack : OreDictionary.getOres((String)stack.stack))
							if(subStack.getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
								return true;
				}
			}
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		return yOff;
	}
}
