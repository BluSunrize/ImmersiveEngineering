/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.render.tooltip;

import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public record RevolverClientTooltip(RevolverServerTooltip data) implements ClientTooltipComponent
{
	@Override
	public int getHeight()
	{
		return 40;
	}

	@Override
	public int getWidth(Font pFont)
	{
		return 40;
	}

	@Override
	public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics graphics)
	{
		graphics.pose().pushPose();
		graphics.pose().translate(mouseX, mouseY, 0);
		graphics.pose().scale(.5f, .5f, 1);
		RevolverScreen.drawExternalGUI(data.bullets(), data.bulletCount(), graphics);
		graphics.pose().popPose();
	}
}
