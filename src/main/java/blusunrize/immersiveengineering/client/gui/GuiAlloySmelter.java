/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityAlloySmelter;
import blusunrize.immersiveengineering.common.gui.ContainerAlloySmelter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiAlloySmelter extends GuiIEContainerBase
{
	TileEntityAlloySmelter tile;

	public GuiAlloySmelter(InventoryPlayer inventoryPlayer, TileEntityAlloySmelter tile)
	{
		super(new ContainerAlloySmelter(inventoryPlayer, tile));
		this.tile = tile;
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/alloy_smelter.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(tile.lastBurnTime > 0)
		{
			int h = (int)(12*(tile.burnTime/(float)tile.lastBurnTime));
			this.drawTexturedModalRect(guiLeft+56, guiTop+37+12-h, 179, 1+12-h, 9, h);
		}
		if(tile.processMax > 0)
		{
			int w = (int)(22*((tile.processMax-tile.process)/(float)tile.processMax));
			this.drawTexturedModalRect(guiLeft+84, guiTop+35, 177, 14, w, 16);
		}
	}
}
