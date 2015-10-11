package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.gui.ContainerCokeOven;

public class GuiCokeOven extends GuiContainer
{
	TileEntityCokeOven tile;
	public GuiCokeOven(InventoryPlayer inventoryPlayer, TileEntityCokeOven tile)
	{
		super(new ContainerCokeOven(inventoryPlayer, tile));
		this.tile=tile;
	}

	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft+129,guiTop+20, 16,47, 176,31,20,51, mx,my, "immersiveengineering:textures/gui/cokeOven.png", tooltip);
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx-guiLeft, my-guiTop, fontRendererObj, guiLeft+xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}
//		if(mx>guiLeft+129&&mx<guiLeft+129+16 && my>guiTop+20&&my<guiTop+20+47)
//		{
//			if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
//				tooltip.add(tile.tank.getFluid().getLocalizedName());
//			else
//				tooltip.add(StatCollector.translateToLocal("gui.ImmersiveEngineering.empty"));
//			tooltip.add(tile.tank.getFluidAmount()+"/"+tile.tank.getCapacity()+"mB");
//
//		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/cokeOven.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		if(tile.processMax>0&&tile.process>0)
		{
			int h = (int)(12*(tile.process/(float)tile.processMax));
			this.drawTexturedModalRect(guiLeft+59,guiTop+37+12-h, 179, 1+12-h, 9, h);
		}

//		if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
//		{
//			int h = (int)(47*(tile.tank.getFluid().amount/(float)tile.tank.getCapacity()));
//			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft+129,guiTop+20+47-h, 16, h);
//			ClientUtils.bindTexture("immersiveengineering:textures/gui/cokeOven.png");
//		}
//		this.drawTexturedModalRect(guiLeft+127,guiTop+18, 176,31, 20,51);
		ClientUtils.handleGuiTank(tile.tank, guiLeft+129,guiTop+20, 16,47, 176,31,20,51, mx,my, "immersiveengineering:textures/gui/cokeOven.png", null);
		
	}
}
