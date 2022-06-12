/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.gui.ToolboxBlockContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Consumer;

public class ToolboxBlockScreen extends IEContainerScreen<ToolboxBlockContainer>
{
	public ToolboxBlockScreen(ToolboxBlockContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("toolbox"));
		this.imageHeight = 238;
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		ToolboxScreen.gatherEmptySlotTooltip(menu, menu.slotCount, leftPos, topPos, mouseX, mouseY, addGray);
	}

	@Override
	protected void drawBackgroundTexture(PoseStack transform)
	{
		blit(transform, leftPos, topPos - 17, 0, 0, 176, imageHeight + 17);
	}
}