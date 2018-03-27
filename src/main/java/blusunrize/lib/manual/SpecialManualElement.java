/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiManual;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;

import java.util.List;

public abstract class SpecialManualElement
{
	public abstract int getLinesTaken();

	public abstract void onOpened(GuiManual m, int x, int y, List<GuiButton> buttons);

	public abstract void render(GuiManual m, int x, int y, int mouseX, int mouseY);

	public abstract void buttonPressed(GuiManual gui, GuiButton button);

	public abstract void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, GuiButton button);

	public abstract boolean listForSearch(String searchTag);

	public abstract void recalculateCraftingRecipes();

	public ItemStack[] getProvidedRecipes()
	{
		return new ItemStack[0];
	}

	public ItemStack getHighlightedStack()
	{
		return ItemStack.EMPTY;
	}

	public boolean isAbove()
	{
		return true;
	}

}
