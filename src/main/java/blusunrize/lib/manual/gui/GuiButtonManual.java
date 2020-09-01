/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA;

public class GuiButtonManual extends Button
{
	public ManualScreen gui;
	public int[] colour = {0x33000000, 0x33cb7f32};
	public int[] textColour = {0xffe0e0e0, 0xffffffa0};

	public GuiButtonManual(ManualScreen gui, int x, int y, int w, int h, ITextComponent text, IPressable handler)
	{
		super(x, y, w, h, text, handler);
		this.gui = gui;
	}

	public GuiButtonManual setColour(int normal, int hovered)
	{
		colour = new int[]{normal, hovered};
		return this;
	}

	public GuiButtonManual setTextColour(int normal, int hovered)
	{
		textColour = new int[]{normal, hovered};
		return this;
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partialTicks)
	{
		if(this.visible)
		{
			ManualUtils.bindTexture(gui.texture);
			RenderSystem.color3f(1.0F, 1.0F, 1.0F);
			this.isHovered = mx >= this.x&&mx < (this.x+this.width)&&my >= this.y&&my < (this.y+this.height);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO);

			int col = colour[isHovered?1: 0];
			fill(transform, x, y, x+width, y+height, col);
			int txtCol = textColour[isHovered?1: 0];
			int sw = gui.manual.fontRenderer().getStringWidth(getMessage().getString());
			gui.manual.fontRenderer().drawString(transform, getMessage().getString(), x+width/2-sw/2, y+height/2-gui.manual.fontRenderer().FONT_HEIGHT/2, txtCol);
			//TODO this.mouseDragged(mc, mx, my);
		}
	}
}