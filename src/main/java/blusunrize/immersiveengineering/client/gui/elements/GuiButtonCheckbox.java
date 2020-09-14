/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;

public class GuiButtonCheckbox extends GuiButtonBoolean
{
	public GuiButtonCheckbox(int x, int y, String name, boolean state, IIEPressable<GuiButtonState<Boolean>> handler)
	{
		super(x, y, 8, 8, name, state, "immersiveengineering:textures/gui/hud_elements.png", 0, 128, -1, handler);
	}

	@Override
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
	{
		super.render(transform, mouseX, mouseY, partialTicks);
		if(this.visible&&getState())
		{
			int color;
			if(!this.active)
				color = 0xA0A0A0;
			else if(this.isHovered)
				color = Lib.COLOUR_I_ImmersiveOrange;
			else
				color = 0xE0E0E0;
			this.drawCenteredString(transform, Minecraft.getInstance().fontRenderer, "\u2714", x+width/2, y-2, color);
		}
	}
}
