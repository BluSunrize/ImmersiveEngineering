package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

public class GuiButtonManual extends GuiButton
{
	public GuiManual gui;
	public int[] colour = {0x33000000,0x33cb7f32};
	public int[] textColour = {0xffe0e0e0,0xffffffa0};
	public GuiButtonManual(GuiManual gui, int id, int x, int y, int w, int h, String text)
	{
		super(id, x, y, w, h, text);
		this.gui = gui;
	}
	public GuiButtonManual setColour(int normal, int hovered)
	{
		colour = new int[]{normal,hovered};
		return this;
	}
	public GuiButtonManual setTextColour(int normal, int hovered)
	{
		textColour = new int[]{normal,hovered};
		return this;
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my)
	{
		if (this.visible)
		{
			ManualUtils.bindTexture(gui.texture);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mx>=this.xPosition&&mx<(this.xPosition+this.width) && my>=this.yPosition&&my<(this.yPosition+this.height);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			int col = colour[hovered?1:0];
			this.drawGradientRect(xPosition,yPosition, xPosition+width,yPosition+height, col,col);
			int txtCol = textColour[hovered?1:0];
			boolean uni = gui.manual.fontRenderer.getUnicodeFlag();
			gui.manual.fontRenderer.setUnicodeFlag(true);
			int sw = gui.manual.fontRenderer.getStringWidth(displayString);
			gui.manual.fontRenderer.drawString(displayString, xPosition+width/2-sw/2, yPosition+height/2-gui.manual.fontRenderer.FONT_HEIGHT/2, txtCol);
			gui.manual.fontRenderer.setUnicodeFlag(uni);
			this.mouseDragged(mc, mx, my);
		}
	}
}