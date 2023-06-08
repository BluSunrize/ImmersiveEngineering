/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class ClientBlockEntityScreen<T extends BlockEntity> extends Screen
{
	protected int xSize = 176;
	protected int ySize = 166;
	protected int guiLeft;
	protected int guiTop;
	protected T blockEntity;

	public ClientBlockEntityScreen(T blockEntity, Component title)
	{
		super(title);
		this.blockEntity = blockEntity;
	}

	@Override
	protected void init()
	{
		super.init();
		this.guiLeft = (this.width-this.xSize)/2;
		this.guiTop = (this.height-this.ySize)/2;
	}

	protected abstract void drawGuiContainerBackgroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

	protected abstract void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

	@Override
	public void render(GuiGraphics graphics, int mx, int my, float partial)
	{
		// Render dark background
		this.renderBackground(graphics);
		// Background texture
		drawGuiContainerBackgroundLayer(graphics, mx, my, partial);

		// Buttons
		super.render(graphics, mx, my, partial);

		// Foreground
		drawGuiContainerForegroundLayer(graphics, mx, my, partial);
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
}