/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IIntArray;
import net.minecraft.util.ResourceLocation;
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
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
	{
		this.playerInventoryTitleY = this.ySize-94;
		this.renderBackground(transform);
		super.render(transform, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(transform, mouseX, mouseY);
	}

	protected boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h)
	{
		return mouseX >= guiLeft+x&&mouseY >= guiTop+y
				&&mouseX < guiLeft+x+w&&mouseY < guiTop+y+h;
	}

	protected void clearIntArray(IIntArray ints)
	{
		// Clear GUI ints, the sync code assumes that 0 is the initial state
		for(int i = 0; i < ints.size(); ++i)
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
