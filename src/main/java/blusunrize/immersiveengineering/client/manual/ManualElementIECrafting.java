/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

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

public abstract class ManualElementIECrafting extends SpecialManualElements
{
	protected final ItemStack[] stacks;
	protected final List<PositionedItemStack[]> recipes = new ArrayList<>();
	protected final List<ArrowPosition> arrowPositions = new ArrayList<>();
	protected int recipePage;
	protected int yOff;

	public ManualElementIECrafting(ManualInstance manual, ItemStack... stacks)
	{
		super(manual);
		this.stacks = stacks;
	}

	@Override
	public abstract void recalculateCraftingRecipes();

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
			PositionedItemStack[] pStacks = recipes.get(recipePage);
			int maxX = 0;
			for(PositionedItemStack pStack : pStacks)
			{
				if(pStack!=null)
				{
					if(pStack.x() > maxX)
						maxX = pStack.x();
					GuiComponent.fill(transform, x+pStack.x(), y+pStack.y(), x+pStack.x()+16, y+pStack.y()+16, 0x33666666);

					if(!pStack.getStackAtCurrentTime().isEmpty())
					{
						ManualUtils.renderItemStack(transform, pStack.getStackAtCurrentTime(), x+pStack.x(), y+pStack.y(), true, pStack.amount());
						if(mouseX >= x+pStack.x()&&mouseX < x+pStack.x()+16&&mouseY >= y+pStack.y()&&mouseY < y+pStack.y()+16)
							highlighted = pStack.getStackAtCurrentTime();
					}
				}
			}
			ArrowPosition arrow = arrowPositions.get(recipePage);
			ManualUtils.drawTexturedRect(transform, manual.texture, x+arrow.x(), y+arrow.y(), 16, 10, 0/256f, 16/256f, 226/256f, 236/256f);
		}

		this.renderHighlightedTooltip(transform, gui, mouseX, mouseY);
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		for(PositionedItemStack[] recipe : this.recipes)
			for(PositionedItemStack pStack : recipe)
				for(ItemStack stack : pStack.displayList())
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
				maxY = Math.max(maxY, pstack.y());
		return maxY+18;
	}

	record ArrowPosition(int x, int y){}
}
