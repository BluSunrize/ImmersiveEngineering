/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public abstract class ClientTileScreen<T extends TileEntity> extends Screen
{
	protected int xSize = 176;
	protected int ySize = 166;
	protected int guiLeft;
	protected int guiTop;
	protected T tileEntity;

	public ClientTileScreen(T tileEntity, ITextComponent title)
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

	protected abstract void drawGuiContainerBackgroundLayer(int mouseX, int mouseY, float partialTick);

	protected abstract void drawGuiContainerForegroundLayer(int mouseX, int mouseY, float partialTick);

	@Override
	public void render(int mx, int my, float partial)
	{
		// Render dark background
		this.renderBackground();
		// Background texture
		drawGuiContainerBackgroundLayer(mx, my, partial);

		// Buttons
		super.render(mx, my, partial);

		// Foreground
		drawGuiContainerForegroundLayer(mx, my, partial);
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
}