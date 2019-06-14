/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiSliderIE extends GuiSlider
{
	public GuiSliderIE(int buttonId, int x, int y, int width, String name, float value)
	{
		super(buttonId, x, y, width, 8, name+" ", "%", 0, 100, 100*value, false, true);
	}

	@Override
	public void render(int mouseX, int mouseY, float partial)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
			FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
			GlStateManager.color3f(1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(770, 771);
			this.drawTexturedModalRect(x, y, 8, 128, 4, height);
			this.drawTexturedModalRect(x+width-4, y, 16, 128, 4, height);
			for(int i = 0; i < width-8; i += 2)
				this.drawTexturedModalRect(x+4+i, y, 13, 128, 2, height);
			//TODO this.mouseDragged(mc, mouseX, mouseY);
			int color = 0xe0e0e0;
			if(!this.enabled)
				color = 0xa0a0a0;
			else if(this.hovered)
				color = 0xffffa0;
			this.drawCenteredString(fontrenderer, displayString, x+width/2, y-10+height/2-3, color);
		}
	}

	@Override
	public boolean mouseDragged(double cx, double cy, int button, double dx, double dy)
	{
		if(this.visible)
		{
			if(this.dragging)
			{
				this.sliderValue = (cx-(this.x+4))/(float)(this.width-8);
				updateSlider();
			}
			GlStateManager.color3f(1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.x+2+(int)(this.sliderValue*(float)(this.width-2))-2, this.y, 20, 128, 4, 8);
			return true;
		}
		else
			return false;
	}
}
