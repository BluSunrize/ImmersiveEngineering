/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.gui.LogicUnitMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

public class LogicUnitScreen extends IEContainerScreen<LogicUnitMenu>
{
	private static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("logic_unit");

	public LogicUnitScreen(LogicUnitMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		for(int i = 0; i < 10; i++)
			graphics.drawCenteredString(this.font, ""+(i+1), 52+(i%5)*18, 23+(i/5)*18, DyeColor.GRAY.getTextColor());
	}
}