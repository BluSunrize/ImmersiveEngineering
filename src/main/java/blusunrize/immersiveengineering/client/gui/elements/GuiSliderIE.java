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
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.gui.widget.Slider;

public class GuiSliderIE extends Slider
{
	public GuiSliderIE(int x, int y, int width, String name, float value, OnPress handler)
	{
		super(x, y, width, 8, Component.nullToEmpty(name+" "), Component.nullToEmpty("%"), 0, 100, 100*value, false, true, handler);
	}

	public GuiSliderIE(int x, int y, int width, Component prefix, int minVal, int maxVal, int value, OnPress handler)
	{
		super(x, y, width, 8, prefix, TextComponent.EMPTY, minVal, maxVal, value, false, true, handler);
	}

	@Override
	public void render(PoseStack transform, int mouseX, int mouseY, float partial)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture(GuiReactiveList.TEXTURE);
			Font fontrenderer = Minecraft.getInstance().font;
			isHovered = mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);
			this.blit(transform, x, y, 8, 128, 4, height);
			this.blit(transform, x+width-4, y, 16, 128, 4, height);
			for(int i = 0; i < width-8; i += 2)
				this.blit(transform, x+4+i, y, 13, 128, 2, height);
			this.blit(transform, this.x+2+(int)(this.sliderValue*(float)(this.width-2))-2, this.y, 20, 128, 4, 8);
			//TODO this.mouseDragged(mc, mouseX, mouseY);
			int color = 0xe0e0e0;
			if(!this.active)
				color = 0xa0a0a0;
			else if(this.isHovered)
				color = 0xffffa0;
			drawCenteredString(transform, fontrenderer, getMessage(), x+width/2, y-10+height/2-3, color);
		}
	}

	@Override
	public void updateSlider()
	{
		super.updateSlider();
		onPress.onPress(this);
	}
}
