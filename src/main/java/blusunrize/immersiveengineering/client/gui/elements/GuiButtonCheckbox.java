/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.client.Minecraft;

public class GuiButtonCheckbox extends GuiButtonState
{
	public GuiButtonCheckbox(int buttonId, int x, int y, String name, boolean state)
	{
		super(buttonId, x, y, 8, 8, name, state, "immersiveengineering:textures/gui/hud_elements.png", 0, 128, -1);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		if(this.visible&&state)
			this.drawCenteredString(mc.fontRenderer, "\u2714", x+width/2, y-2, !this.enabled?0xA0A0A0: this.hovered?Lib.COLOUR_I_ImmersiveOrange: 0xE0E0E0);
	}
}
