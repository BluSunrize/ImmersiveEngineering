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
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import javax.annotation.Nonnull;

public class GuiSliderIE extends ForgeSlider
{
	private final FloatConsumer handler;

	public GuiSliderIE(int x, int y, int width, String name, float value, FloatConsumer handler)
	{
		super(
				x, y, width, 8,
				Component.nullToEmpty(name+" "),
				Component.nullToEmpty("%"),
				0, 100, 100*value,
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
	public void renderButton(@Nonnull PoseStack transform, int pMouseX, int pMouseY, float pPartialTick)
	{
		ClientUtils.bindTexture(GuiReactiveList.TEXTURE);
		Font fontrenderer = Minecraft.getInstance().font;
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.blendFunc(770, 771);
		this.blit(transform, x, y, 8, 128, 4, height);
		this.blit(transform, x+width-4, y, 16, 128, 4, height);
		for(int i = 0; i < width-8; i += 2)
			this.blit(transform, x+4+i, y, 13, 128, 2, height);
		this.blit(transform, this.x+2+(int)(value*(float)(this.width-2))-2, this.y, 20, 128, 4, 8);
		int color = 0xe0e0e0;
		if(!this.active)
			color = 0xa0a0a0;
		else if(this.isHovered)
			color = 0xffffa0;
		drawCenteredString(transform, fontrenderer, getMessage(), x+width/2, y-10+height/2-3, color);
	}

	@Override
	protected void applyValue()
	{
		super.applyValue();
		handler.accept((float)value);
	}
}
