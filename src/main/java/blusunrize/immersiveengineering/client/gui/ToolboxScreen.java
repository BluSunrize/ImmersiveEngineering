/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.gui.ToolboxMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.function.Consumer;

public class ToolboxScreen extends IEContainerScreen<ToolboxMenu>
{
	public ToolboxScreen(ToolboxMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("toolbox"));
		this.imageHeight = 238;
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		int slot = -1;
		for(int i = 0; i < menu.ownSlotCount; i++)
		{
			Slot s = menu.getSlot(i);
			if(!s.hasItem()&&mouseX > leftPos+s.x&&mouseX < leftPos+s.x+16&&mouseY > topPos+s.y&&mouseY < topPos+s.y+16)
				slot = i;
		}
		if(slot >= 0)
		{
			String areaKey = slot < 3?"food": slot < 10?"tool": slot < 16?"wire": "any";
			addGray.accept(Component.translatable(Lib.DESC_INFO+"toolbox."+areaKey));
		}
	}

	@Override
	protected void drawBackgroundTexture(GuiGraphics graphics)
	{
		graphics.blit(background, leftPos, topPos-17, 0, 0, 176, imageHeight+17);
	}
}