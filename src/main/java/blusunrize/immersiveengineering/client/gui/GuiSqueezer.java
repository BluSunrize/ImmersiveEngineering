package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.gui.ContainerSqueezer;

public class GuiSqueezer extends GuiContainer
{
	TileEntitySqueezer tile;
	public GuiSqueezer(InventoryPlayer inventoryPlayer, TileEntitySqueezer tile)
	{
		super(new ContainerSqueezer(inventoryPlayer, tile));
		this.tile=tile;
	}

	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
		ArrayList<String> tooltip = null;
		if(mx>guiLeft+98&&mx<guiLeft+98+16 && my>guiTop+21&&my<guiTop+21+47)
		{
			tooltip = new ArrayList<String>();
			if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
				tooltip.add(tile.tank.getFluid().getLocalizedName());
			else
				tooltip.add(StatCollector.translateToLocal("gui.ImmersiveEngineering.empty"));
			tooltip.add(tile.tank.getFluidAmount()+"/"+tile.tank.getCapacity()+"mB");
		}
		if(mx>guiLeft+157&&mx<guiLeft+164 && my>guiTop+22&&my<guiTop+68)
		{
			tooltip = new ArrayList<String>();
			tooltip.add(tile.energyStorage.getEnergyStored()+"/"+tile.energyStorage.getMaxEnergyStored()+" RF");
		}

		if(tooltip!=null)
		{
			this.drawHoveringText(tooltip, mx-guiLeft, my-guiTop, fontRendererObj);
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
			int h = (int)(18*(tile.tick/80f));
			ClientUtils.drawGradientRect(guiLeft+83,guiTop+34+h, guiLeft+90,guiTop+52, 0xffd4d2ab, 0xffc4c29e);
		}

		int stored = (int)(46*(tile.energyStorage.getEnergyStored()/(float)tile.energyStorage.getMaxEnergyStored()));
		ClientUtils.drawGradientRect(guiLeft+157,guiTop+22+(46-stored), guiLeft+164,guiTop+68, 0xffb51500, 0xff600b00);

		if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
		{
			int h = (int)(47*(tile.tank.getFluid().amount/(float)tile.tank.getCapacity()));
			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft+98,guiTop+21+47-h, 16, h);
			ClientUtils.bindTexture("immersiveengineering:textures/gui/fluidProducer.png");
		}
		this.drawTexturedModalRect(guiLeft+96,guiTop+19, 177,31, 20,51);
	}
}
