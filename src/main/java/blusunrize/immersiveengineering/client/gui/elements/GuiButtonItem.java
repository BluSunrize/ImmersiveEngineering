/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GuiButtonItem extends Button
{
	public boolean state;
	ItemStack item;

	public GuiButtonItem(int x, int y, ItemStack stack, boolean state, OnPress handler)
	{
		super(x, y, 18, 18, Component.empty(), handler, DEFAULT_NARRATION);
		this.state = state;
		this.item = stack;
	}

	@Override
	public void render(PoseStack transform, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture(GuiReactiveList.TEXTURE);
			this.isHovered = mouseX >= this.getX()&&mouseY >= this.getY()&&mouseX < this.getX()+this.width&&mouseY < this.getY()+this.height;
			RenderSystem.defaultBlendFunc();
			this.blit(transform, getX(), getY(), 24+(state?18: 0), 128, width, height);
			//TODO this.mouseDragged(mc, mouseX, mouseY);

			if(!item.isEmpty())
			{
				Minecraft mc = Minecraft.getInstance();
				mc.getItemRenderer().renderAndDecorateItem(item, getX()+1, getY()+1);

				if(!state)
				{
					RenderSystem.disableDepthTest();
					fill(transform, getX()+1, getY()+1, getX()+17, getY()+17, 0x77444444);
					RenderSystem.enableDepthTest();
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean b = super.mouseClicked(mouseX, mouseY, button);
		if(b)
			this.state = !state;
		return b;
	}
}
