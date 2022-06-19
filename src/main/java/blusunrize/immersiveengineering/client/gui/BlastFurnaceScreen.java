/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.stone.FurnaceLikeBlockEntity.StateView;
import blusunrize.immersiveengineering.common.gui.BlastFurnaceMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

import javax.annotation.Nonnull;

public class BlastFurnaceScreen extends IEContainerScreen<BlastFurnaceMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("blast_furnace");

	public BlastFurnaceScreen(BlastFurnaceMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
	}

	public static void drawFlameAndArrow(
			Screen screen, ContainerData state, PoseStack transform, int leftPos, int topPos, int arrowXOffset
	)
	{
		if(StateView.getLastBurnTime(state) > 0)
		{
			int h = (int)(12*(StateView.getBurnTime(state)/(float)StateView.getLastBurnTime(state)));
			screen.blit(transform, leftPos+56, topPos+37+12-h, 179, 1+12-h, 9, h);
		}
		if(StateView.getMaxProcess(state) > 0)
		{
			int w = (int)(22*(1-StateView.getProcess(state)/(float)StateView.getMaxProcess(state)));
			screen.blit(transform, leftPos+arrowXOffset, topPos+35, 177, 14, w, 16);
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
		drawFlameAndArrow(this, menu.state, transform, leftPos, topPos, 76);
	}

	public static class Advanced extends BlastFurnaceScreen
	{
		public Advanced(BlastFurnaceMenu container, Inventory inventoryPlayer, Component title)
		{
			super(container, inventoryPlayer, title);
			this.imageWidth = 210;
		}

		@Override
		protected void drawContainerBackgroundPre(@Nonnull PoseStack transform, float f, int mx, int my)
		{
			this.blit(transform, leftPos+140, topPos+11, 176, 32, 70, 46);
			if(menu.leftHeater.get())
				this.blit(transform, leftPos+182, topPos+27, 200, 22, 10, 10);
			if(menu.rightHeater.get())
				this.blit(transform, leftPos+182, topPos+39, 200, 22, 10, 10);
			super.drawContainerBackgroundPre(transform, f, mx, my);
		}

		@Override
		protected void renderLabels(PoseStack transform, int x, int y)
		{
			String title = I18n.get(Lib.GUI+"blast_furnace.preheaters");
			int w = this.font.width(title)/2;
			this.font.draw(transform, title, 175-w, 18, 0xAEAEAE);
			this.font.draw(transform, I18n.get(Lib.GUI+"left"), 154, 28, 0xAEAEAE);
			this.font.draw(transform, I18n.get(Lib.GUI+"right"), 154, 40, 0xAEAEAE);
		}
	}
}
