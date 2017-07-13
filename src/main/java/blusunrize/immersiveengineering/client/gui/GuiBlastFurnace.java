package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.gui.ContainerBlastFurnace;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

public class GuiBlastFurnace extends GuiIEContainerBase
{
	TileEntityBlastFurnace tile;
	public GuiBlastFurnace(InventoryPlayer inventoryPlayer, TileEntityBlastFurnace tile)
	{
		super(new ContainerBlastFurnace(inventoryPlayer, tile));
		this.tile=tile;
	}
	

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/blast_furnace.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		if(tile.lastBurnTime>0)
		{
			int h = (int)(12*(tile.burnTime/(float)tile.lastBurnTime));
			this.drawTexturedModalRect(guiLeft+56,guiTop+37+12-h, 179, 1+12-h, 9, h);
		}
		if(tile.processMax>0)
		{
			int w = (int)(22*( (tile.processMax-tile.process)/(float)tile.processMax));
			this.drawTexturedModalRect(guiLeft+76,guiTop+35, 177, 14, w, 16);
		}
	}
}
