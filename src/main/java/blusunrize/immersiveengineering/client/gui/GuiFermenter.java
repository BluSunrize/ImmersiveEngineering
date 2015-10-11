package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.gui.ContainerFermenter;

public class GuiFermenter extends GuiContainer
{
	TileEntityFermenter tile;
	public GuiFermenter(InventoryPlayer inventoryPlayer, TileEntityFermenter tile)
	{
		super(new ContainerFermenter(inventoryPlayer, tile));
		this.tile=tile;
	}

	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft+111,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/fluidProducer.png", tooltip);
		if(mx>guiLeft+157&&mx<guiLeft+164 && my>guiTop+22&&my<guiTop+68)
			tooltip.add(tile.energyStorage.getEnergyStored()+"/"+tile.energyStorage.getMaxEnergyStored()+" RF");

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx-guiLeft, my-guiTop, fontRendererObj, guiLeft+xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/fluidProducer.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		if(tile.tick>0)
		{
			int h = tile.getScaledProgress(18);
			ClientUtils.drawGradientRect(guiLeft+80,guiTop+34+h, guiLeft+87,guiTop+52, 0xffd4d2ab, 0xffc4c29e);
		}

		int stored = (int)(46*(tile.energyStorage.getEnergyStored()/(float)tile.energyStorage.getMaxEnergyStored()));
		ClientUtils.drawGradientRect(guiLeft+157,guiTop+22+(46-stored), guiLeft+164,guiTop+68, 0xffb51500, 0xff600b00);

		ClientUtils.handleGuiTank(tile.tank, guiLeft+111,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/fluidProducer.png", null);
	}
}
