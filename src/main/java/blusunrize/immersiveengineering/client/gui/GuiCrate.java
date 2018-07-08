/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.gui.ContainerCrate;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiCrate extends GuiIEContainerBase
{
	public GuiCrate(InventoryPlayer inventoryPlayer, TileEntityWoodenCrate tile)
	{
		super(new ContainerCrate(inventoryPlayer, tile));
		this.ySize = 168;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.fontRenderer.drawString(((ContainerCrate)this.inventorySlots).tile.getDisplayName().getUnformattedText(), 8, 6, 0x0a0a0a);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/crate.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}