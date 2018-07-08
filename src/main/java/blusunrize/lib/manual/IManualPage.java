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

public interface IManualPage
{
	ManualInstance getManualHelper();

	void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons);

	void renderPage(GuiManual gui, int x, int y, int mx, int my);

	void buttonPressed(GuiManual gui, GuiButton button);

	void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button);

	boolean listForSearch(String searchTag);

	void recalculateCraftingRecipes();

	default ItemStack[] getProvidedRecipes()
	{
		return new ItemStack[0];
	}

	default ItemStack getHighlightedStack()
	{
		return ItemStack.EMPTY;
	}
}