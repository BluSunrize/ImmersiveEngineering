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
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import blusunrize.immersiveengineering.common.gui.ContainerBelljar;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.ArrayList;

public class GuiBelljar extends GuiIEContainerBase
{
	TileEntityBelljar tile;

	public GuiBelljar(InventoryPlayer inventoryPlayer, TileEntityBelljar tile)
	{
		super(new ContainerBelljar(inventoryPlayer, tile));
		this.tile = tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft+8, guiTop+8, 16, 47, 176, 30, 20, 51, mx, my, "immersiveengineering:textures/gui/belljar.png", tooltip);
		if(mx > guiLeft+30&&mx < guiLeft+37&&my > guiTop+22&&my < guiTop+68)
		{
			tooltip.add(I18n.format(Lib.DESC_INFO+"fertFill", Utils.formatDouble(tile.fertilizerAmount/(float)IEConfig.Machines.belljar_fertilizer, "0.00")));
			tooltip.add(I18n.format(Lib.DESC_INFO+"fertMod", Utils.formatDouble(tile.fertilizerMod, "0.00")));

		}
		if(mx > guiLeft+158&&mx < guiLeft+165&&my > guiTop+22&&my < guiTop+68)
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF");
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
		GlStateManager.enableBlend();
		ClientUtils.bindTexture("immersiveengineering:textures/gui/belljar.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		GlStateManager.disableBlend();

		ClientUtils.handleGuiTank(tile.tank, guiLeft+8, guiTop+8, 16, 47, 176, 30, 20, 51, mx, my, "immersiveengineering:textures/gui/belljar.png", null);
		int stored = (int)(46*(tile.fertilizerAmount/(float)IEConfig.Machines.belljar_fertilizer));
		ClientUtils.drawGradientRect(guiLeft+30, guiTop+22+(46-stored), guiLeft+37, guiTop+68, 0xff95ed00, 0xff8a5a00);

		stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+158, guiTop+22+(46-stored), guiLeft+165, guiTop+68, 0xffb51500, 0xff600b00);
	}
}