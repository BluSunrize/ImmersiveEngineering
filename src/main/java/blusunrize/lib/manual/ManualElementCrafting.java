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
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;
import java.util.Locale;

public class ManualElementCrafting extends SpecialManualElements
{
	private Object[] recipeRows;
	private ArrayListMultimap<Object, PositionedItemStack[]> recipeLayout = ArrayListMultimap.create();
	private int recipePage[];
	private int heightPixels[];

	public ManualElementCrafting(ManualInstance manual, Object... stacks)
	{
		super(manual);
		this.recipeRows = stacks;
		this.recipePage = new int[stacks.length];
		this.heightPixels = new int[stacks.length];
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		this.recipeLayout.clear();
		this.providedItems.clear();
		for(int iStack = 0; iStack < recipeRows.length; iStack++)
		{
			Object stack = recipeRows[iStack];
			if(stack instanceof Object[])
				for(Object subStack : (Object[])stack)
					for(IRecipe recipe : CraftingManager.REGISTRY)
						checkRecipe(recipe, stack, subStack, iStack);
			else
				for(IRecipe recipe : CraftingManager.REGISTRY)
					checkRecipe(recipe, stack, stack, iStack);
		}
	}

	private void checkRecipe(IRecipe rec, Object key, Object stack, int recipeIndex)
	{
		boolean matches = !rec.getRecipeOutput().isEmpty()&&ManualUtils.stackMatchesObject(rec.getRecipeOutput(), stack);
		if(!matches&&stack instanceof ResourceLocation&&stack.equals(rec.getRegistryName()))
			matches = true;
		if(matches)
		{
			NonNullList<Ingredient> ingredientsPre = rec.getIngredients();
			int recipeWidth;
			int recipeHeight;
			if(rec instanceof ShapelessRecipes||rec instanceof ShapelessOreRecipe)
			{
				recipeWidth = MathHelper.clamp(ingredientsPre.size(), 1, 3);
				recipeHeight = (ingredientsPre.size()-1)/3+1;
			}
			else if(rec instanceof IShapedRecipe)
			{
				recipeWidth = ((IShapedRecipe)rec).getRecipeWidth();
				recipeHeight = ((IShapedRecipe)rec).getRecipeHeight();
			}
			else
				return;

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
			pIngredients[pIngredients.length-1] = new PositionedItemStack(rec.getRecipeOutput(), xBase+recipeWidth*18+18,
					recipeHeight*9-8+yOffset);
			if(this.heightPixels[recipeIndex] < recipeHeight*18)
			{
				this.heightPixels[recipeIndex] = recipeHeight*18;
				for(int prevId = 0; prevId <= recipeIndex; ++prevId)
					for(PositionedItemStack[] oldStacks : recipeLayout.get(recipeRows[prevId]))
						for(PositionedItemStack oldStack : oldStacks)
							oldStack.y += yOffset;
			}
			this.recipeLayout.put(key, pIngredients);
			addProvidedItem(rec.getRecipeOutput());
		}
	}

	@Override
	public void onOpened(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		int i = 1;
		int recipeYOffset = 0;
		for(Object stack : this.recipeRows)
		{
			if(this.recipeLayout.get(stack).size() > 1)
			{
				pageButtons.add(new GuiButtonManualNavigation(gui, 100*i+0, x-2, y+recipeYOffset+heightPixels[i-1]/2-5, 8, 10, 0));
				pageButtons.add(new GuiButtonManualNavigation(gui, 100*i+1, x+122-16, y+recipeYOffset+heightPixels[i-1]/2-5, 8, 10, 1));
			}
			if(this.recipeLayout.get(stack).size() > 0)
				recipeYOffset += heightPixels[i-1]+8;
			i++;
		}
		super.onOpened(gui, x, y+recipeYOffset-2, pageButtons);
	}

	@Override
	public void render(GuiManual gui, int x, int y, int mx, int my)
	{
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();

		int totalYOff = 0;
		highlighted = ItemStack.EMPTY;
		for(int i = 0; i < recipeRows.length; i++)
		{
			Object stack = recipeRows[i];
			List<PositionedItemStack[]> rList = this.recipeLayout.get(stack);
			if(!rList.isEmpty()&&recipePage[i] >= 0&&recipePage[i] < this.recipeLayout.size())
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
				ManualUtils.drawTexturedRect(x+maxX-17, y+totalYOff+heightPixels[i]/2-5, 16, 10, 0/256f, 16/256f, 226/256f, 236/256f);

				totalYOff += heightPixels[i]+8;
			}
		}

		totalYOff = 0;
		GlStateManager.translate(0, 0, 300);
		boolean uni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(false);
		/*
		 RenderItem.getInstance().renderWithColor=true;*/
		for(int i = 0; i < recipeRows.length; i++)
		{
			Object stack = recipeRows[i];
			List<PositionedItemStack[]> rList = this.recipeLayout.get(stack);
			if(!rList.isEmpty()&&recipePage[i] >= 0&&recipePage[i] < this.recipeLayout.size())
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
				totalYOff += heightPixels[i]+8;
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
		GlStateManager.enableAlpha();
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		super.buttonPressed(gui, button);
		int r = button.id/100-1;
		if(r >= 0&&r < recipeRows.length)
		{
			if(button.id%100==0)
				recipePage[r]--;
			else
				recipePage[r]++;

			if(recipePage[r] >= this.recipeLayout.get(recipeRows[r]).size())
				recipePage[r] = 0;
			if(recipePage[r] < 0)
				recipePage[r] = this.recipeLayout.get(recipeRows[r]).size()-1;
		}
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		for(Object stack : recipeRows)
		{
			if(stack instanceof Object[])
			{
				for(Object subStack : (Object[])stack)
					if(subStack instanceof ItemStack&&
							((ItemStack)subStack).getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
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
		for(int i = 0; i < this.heightPixels.length; i++)
		{
			yOff += this.heightPixels[i]+8;
		}
		return yOff;
	}
}
