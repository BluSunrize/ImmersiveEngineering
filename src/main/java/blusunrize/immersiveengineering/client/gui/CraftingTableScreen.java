/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.gui.CraftingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CraftingTableScreen extends IEContainerScreen<CraftingTableMenu>
{
	public CraftingTableScreen(CraftingTableMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("craftingtable"));
		this.imageHeight = 210;
		this.inventoryLabelY = this.imageHeight-91;
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		super.renderLabels(graphics, mouseX, mouseY);
		graphics.drawString(this.font, title, 8, 6, Lib.COLOUR_I_ImmersiveOrange, true);
	}
}