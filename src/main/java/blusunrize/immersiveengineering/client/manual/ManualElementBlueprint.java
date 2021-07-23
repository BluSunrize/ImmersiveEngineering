/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.PositionedItemStack;
import blusunrize.lib.manual.SpecialManualElements;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ManualElementBlueprint extends SpecialManualElements
{
	private final ItemStack[] stacks;
	private final List<PositionedItemStack[]> recipes = new ArrayList<>();
	private int recipePage;
	private int yOff;

	public ManualElementBlueprint(ManualInstance manual, ItemStack... stacks)
	{
		super(manual);
		this.stacks = stacks;
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		this.recipes.clear();

		for(String category : BlueprintCraftingRecipe.recipeCategories)
			for(BlueprintCraftingRecipe recipe : BlueprintCraftingRecipe.findRecipes(category))
				for(int iStack = 0; iStack < stacks.length; iStack++)
				{
					ItemStack output = stacks[iStack];
					if(!recipe.output.isEmpty()&&ManualUtils.stackMatchesObject(recipe.output, output)&&recipe.inputs!=null&&recipe.inputs.length > 0)
					{
						int h = (int)Math.ceil(recipe.inputs.length/2f);
						PositionedItemStack[] pIngredients = new PositionedItemStack[recipe.inputs.length+2];
						for(int i = 0; i < recipe.inputs.length; i++)
							pIngredients[i] = new PositionedItemStack(recipe.inputs[i].getMatchingStacks(), 32+i%2*18, i/2*18);
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
	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons)
	{
		if(this.recipes.size() > 1)
		{
			pageButtons.add(new GuiButtonManualNavigation(gui, x-2, y+yOff/2-3, 8, 10, 0, btn -> {
				--recipePage;
				if(recipePage < 0)
					recipePage = this.recipes.size()-1;
			}));
			pageButtons.add(new GuiButtonManualNavigation(gui, x+122-16, y+yOff/2-3, 8, 10, 1, btn -> {
				++recipePage;
				if(recipePage >= this.recipes.size())
					recipePage = 0;
			}));
		}
		super.onOpened(gui, x, y+yOff+2, pageButtons);
	}

	@Override
	public void render(PoseStack transform, ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		highlighted = ItemStack.EMPTY;

		if(!recipes.isEmpty()&&recipePage >= 0&&recipePage < this.recipes.size())
		{
			int maxX = 0;
			for(PositionedItemStack pstack : recipes.get(recipePage))
				if(pstack!=null)
				{
					if(pstack.x > maxX)
						maxX = pstack.x;
					GuiComponent.fill(transform, x+pstack.x, y+pstack.y, x+pstack.x+16, y+pstack.y+16, 0x33666666);
				}
			ManualUtils.drawTexturedRect(manual.texture, x+maxX-17, y+yOff/2-5, 16, 10, 0/256f, 16/256f, 226/256f, 236/256f);
		}

		if(!recipes.isEmpty()&&recipePage >= 0&&recipePage < this.recipes.size())
		{
			for(PositionedItemStack pstack : recipes.get(recipePage))
				if(pstack!=null)
					if(!pstack.getStack().isEmpty())
					{
						ManualUtils.renderItemStack(transform, pstack.getStack(), x+pstack.x, y+pstack.y, true);
						if(mouseX >= x+pstack.x&&mouseX < x+pstack.x+16&&mouseY >= y+pstack.y&&mouseY < y+pstack.y+16)
							highlighted = pstack.getStack();
					}
		}

		this.renderHighlightedTooltip(transform, gui, mouseX, mouseY);
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		for(PositionedItemStack[] recipe : this.recipes)
			for(PositionedItemStack pStack : recipe)
				for(ItemStack stack : pStack.getDisplayList())
					if(ManualUtils.listStack(searchTag, stack))
						return true;
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		int maxY = 0;
		for(PositionedItemStack[] recipe : recipes)
			for(PositionedItemStack pstack : recipe)
				maxY = Math.max(maxY, pstack.y);
		return maxY+18;
	}
}