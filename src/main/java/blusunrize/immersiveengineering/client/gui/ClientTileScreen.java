/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class ClientTileScreen<T extends BlockEntity> extends Screen
{
	protected int xSize = 176;
	protected int ySize = 166;
	protected int guiLeft;
	protected int guiTop;
	protected T tileEntity;

	public ClientTileScreen(T tileEntity, Component title)
	{
		super(title);
		this.tileEntity = tileEntity;
	}

	@Override
	protected void init()
	{
		super.init();
		this.guiLeft = (this.width-this.xSize)/2;
		this.guiTop = (this.height-this.ySize)/2;
	}

	protected abstract void drawGuiContainerBackgroundLayer(PoseStack transform, int mouseX, int mouseY, float partialTick);

	protected abstract void drawGuiContainerForegroundLayer(PoseStack transform, int mouseX, int mouseY, float partialTick);

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		// Render dark background
		this.renderBackground(transform);
		// Background texture
		drawGuiContainerBackgroundLayer(transform, mx, my, partial);

		// Buttons
		super.render(transform, mx, my, partial);

		// Foreground
		drawGuiContainerForegroundLayer(transform, mx, my, partial);
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
}