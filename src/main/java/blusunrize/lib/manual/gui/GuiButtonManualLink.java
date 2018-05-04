/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualInstance.ManualLink;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

public class GuiButtonManualLink extends GuiButton
{
	public String localized;
	public ManualLink link;
	GuiManual gui;
	public List<GuiButtonManualLink> otherParts = ImmutableList.of();
	public GuiButtonManualLink(GuiManual gui, int id, int x, int y, int w, int h, ManualLink link, String localized)
	{
		super(id, x,y, w,h, "");
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
		this.hovered = mx >= this.x && my >= this.y && mx < this.x + this.width && my < this.y + this.height;
		if(hovered)
		{
			drawHovered(mc, true, mx, my);
			for (GuiButtonManualLink btn:otherParts)
				if (btn!=this)
					btn.drawHovered(mc, false, mx, my);
			GlStateManager.enableBlend();
		}
	}

	private void drawHovered(Minecraft mc, boolean mouse, int mx, int my)
	{
		FontRenderer font = mc.fontRenderer;
		boolean uni = font.getUnicodeFlag();
		font.setUnicodeFlag(true);
		font.drawString(localized, x, y, gui.manual.getHighlightColour());
		font.setUnicodeFlag(false);
		gui.drawHoveringText(Collections.singletonList(gui.manual.formatLink(link)), mx+8,my+4, font);
		font.setUnicodeFlag(uni);
		GlStateManager.disableLighting();
	}
}