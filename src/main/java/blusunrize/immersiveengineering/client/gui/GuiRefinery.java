package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.gui.ContainerRefinery;

public class GuiRefinery extends GuiContainer
{
	TileEntityRefinery tile;
	public GuiRefinery(InventoryPlayer inventoryPlayer, TileEntityRefinery tile)
	{
		super(new ContainerRefinery(inventoryPlayer, tile));
		this.tile=tile;
	}

	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
		ArrayList<String> tooltip = new ArrayList();
		//		if(mx>guiLeft+98&&mx<guiLeft+98+16 && my>guiTop+21&&my<guiTop+21+47)
		//		{
		//			tooltip = new ArrayList<String>();
		//			if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
		//				tooltip.add(tile.tank.getFluid().getLocalizedName());
		//			else
		//				tooltip.add(StatCollector.translateToLocal("gui.ImmersiveEngineering.empty"));
		//			tooltip.add(tile.tank.getFluidAmount()+"/"+tile.tank.getCapacity()+"mB");
		//		}
		ClientUtils.handleGuiTank(tile.tank0, guiLeft+ 13,guiTop+20, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/refinery.png", tooltip);
		ClientUtils.handleGuiTank(tile.tank1, guiLeft+ 61,guiTop+20, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/refinery.png", tooltip);
		ClientUtils.handleGuiTank(tile.tank2, guiLeft+109,guiTop+20, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/refinery.png", tooltip);
		if(mx>guiLeft+157&&mx<guiLeft+164 && my>guiTop+21&&my<guiTop+67)
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
		ClientUtils.bindTexture("immersiveengineering:textures/gui/refinery.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		//		if(tile.tick>0)
		//		{
		//			int h = (int)(18*(tile.tick/80f));
		//			ClientUtils.drawGradientRect(guiLeft+83,guiTop+34+h, guiLeft+90,guiTop+52, 0xffd4d2ab, 0xffc4c29e);
		//		}

		int stored = (int)(46*(tile.energyStorage.getEnergyStored()/(float)tile.energyStorage.getMaxEnergyStored()));
		ClientUtils.drawGradientRect(guiLeft+157,guiTop+21+(46-stored), guiLeft+164,guiTop+67, 0xffb51500, 0xff600b00);

		ClientUtils.handleGuiTank(tile.tank0, guiLeft+ 13,guiTop+20, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/refinery.png", null);
		ClientUtils.handleGuiTank(tile.tank1, guiLeft+ 61,guiTop+20, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/refinery.png", null);
		ClientUtils.handleGuiTank(tile.tank2, guiLeft+109,guiTop+20, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/refinery.png", null);
		
		
		//		if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
		//		{
		//			int h = (int)(47*(tile.tank.getFluid().amount/(float)tile.tank.getCapacity()));
		//			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft+98,guiTop+21+47-h, 16, h);
		//			ClientUtils.bindTexture("immersiveengineering:textures/gui/fluidProducer.png");
		//		}
		//		this.drawTexturedModalRect(guiLeft+96,guiTop+19, 177,31, 20,51);
	}
}