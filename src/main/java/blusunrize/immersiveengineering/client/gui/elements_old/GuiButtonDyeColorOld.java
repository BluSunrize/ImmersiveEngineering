/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements_old;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

public class GuiButtonDyeColorOld extends GuiButtonStateOld<DyeColor>
{
	public GuiButtonDyeColorOld(
			int x, int y, String name, IntSupplier color,
			IIEPressable<GuiButtonStateOld<DyeColor>> handler, BiConsumer<List<Component>, DyeColor> tooltip
	)
	{
		super(x, y, 8, 8, Component.nullToEmpty(name), DyeColor.values(), color, GuiReactiveListOld.TEXTURE, 0, 128, -1, handler, tooltip);
	}

	public GuiButtonDyeColorOld(
			int x, int y, int w, int h, IntSupplier color, ResourceLocation texture, int u, int v,
			IIEPressable<GuiButtonStateOld<DyeColor>> handler, BiConsumer<List<Component>, DyeColor> tooltip
	)
	{
		super(x, y, w, h, Component.empty(), DyeColor.values(), color, texture, u, v, -1, handler, tooltip);
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		super.renderWidget(graphics, mouseX, mouseY, partialTicks);
		GuiHelper.drawColouredRect(graphics, getX()+2, getY()+2, width-4, height-4, getState());
	}
}
