package blusunrize.immersiveengineering.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;

public class GuiModWorkbench extends GuiContainer
{
//	TileEntityModWorkbench workbench;
	public GuiModWorkbench(InventoryPlayer inventoryPlayer, TileEntityModWorkbench tile )
	{
		super(new ContainerModWorkbench(inventoryPlayer, tile));
//		workbench = tile;
		this.ySize=168;
	}

	protected void drawGuiContainerForegroundLayer(int mx, int my)
	{
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/workbench.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);
		
		
		for(int i=0; i<((ContainerModWorkbench)inventorySlots).slotCount; i++)
		{
			Slot s = inventorySlots.getSlot(i);

			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition-1, guiTop+ s.yDisplayPosition-1, 17,1, 0x77222222);
			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition-1, guiTop+ s.yDisplayPosition+0, 1,16, 0x77222222);
			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition+16, guiTop+ s.yDisplayPosition+0, 1,17, 0x77999999);
			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition+0, guiTop+ s.yDisplayPosition+16, 16,1, 0x77999999);
			ClientUtils.drawColouredRect(guiLeft+ s.xDisplayPosition+0, guiTop+ s.yDisplayPosition+0, 16,16, 0x77444444);
			
//			ClientUtils.drawColouredRect(guiLeft+s.xDisplayPosition, guiTop+s.yDisplayPosition, 16, 16, 0xffffffff);
		}
	}
}