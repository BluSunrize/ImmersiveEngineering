/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.gui.ContainerCokeOven;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.ArrayList;

public class GuiCokeOven extends GuiIEContainerBase
{
	TileEntityCokeOven tile;

	public GuiCokeOven(InventoryPlayer inventoryPlayer, TileEntityCokeOven tile)
	{
		super(new ContainerCokeOven(inventoryPlayer, tile));
		this.tile = tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft+129, guiTop+20, 16, 47, 176, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/coke_oven.png", tooltip);
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/coke_oven.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(tile.processMax > 0&&tile.process > 0)
		{
			int h = (int)(12*(tile.process/(float)tile.processMax));
			this.drawTexturedModalRect(guiLeft+59, guiTop+37+12-h, 179, 1+12-h, 9, h);
		}

//		if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
//		{
//			int h = (int)(47*(tile.tank.getFluid().amount/(float)tile.tank.getCapacity()));
//			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft+129,guiTop+20+47-h, 16, h);
//			ClientUtils.bindTexture("immersiveengineering:textures/gui/cokeOven.png");
//		}
//		this.drawTexturedModalRect(guiLeft+127,guiTop+18, 176,31, 20,51);
		ClientUtils.handleGuiTank(tile.tank, guiLeft+129, guiTop+20, 16, 47, 176, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/coke_oven.png", null);

	}
}
