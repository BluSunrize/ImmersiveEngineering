/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

public class GuiButtonDyeColor extends GuiButtonState<DyeColor>
{
	public GuiButtonDyeColor(
			int x, int y, String name, IntSupplier color,
			IIEPressable<GuiButtonState<DyeColor>> handler, BiConsumer<List<Component>, DyeColor> tooltip
	)
	{
		super(x, y, 8, 8, Component.nullToEmpty(name), DyeColor.values(), color, GuiReactiveList.TEXTURE, 0, 128, -1, handler, tooltip);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		super.render(graphics, mouseX, mouseY, partialTicks);
		if(this.visible)
			GuiHelper.drawColouredRect(graphics.pose(), getX()+2, getY()+2, 4, 4, getState());
	}
}
