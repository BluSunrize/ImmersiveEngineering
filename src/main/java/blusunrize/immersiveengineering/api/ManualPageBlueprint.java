/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.GuiManual;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManualPageBlueprint extends ManualPages
{
	ItemStack[] stacks;
	ArrayList<PositionedItemStack[]> recipes = new ArrayList();
	int recipePage;
	int yOff;

	public ManualPageBlueprint(ManualInstance manual, String text, ItemStack... stacks)
	{
		super(manual, text);
		this.stacks = stacks;
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		this.recipes.clear();
		List<String> cmCategories = BlueprintCraftingRecipe.blueprintCategories;
		ArrayListMultimap<String, BlueprintCraftingRecipe> cmRecipes = BlueprintCraftingRecipe.recipeList;

		for(String category : cmCategories)
			for(BlueprintCraftingRecipe recipe : cmRecipes.get(category))
				for(int iStack = 0; iStack < stacks.length; iStack++)
				{
					ItemStack output = stacks[iStack];
					if(!recipe.output.isEmpty()&&ManualUtils.stackMatchesObject(recipe.output, output)&&recipe.inputs!=null&&recipe.inputs.length > 0)
					{
						int h = (int)Math.ceil(recipe.inputs.length/2f);
						PositionedItemStack[] pIngredients = new PositionedItemStack[recipe.inputs.length+2];
						for(int i = 0; i < recipe.inputs.length; i++)
							pIngredients[i] = new PositionedItemStack(recipe.inputs[i].getSizedStackList(), 32+i%2*18, i/2*18);
						int middle = (int)(h/2f*18)-8;
						pIngredients[pIngredients.length-2] = new PositionedItemStack(recipe.output, 86, middle);
						pIngredients[pIngredients.length-1] = new PositionedItemStack(BlueprintCraftingRecipe.getTypedBlueprint(category), 8, middle);

						if(iStack < this.recipes.size())
							this.recipes.add(iStack, pIngredients);
						else
							this.recipes.add(pIngredients);
						if(h*18 > yOff)
							yOff = h*18;
					}
				}
		if(providedItems!=null)
			this.providedItems.clear();
		for(ItemStack stack : stacks)
			this.addProvidedItem(stack);
	}

	@Override
	public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		if(this.recipes.size() > 1)
		{
			pageButtons.add(new GuiButtonManualNavigation(gui, 100+0, x-2, y+yOff/2-3, 8, 10, 0));
			pageButtons.add(new GuiButtonManualNavigation(gui, 100+1, x+122-16, y+yOff/2-3, 8, 10, 1));
		}
		super.initPage(gui, x, y+yOff+2, pageButtons);
	}

	@Override
	public void renderPage(GuiManual gui, int x, int y, int mx, int my)
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

		manual.fontRenderer.setUnicodeFlag(uni);
		if(localizedText!=null&&!localizedText.isEmpty())
			ManualUtils.drawSplitString(manual.fontRenderer, localizedText, x, y+yOff+2, 120, manual.getTextColour());
		//			manual.fontRenderer.drawSplitString(localizedText, x,y+yOff+2, 120, manual.getTextColour());

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
}