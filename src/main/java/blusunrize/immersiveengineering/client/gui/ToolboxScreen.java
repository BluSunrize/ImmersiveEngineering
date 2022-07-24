/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.ToolboxBlockContainer;
import blusunrize.immersiveengineering.common.gui.ToolboxContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.function.Consumer;

public class ToolboxScreen<C extends AbstractContainerMenu> extends IEContainerScreen<C>
{
	public static ScreenConstructor<ToolboxBlockContainer, ToolboxScreen<ToolboxBlockContainer>> CONSTRUCTOR_BLOCK = ToolboxScreen::new;
	public static ScreenConstructor<ToolboxContainer, ToolboxScreen<ToolboxContainer>> CONSTRUCTOR_ITEM = ToolboxScreen::new;

	private ToolboxScreen(C container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("toolbox"));
		this.imageHeight = 238;
		this.inventoryLabelY = this.imageHeight-91;
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		Slot slot = getSlotUnderMouse();
		if(slot instanceof IESlot.ContainerCallback&&!slot.hasItem())
		{
			int iSlot = slot.getSlotIndex();
			addGray.accept(new TranslatableComponent(Lib.DESC_INFO+"toolbox."+(
					iSlot < 3?"food": iSlot < 10?"tool": iSlot < 16?"wire": "any"
			)));
		}
	}

	@Override
	protected void drawBackgroundTexture(PoseStack transform)
	{
		blit(transform, leftPos, topPos-17, 0, 0, 176, imageHeight+17);
	}
}