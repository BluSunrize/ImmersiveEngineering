/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class GuiButtonItem extends Button
{
	private static final ResourceLocation FALSE_TEXTURE = ieLoc("hud/square_out");
	private static final ResourceLocation TRUE_TEXTURE = ieLoc("hud/square_in");
	public boolean state;
	private final ItemStack item;

	public GuiButtonItem(int x, int y, ItemStack stack, boolean state, OnPress handler)
	{
		super(x, y, 18, 18, Component.empty(), handler, DEFAULT_NARRATION);
		this.state = state;
		this.item = stack;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			this.isHovered = mouseX >= this.getX()&&mouseY >= this.getY()&&mouseX < this.getX()+this.width&&mouseY < this.getY()+this.height;
			RenderSystem.defaultBlendFunc();
			graphics.blitSprite(
					state?TRUE_TEXTURE: FALSE_TEXTURE, getX(), getY(), width, height
			);
			//TODO this.mouseDragged(mc, mouseX, mouseY);

			if(!item.isEmpty())
			{
				Minecraft mc = Minecraft.getInstance();
				graphics.renderItem(item, getX()+1, getY()+1);

				if(!state)
				{
					RenderSystem.disableDepthTest();
					graphics.fill(getX()+1, getY()+1, getX()+17, getY()+17, 0x77444444);
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
