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
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;

public class LogicUnitScreen extends IEContainerScreen<LogicUnitContainer>
{
	public LogicUnitScreen(LogicUnitContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY)
	{
		for(int i = 0; i < 10; i++)
			drawCenteredString(transform, this.font, ""+(i+1), 52+(i%5)*18, 23+(i/5)*18, DyeColor.GRAY.getColorValue());
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture("immersiveengineering:textures/gui/logic_unit.png");
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}