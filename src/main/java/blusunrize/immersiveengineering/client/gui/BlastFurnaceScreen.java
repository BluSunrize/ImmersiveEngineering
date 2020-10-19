/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlastFurnacePreheaterTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceAdvancedTileEntity;
import blusunrize.immersiveengineering.common.gui.BlastFurnaceContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Function;

public class BlastFurnaceScreen extends IEContainerScreen<BlastFurnaceContainer>
{
	private static final Function<BlastFurnacePreheaterTileEntity, Boolean> PREHEATER_ACTIVE = tile -> tile.active;

	public BlastFurnaceScreen(BlastFurnaceContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		if(container.tile instanceof BlastFurnaceAdvancedTileEntity)
			this.xSize = 210;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		if(container.tile instanceof BlastFurnaceAdvancedTileEntity)
		{
			String title = I18n.format(Lib.GUI+"blast_furnace.preheaters");
			int w = this.font.getStringWidth(title)/2;
			this.font.drawString(title, 175-w, 18, 0xAEAEAE);
			this.font.drawString(I18n.format(Lib.GUI+"left"), 154, 28, 0xAEAEAE);
			this.font.drawString(I18n.format(Lib.GUI+"right"), 154, 40, 0xAEAEAE);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/blast_furnace.png");
		this.blit(guiLeft, guiTop, 0, 0, 176, ySize);

		if(container.tile instanceof BlastFurnaceAdvancedTileEntity)
		{
			BlastFurnaceAdvancedTileEntity tile = (BlastFurnaceAdvancedTileEntity)container.tile;
			this.blit(guiLeft+140, guiTop+11, 176, 32, 70, 46);
			if(tile.getFromPreheater(true, PREHEATER_ACTIVE, false))
				this.blit(guiLeft+182, guiTop+27, 200, 22, 10, 10);
			if(tile.getFromPreheater(false, PREHEATER_ACTIVE, false))
				this.blit(guiLeft+182, guiTop+39, 200, 22, 10, 10);
		}

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
