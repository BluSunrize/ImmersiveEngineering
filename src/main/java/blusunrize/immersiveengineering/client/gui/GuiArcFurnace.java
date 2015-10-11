package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.gui.ContainerArcFurnace;

public class GuiArcFurnace extends GuiContainer
{
	TileEntityArcFurnace tile;
	public GuiArcFurnace(InventoryPlayer inventoryPlayer, TileEntityArcFurnace tile)
	{
		super(new ContainerArcFurnace(inventoryPlayer, tile));
		this.ySize=207;
		this.tile=tile;
	}

	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
		ArrayList<String> tooltip = new ArrayList<String>();
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
		ClientUtils.bindTexture("immersiveengineering:textures/gui/arcFurnace.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		for(int i=0; i<12; i++)
			if(tile.process[i]>0 && tile.processMax[i]>0)
			{
				float mod = tile.process[i]/(float)tile.processMax[i];
				int h = (int)Math.max(1, mod*16);
				this.drawTexturedModalRect(guiLeft+27+i%3*21,guiTop+34+i/3*18+(16-h), 176,16-h, 2,h);
			}

		int stored = (int)(46*(tile.energyStorage.getEnergyStored()/(float)tile.energyStorage.getMaxEnergyStored()));
		ClientUtils.drawGradientRect(guiLeft+157,guiTop+22+(46-stored), guiLeft+164,guiTop+68, 0xffb51500, 0xff600b00);
	}
}
