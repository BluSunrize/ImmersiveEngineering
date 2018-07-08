/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

public class GuiClickableList extends GuiButton
{
	String[] entries;
	float textScale;
	int offset;
	int maxOffset;
	int perPage;
	int translationType;
	GuiManual gui;

	private long prevWheelNano = 0;

	public GuiClickableList(GuiManual gui, int id, int x, int y, int w, int h, float textScale, int translationType, String... entries)
	{
		super(id, x, y, w, h, "");
		this.gui = gui;
		this.textScale = textScale;
		this.entries = entries;
		this.translationType = translationType;

		perPage = (h-8)/getFontHeight();
		if(perPage < entries.length)
			maxOffset = entries.length-perPage;
	}

	int getFontHeight()
	{
		return (int)(gui.manual.fontRenderer.FONT_HEIGHT*textScale);
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my, float partialTicks)
	{
		FontRenderer fr = gui.manual.fontRenderer;
		boolean uni = fr.getUnicodeFlag();
		fr.setUnicodeFlag(true);

		int mmY = my-this.y;
		GlStateManager.pushMatrix();
		GlStateManager.scale(textScale, textScale, textScale);
		GlStateManager.translate(x/textScale, y/textScale, 0);
		GlStateManager.color(1, 1, 1);
		this.hovered = mx >= x&&mx < x+width&&my >= y&&my < y+height;
		for(int i = 0; i < Math.min(perPage, entries.length); i++)
		{
			int col = gui.manual.getTextColour();
			if(hovered&&mmY >= i*getFontHeight()&&mmY < (i+1)*getFontHeight())
				col = gui.manual.getHighlightColour();
			if(i!=0)
				GlStateManager.translate(0, getFontHeight(), 0);
			int j = offset+i;
			if(j > entries.length-1)
				j = entries.length-1;
			String s = translationType==-1?entries[j]: translationType==0?gui.manual.formatCategoryName(entries[j]): gui.manual.formatEntryName(entries[j]);
			fr.drawString(s, 0, 0, col, false);
		}
		GlStateManager.scale(1/textScale, 1/textScale, 1/textScale);
		GlStateManager.popMatrix();
		if(maxOffset > 0)
		{
			int h1 = offset*getFontHeight();
			int h2 = height-8-maxOffset*getFontHeight();
			this.drawGradientRect(x+width, y+h1, x+width+8, y+h1+h2, 0x0a000000, 0x0a000000);
			this.drawGradientRect(x+width+1, y+h1, x+width+6, y+h1+h2, 0x28000000, 0x28000000);
			if(offset > 0)
				this.drawGradientRect(x+width, y, x+width+8, y+h1, 0x0a000000, 0x0a000000);
			if(offset < maxOffset)
			{
				int h3 = (maxOffset-offset)*getFontHeight();
				this.drawGradientRect(x+width, y+height-8-h3, x+width+8, y+height-8, 0x0a000000, 0x11000000);
			}
		}

		fr.setUnicodeFlag(uni);

		//Handle DWheel
		int mouseWheel = Mouse.getEventDWheel();
		if(mouseWheel!=0&&maxOffset > 0&&Mouse.getEventNanoseconds()!=prevWheelNano)
		{
			prevWheelNano = Mouse.getEventNanoseconds();
			if(mouseWheel < 0&&offset < maxOffset)
				offset++;
			if(mouseWheel > 0&&offset > 0)
				offset--;
		}
	}

	public int selectedOption = -1;

	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my)
	{
		boolean b = super.mousePressed(mc, mx, my);
		selectedOption = -1;
		if(b)
		{
			int mmY = my-this.y;
			for(int i = 0; i < Math.min(perPage, entries.length); i++)
				if(mmY >= i*getFontHeight()&&mmY < (i+1)*getFontHeight())
					selectedOption = offset+i;
		}
		return selectedOption!=-1;
	}
}