/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA;

public class GuiButtonManualNavigation extends Button
{
	public int type;
	public ManualScreen gui;

	public GuiButtonManualNavigation(ManualScreen gui, int x, int y, int w, int h, int type, OnPress handler)
	{
		super(
                x, y,
                type >= 4?10: Math.min(type < 2?16: 10, w), type >= 4?10: Math.min(type < 2?10: 16, h),
                Component.empty(), handler, DEFAULT_NARRATION
                );
		this.gui = gui;
		this.type = type;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mx, int my, float partial)
	{
		isHovered = mx >= this.getX()&&mx < (this.getX()+this.width)&&my >= this.getY()&&my < (this.getY()+this.height);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO);
		int u = type==5?46: type==4||type==6?36: (type < 2?0: type < 3?16: 26)+(type > 1?(10-width): type==1?(16-width): 0);
		int v = 216+(type==0?0: type==1?10: type==2?(16-height): type==3?0: type==4||type==5?10: 0);
		if(isHovered)
			v += 20;
		graphics.blit(gui.texture, this.getX(), this.getY(), u, v, width, height);
	}
}