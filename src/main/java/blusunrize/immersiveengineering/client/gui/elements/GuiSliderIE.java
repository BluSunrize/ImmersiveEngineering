/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import javax.annotation.Nonnull;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class GuiSliderIE extends ExtendedSlider
{
	private static final ResourceLocation LEFT = ieLoc("slider_horizontal/left");
	private static final ResourceLocation CENTER = ieLoc("slider_horizontal/center");
	private static final ResourceLocation RIGHT = ieLoc("slider_horizontal/right");
	private static final ResourceLocation BUTTON = ieLoc("slider_horizontal/slider");

	private final FloatConsumer handler;

	public GuiSliderIE(int x, int y, int width, String name, float min, float max, float value, FloatConsumer handler)
	{
		super(
				x, y, width, 8,
				Component.nullToEmpty(name+" "),
				Component.nullToEmpty("%"),
				100*min, 100*max, 100*value,
				1, 0, true
		);
		this.handler = handler;
	}

	public GuiSliderIE(int x, int y, int width, Component prefix, int minVal, int maxVal, int value, FloatConsumer handler)
	{
		super(x, y, width, 8, prefix, Component.empty(), minVal, maxVal, value, 1, 0, true);
		this.handler = handler;
	}

	@Override
	public void renderWidget(@Nonnull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick)
	{
		Font fontrenderer = Minecraft.getInstance().font;
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.blendFunc(770, 771);
		graphics.blitSprite(LEFT, getX(), getY(), 4, height);
		graphics.blitSprite(RIGHT, getX()+width-4, getY(), 4, height);
		for(int i = 0; i < width-8; i += 2)
			graphics.blitSprite(CENTER, getX()+4+i, getY(), 2, height);
		graphics.blitSprite(BUTTON, this.getX()+2+(int)(value*(float)(this.width-2))-2, this.getY(), 4, 8);
		int color = 0xe0e0e0;
		if(!this.active)
			color = 0xa0a0a0;
		else if(this.isHovered)
			color = 0xffffa0;
		graphics.drawCenteredString(fontrenderer, getMessage(), getX()+width/2, getY()-10+height/2-3, color);
	}

	@Override
	protected void applyValue()
	{
		super.applyValue();
		handler.accept((float)value);
	}
}
