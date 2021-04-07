/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.LogicUnitContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class LogicUnitScreen extends IEContainerScreen<LogicUnitContainer>
{
	public LogicUnitScreen(LogicUnitContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.ySize = 168;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY)
	{
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture("immersiveengineering:textures/gui/crate.png");
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}