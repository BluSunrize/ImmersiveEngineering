/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.CraftingTableTileEntity;
import blusunrize.immersiveengineering.common.gui.CraftingTableContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public class CraftingTableScreen extends IEContainerScreen<CraftingTableContainer>
{
	public CraftingTableScreen(CraftingTableContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.ySize = 210;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY)
	{
		TileEntity te = container.tile;
		this.font.drawString(transform, ((CraftingTableTileEntity)te).getDisplayName().getString(),
				8, 6, 0x190b06);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture("immersiveengineering:textures/gui/craftingtable.png");
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}