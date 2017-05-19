package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityAlloySmelter;
import blusunrize.immersiveengineering.common.gui.ContainerAlloySmelter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

public class GuiAlloySmelter extends GuiContainer
{
	TileEntityAlloySmelter tile;
	public GuiAlloySmelter(InventoryPlayer inventoryPlayer, TileEntityAlloySmelter tile)
	{
		super(new ContainerAlloySmelter(inventoryPlayer, tile));
		this.tile=tile;
	}
	

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/alloy_smelter.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		if(tile.lastBurnTime>0)
		{
			int h = (int)(12*(tile.burnTime/(float)tile.lastBurnTime));
			this.drawTexturedModalRect(guiLeft+56,guiTop+37+12-h, 179, 1+12-h, 9, h);
		}
		if(tile.processMax>0)
		{
			int w = (int)(22*( (tile.processMax-tile.process)/(float)tile.processMax));
			this.drawTexturedModalRect(guiLeft+84,guiTop+35, 177, 14, w, 16);
		}
	}
}
