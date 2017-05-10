package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.gui.ContainerCrate;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

public class GuiCrate extends GuiContainer
{
	public GuiCrate(InventoryPlayer inventoryPlayer, TileEntityWoodenCrate tile )
	{
		super(new ContainerCrate(inventoryPlayer, tile));
		this.ySize=168;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.fontRenderer.drawString(((ContainerCrate)this.inventorySlots).tile.getDisplayName().getUnformattedText(), 8, 6, 0x0a0a0a);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/crate.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 	0, xSize, ySize);
	}
}