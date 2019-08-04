/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import blusunrize.immersiveengineering.common.gui.ContainerCrate;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;

public class CrateScreen extends IEContainerScreen
{
	public CrateScreen(PlayerInventory inventoryPlayer, WoodenCrateTileEntity tile)
	{
		super(new ContainerCrate(inventoryPlayer, tile), inventoryPlayer);
		this.ySize = 168;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		TileEntity te = ((ContainerCrate)container).tile;
		this.font.drawString(((WoodenCrateTileEntity)te).getDisplayName().getFormattedText(),
				8, 6, 0x0a0a0a);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/crate.png");
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}