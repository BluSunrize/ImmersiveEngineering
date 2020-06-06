/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import net.minecraft.item.DyeColor;

public class GuiButtonDyeColor extends GuiButtonState<DyeColor>
{
	public GuiButtonDyeColor(int x, int y, String name, DyeColor initialColor, IIEPressable<GuiButtonState<DyeColor>> handler)
	{
		super(x, y, 8, 8, name, DyeColor.values(), initialColor.ordinal(), "immersiveengineering:textures/gui/hud_elements.png", 0, 128, -1, handler);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		super.render(mouseX, mouseY, partialTicks);
		if(this.visible)
		{
			DyeColor dye = getState();
			int col = 0xff000000|dye.colorValue;
			this.fillGradient(x+2, y+2, x+6, y+6, col, col);
		}
	}
}
