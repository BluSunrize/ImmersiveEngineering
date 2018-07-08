/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.gui.ContainerArcFurnace;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.ArrayList;

public class GuiArcFurnace extends GuiIEContainerBase
{
	TileEntityArcFurnace tile;

	public GuiArcFurnace(InventoryPlayer inventoryPlayer, TileEntityArcFurnace tile)
	{
		super(new ContainerArcFurnace(inventoryPlayer, tile));
		this.ySize = 207;
		this.tile = tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		if(mx > guiLeft+157&&mx < guiLeft+164&&my > guiTop+22&&my < guiTop+68)
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" RF");

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color(1, 1, 1, 1);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/arc_furnace.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(MultiblockProcess process : tile.processQueue)
			if(process instanceof MultiblockProcessInMachine)
			{
				float mod = process.processTick/(float)process.maxTicks;
				int slot = ((MultiblockProcessInMachine)process).getInputSlots()[0];
				int h = (int)Math.max(1, mod*16);
				this.drawTexturedModalRect(guiLeft+27+slot%3*21, guiTop+34+slot/3*18+(16-h), 176, 16-h, 2, h);
			}
//		for(int i=0; i<12; i++)
//			if(tile.process[i]>0 && tile.processMax[i]>0)
//			{
//				float mod = tile.process[i]/(float)tile.processMax[i];
//				int h = (int)Math.max(1, mod*16);
//				this.drawTexturedModalRect(guiLeft+27+i%3*21,guiTop+34+i/3*18+(16-h), 176,16-h, 2,h);
//			}

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+157, guiTop+22+(46-stored), guiLeft+164, guiTop+68, 0xffb51500, 0xff600b00);
	}
}
