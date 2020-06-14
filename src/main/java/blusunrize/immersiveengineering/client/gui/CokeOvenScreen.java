/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenTileEntity;
import blusunrize.immersiveengineering.common.gui.CokeOvenContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class CokeOvenScreen extends IEContainerScreen<CokeOvenContainer>
{
	private CokeOvenTileEntity tile;

	public CokeOvenScreen(CokeOvenContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		List<ITextComponent> tooltip = new ArrayList<>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft+129, guiTop+20, 16, 47, 176, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/coke_oven.png", tooltip);
		if(!tooltip.isEmpty())
			ClientUtils.drawHoveringText(tooltip, mx, my, font, width, height);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/coke_oven.png");
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(tile.processMax > 0&&tile.process > 0)
		{
			int h = (int)(12*(tile.process/(float)tile.processMax));
			this.blit(guiLeft+59, guiTop+37+12-h, 179, 1+12-h, 9, h);
		}

		ClientUtils.handleGuiTank(tile.tank, guiLeft+129, guiTop+20, 16, 47, 176, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/coke_oven.png", null);

	}
}
