package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.gui.ContainerSqueezer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiSqueezer extends GuiIEContainerBase
{
	TileEntitySqueezer tile;
	public GuiSqueezer(InventoryPlayer inventoryPlayer, TileEntitySqueezer tile)
	{
		super(new ContainerSqueezer(inventoryPlayer, tile));
		this.tile=tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+112,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/cokeOven.png", tooltip);
		if(mx>guiLeft+158&&mx<guiLeft+165 && my>guiTop+22&&my<guiTop+68)
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" RF");
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/squeezer.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+158,guiTop+22+(46-stored), guiLeft+165,guiTop+68, 0xffb51500, 0xff600b00);

		//		if(tile.processMax>0&&tile.process>0)
		//		{
		//			int h = (int)(12*(tile.process/(float)tile.processMax));
		//			this.drawTexturedModalRect(guiLeft+59,guiTop+37+12-h, 179, 1+12-h, 9, h);
		//		}

		//		if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
		//		{
		//			int h = (int)(47*(tile.tank.getFluid().amount/(float)tile.tank.getCapacity()));
		//			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft+129,guiTop+20+47-h, 16, h);
		//			ClientUtils.bindTexture("immersiveengineering:textures/gui/cokeOven.png");
		//		}
		//		this.drawTexturedModalRect(guiLeft+127,guiTop+18, 176,31, 20,51);
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+112,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersiveengineering:textures/gui/squeezer.png", null);

	}
}
