/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.StringTextComponent;

/**
 * @author BluSunrize - 05.07.2017
 */
public abstract class GuiIEContainerBase extends ContainerScreen
{
	public GuiIEContainerBase(Container inventorySlotsIn, PlayerInventory inv)
	{
		super(inventorySlotsIn, inv, new StringTextComponent(""));
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void init()
	{
		super.init();
	}
}
