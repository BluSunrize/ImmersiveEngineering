/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.BlastFurnaceContainer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class BlastFurnaceScreen extends IEContainerScreen<BlastFurnaceContainer>
{
	public BlastFurnaceScreen(BlastFurnaceContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/blast_furnace.png");
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(container.state.getLastBurnTime() > 0)
		{
			int h = (int)(12*(container.state.getBurnTime()/(float)container.state.getLastBurnTime()));
			this.blit(guiLeft+56, guiTop+37+12-h, 179, 1+12-h, 9, h);
		}
		if(container.state.getMaxProcess() > 0)
		{
			int w = (int)(22*((container.state.getMaxProcess()-container.state.getProcess())/(float)container.state.getMaxProcess()));
			this.blit(guiLeft+76, guiTop+35, 177, 14, w, 16);
		}
	}
}
