/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

public class GuiButtonDyeColor extends GuiButtonState<DyeColor>
{
	public GuiButtonDyeColor(int x, int y, String name, DyeColor initialColor, IIEPressable<GuiButtonState<DyeColor>> handler)
	{
		super(x, y, 8, 8, Component.nullToEmpty(name), DyeColor.values(), initialColor.ordinal(), GuiReactiveList.TEXTURE, 0, 128, -1, handler);
	}

	@Override
	public void render(PoseStack transform, int mouseX, int mouseY, float partialTicks)
	{
		super.render(transform, mouseX, mouseY, partialTicks);
		if(this.visible)
		{
			DyeColor dye = getState();
			int col = 0xff000000|dye.getColorValue();
			this.fillGradient(transform, x+2, y+2, x+6, y+6, col, col);
		}
	}
}
