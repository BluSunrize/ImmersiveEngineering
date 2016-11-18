package blusunrize.lib.manual.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class GuiButtonManualLink extends GuiButton
{
	public String key;
	public String localized;
	public int pageLinked;
	GuiManual gui;
	public GuiButtonManualLink(GuiManual gui, int id, int x, int y, int w, int h, String key, String localized, int pageLinked)
	{
		super(id, x,y, w,h, "");
		this.gui = gui;
		this.key = key;
		this.localized = localized;
		if(gui.manual.improveReadability())
			this.localized = TextFormatting.BOLD+localized;
		this.pageLinked = pageLinked;
	}

	@Override
    public boolean mousePressed(Minecraft mc, int mx, int my)
    {
        return super.mousePressed(mc, mx, my);
    }

	@Override
	public void drawButton(Minecraft mc, int mx, int my)
	{
		this.hovered = mx >= this.xPosition && my >= this.yPosition && mx < this.xPosition + this.width && my < this.yPosition + this.height;
		if(hovered)
		{
			FontRenderer font = gui.manual.fontRenderer;
			boolean uni = font.getUnicodeFlag();
			font.setUnicodeFlag(true);
			font.drawString(localized, xPosition, yPosition, gui.manual.getHighlightColour());
			font.setUnicodeFlag(false);
			gui.drawHoveringText(Arrays.asList(gui.manual.formatEntryName(key)+", "+(pageLinked+1)), mx+8,my+4, font);
			font.setUnicodeFlag(uni);
			GlStateManager.enableBlend();
		}
		
	}
}