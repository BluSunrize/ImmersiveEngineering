/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualInstance.ManualLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;

public class GuiButtonManualLink extends GuiButton
{
	public String localized;
	public ManualLink link;
	GuiManual gui;

	public GuiButtonManualLink(GuiManual gui, int id, int x, int y, int w, int h, ManualLink link, String localized)
	{
		super(id, x, y, w, h, "");
		this.gui = gui;
		this.link = link;
		this.localized = localized;
		if(gui.manual.improveReadability())
			this.localized = TextFormatting.BOLD+localized;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my)
	{
		return super.mousePressed(mc, mx, my);
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my, float partialTicks)
	{
		this.hovered = mx >= this.x&&my >= this.y&&mx < this.x+this.width&&my < this.y+this.height;
		if(hovered)
		{
//			FontRenderer font = gui.manual.fontRenderer;
			FontRenderer font = mc.fontRenderer;
			boolean uni = font.getUnicodeFlag();
			font.setUnicodeFlag(true);
			font.drawString(localized, x, y, gui.manual.getHighlightColour());
			font.setUnicodeFlag(false);
			gui.drawHoveringText(Collections.singletonList(gui.manual.formatLink(link)), mx+8, my+4, font);
			font.setUnicodeFlag(uni);
			GlStateManager.enableBlend();
		}

	}
}