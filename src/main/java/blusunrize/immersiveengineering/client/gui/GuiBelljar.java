package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import blusunrize.immersiveengineering.common.gui.ContainerBelljar;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.ArrayList;

public class GuiBelljar extends GuiContainer
{
	TileEntityBelljar tile;
	public GuiBelljar(InventoryPlayer inventoryPlayer, TileEntityBelljar tile )
	{
		super(new ContainerBelljar(inventoryPlayer, tile));
		this.tile = tile;
	}
	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		if(mx>guiLeft+158&&mx<guiLeft+165 && my>guiTop+22&&my<guiTop+68)
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" RF");
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRendererObj, guiLeft+xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		ClientUtils.bindTexture("immersiveengineering:textures/gui/belljar.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);
		GlStateManager.disableBlend();

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+158,guiTop+22+(46-stored), guiLeft+165,guiTop+68, 0xffb51500, 0xff600b00);
	}
}