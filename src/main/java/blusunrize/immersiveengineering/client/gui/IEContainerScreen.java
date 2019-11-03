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
import net.minecraft.util.text.ITextComponent;

/**
 * @author BluSunrize - 05.07.2017
 */
public abstract class IEContainerScreen<C extends Container> extends ContainerScreen<C>
{
	public IEContainerScreen(C inventorySlotsIn, PlayerInventory inv, ITextComponent title)
	{
		super(inventorySlotsIn, inv, title);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	public void fullInit()
	{
		super.init(minecraft, width, height);
	}
}
