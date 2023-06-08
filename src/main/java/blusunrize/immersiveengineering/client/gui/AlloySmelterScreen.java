/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.gui.AlloySmelterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

import javax.annotation.Nonnull;

public class AlloySmelterScreen extends IEContainerScreen<AlloySmelterMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("alloy_smelter");

	public AlloySmelterScreen(AlloySmelterMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		ContainerData state = menu.getStateView();
		BlastFurnaceScreen.drawFlameAndArrow(state, graphics, leftPos, topPos, 84);
	}
}
