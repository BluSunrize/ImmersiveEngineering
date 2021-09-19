/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;

/**
 * @author BluSunrize - 05.07.2017
 */
public abstract class IEContainerScreen<C extends AbstractContainerMenu> extends AbstractContainerScreen<C>
{
	public IEContainerScreen(C inventorySlotsIn, Inventory inv, Component title)
	{
		super(inventorySlotsIn, inv, title);
	}

	@Override
	public void render(PoseStack transform, int mouseX, int mouseY, float partialTicks)
	{
		this.inventoryLabelY = this.imageHeight-94;
		this.renderBackground(transform);
		super.render(transform, mouseX, mouseY, partialTicks);
		this.renderTooltip(transform, mouseX, mouseY);
	}

	protected boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h)
	{
		return mouseX >= leftPos+x&&mouseY >= topPos+y
				&&mouseX < leftPos+x+w&&mouseY < topPos+y+h;
	}

	protected void clearIntArray(ContainerData ints)
	{
		// Clear GUI ints, the sync code assumes that 0 is the initial state
		for(int i = 0; i < ints.getCount(); ++i)
			ints.set(i, 0);
	}

	public void fullInit()
	{
		super.init(minecraft, width, height);
	}

	public static ResourceLocation makeTextureLocation(String name)
	{
		return ImmersiveEngineering.rl("textures/gui/"+name+".png");
	}
}
