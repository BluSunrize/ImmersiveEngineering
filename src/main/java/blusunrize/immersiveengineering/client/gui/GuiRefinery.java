/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.gui.ContainerRefinery;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.ArrayList;

public class GuiRefinery extends GuiIEContainerBase
{
	TileEntityRefinery tile;

	public GuiRefinery(InventoryPlayer inventoryPlayer, TileEntityRefinery tile)
	{
		super(new ContainerRefinery(inventoryPlayer, tile));
		this.tile = tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList();
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+13, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/refinery.png", tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+61, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/refinery.png", tooltip);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft+109, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/refinery.png", tooltip);
		if(mx > guiLeft+157&&mx < guiLeft+164&&my > guiTop+21&&my < guiTop+67)
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
		ClientUtils.bindTexture("immersiveengineering:textures/gui/refinery.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		//		if(tile.tick>0)
		//		{
		//			int h = (int)(18*(tile.tick/80f));
		//			ClientUtils.drawGradientRect(guiLeft+83,guiTop+34+h, guiLeft+90,guiTop+52, 0xffd4d2ab, 0xffc4c29e);
		//		}

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+157, guiTop+21+(46-stored), guiLeft+164, guiTop+67, 0xffb51500, 0xff600b00);

		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+13, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/refinery.png", null);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+61, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/refinery.png", null);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft+109, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/refinery.png", null);


		//		if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
		//		{
		//			int h = (int)(47*(tile.tank.getFluid().amount/(float)tile.tank.getCapacity()));
		//			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft+98,guiTop+21+47-h, 16, h);
		//			ClientUtils.bindTexture("immersiveengineering:textures/gui/fluidProducer.png");
		//		}
		//		this.drawTexturedModalRect(guiLeft+96,guiTop+19, 177,31, 20,51);
	}
}