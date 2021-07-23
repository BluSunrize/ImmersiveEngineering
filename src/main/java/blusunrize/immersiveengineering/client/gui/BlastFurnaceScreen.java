/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.BlastFurnacePreheaterTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceAdvancedTileEntity;
import blusunrize.immersiveengineering.common.gui.BlastFurnaceContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class BlastFurnaceScreen extends IEContainerScreen<BlastFurnaceContainer>
{
	private static final Function<BlastFurnacePreheaterTileEntity, Boolean> PREHEATER_ACTIVE = tile -> tile.active;
	private static final ResourceLocation TEXTURE = makeTextureLocation("blast_furnace");

	public BlastFurnaceScreen(BlastFurnaceContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		if(container.tile instanceof BlastFurnaceAdvancedTileEntity)
			this.imageWidth = 210;
		clearIntArray(container.tile.getGuiInts());
	}

	@Override
	protected void renderLabels(PoseStack transform, int x, int y)
	{
		if(menu.tile instanceof BlastFurnaceAdvancedTileEntity)
		{
			String title = I18n.get(Lib.GUI+"blast_furnace.preheaters");
			int w = this.font.width(title)/2;
			this.font.draw(transform, title, 175-w, 18, 0xAEAEAE);
			this.font.draw(transform, I18n.get(Lib.GUI+"left"), 154, 28, 0xAEAEAE);
			this.font.draw(transform, I18n.get(Lib.GUI+"right"), 154, 40, 0xAEAEAE);
		}
	}

	@Override
	protected void drawBackgroundTexture(PoseStack transform)
	{
		blit(transform, leftPos, topPos, 0, 0, 176, imageHeight);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull PoseStack transform, float f, int mx, int my)
	{
		if(menu.tile instanceof BlastFurnaceAdvancedTileEntity)
		{
			BlastFurnaceAdvancedTileEntity tile = (BlastFurnaceAdvancedTileEntity)menu.tile;
			this.blit(transform, leftPos+140, topPos+11, 176, 32, 70, 46);
			if(tile.getFromPreheater(true, PREHEATER_ACTIVE, false))
				this.blit(transform, leftPos+182, topPos+27, 200, 22, 10, 10);
			if(tile.getFromPreheater(false, PREHEATER_ACTIVE, false))
				this.blit(transform, leftPos+182, topPos+39, 200, 22, 10, 10);
		}

		if(menu.state.getLastBurnTime() > 0)
		{
			int h = (int)(12*(menu.state.getBurnTime()/(float)menu.state.getLastBurnTime()));
			this.blit(transform, leftPos+56, topPos+37+12-h, 179, 1+12-h, 9, h);
		}
		if(menu.state.getMaxProcess() > 0)
		{
			int w = (int)(22*((menu.state.getMaxProcess()-menu.state.getProcess())/(float)menu.state.getMaxProcess()));
			this.blit(transform, leftPos+76, topPos+35, 177, 14, w, 16);
		}
	}
}
