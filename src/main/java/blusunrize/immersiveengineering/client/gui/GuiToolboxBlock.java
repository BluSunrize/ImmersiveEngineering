/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityToolbox;
import blusunrize.immersiveengineering.common.gui.ContainerToolboxBlock;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;

public class GuiToolboxBlock extends GuiIEContainerBase
{
	public GuiToolboxBlock(InventoryPlayer inventoryPlayer, TileEntityToolbox toolbox)
	{
		super(new ContainerToolboxBlock(inventoryPlayer, toolbox));
		this.ySize = 238;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		int slot = -1;
		for(int i = 0; i < ((ContainerToolboxBlock)this.inventorySlots).slotCount; i++)
		{
			Slot s = this.inventorySlots.inventorySlots.get(i);
			if(!s.getHasStack()&&mx > guiLeft+s.xPos&&mx < guiLeft+s.xPos+16&&my > guiTop+s.yPos&&my < guiTop+s.yPos+16)
				slot = i;
		}
		String ss = null;
		if(slot >= 0)
			ss = slot < 3?"food": slot < 10?"tool": slot < 16?"wire": "any";
		if(ss!=null)
			tooltip.add(TextFormatting.GRAY+I18n.format(Lib.DESC_INFO+"toolbox."+ss));
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/toolbox.png");
		this.drawTexturedModalRect(guiLeft, guiTop-17, 0, 0, 176, ySize+17);
	}

}