/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import blusunrize.immersiveengineering.common.gui.AlloySmelterContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class AlloySmelterScreen extends IEContainerScreen<AlloySmelterContainer>
{
	private AlloySmelterTileEntity tile;

	public AlloySmelterScreen(AlloySmelterContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		clearIntArray(tile.guiState);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture("immersiveengineering:textures/gui/alloy_smelter.png");
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);

		if(tile.lastBurnTime > 0)
		{
			int h = (int)(12*(tile.burnTime/(float)tile.lastBurnTime));
			this.blit(transform, guiLeft+56, guiTop+37+12-h, 179, 1+12-h, 9, h);
		}
		if(tile.processMax > 0)
		{
			int w = (int)(22*((tile.processMax-tile.process)/(float)tile.processMax));
			this.blit(transform, guiLeft+84, guiTop+35, 177, 14, w, 16);
		}
	}
}
