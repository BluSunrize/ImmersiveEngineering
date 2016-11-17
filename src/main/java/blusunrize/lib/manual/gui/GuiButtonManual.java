package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

public class GuiButtonManual extends GuiButton
{
	public int colour;
	public GuiManual gui;
	public int[] textColour = {0xe0e0e0,0xffffa0};
	public GuiButtonManual(GuiManual gui, int id, int x, int y, int w, int h, int colour, String text)
	{
		super(id, x, y, w, h, text);
		this.gui = gui;
		this.colour = colour;
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
			this.drawGradientRect(xPosition,yPosition, xPosition+width,yPosition+height, colour,colour);
			int txtCol = textColour[hovered?1:0];
			this.drawCenteredString(gui.manual.fontRenderer, displayString, xPosition+width/2, yPosition+height/2, txtCol);
			this.mouseDragged(mc, mx, my);
		}
	}
}