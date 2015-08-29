package blusunrize.lib.manual.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;

import blusunrize.lib.manual.ManualUtils;

public class GuiButtonManualNavigation extends GuiButton
{
	public int type;
	public GuiManual gui;
	public GuiButtonManualNavigation(GuiManual gui, int id, int x, int y, int w, int h, int type)
	{
		super(id, x, y, type>=4?10:Math.min(type<2?16:10, w), type>=4?10:Math.min(type<2?10:16, h), "");
		this.gui = gui;
		this.type = type;
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my)
	{
		if (this.visible)
		{
			ManualUtils.bindTexture(gui.texture);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.field_146123_n = mx>=this.xPosition&&mx<(this.xPosition+this.width) && my>=this.yPosition&&my<(this.yPosition+this.height);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			int u = type==5?46: type==4||type==6?36: (type<2?0:type<3?16:26)+  (type>1?(10-width): type==1?(16-width): 0);
			int v = 216+( type==0?0: type==1?10: type==2?(16-height): type==3?0: type==4||type==5?10:  0 );
			if(field_146123_n)
				v+=20;
			this.drawTexturedModalRect(this.xPosition, this.yPosition, u,v, width,height);
			this.mouseDragged(mc, mx, my);
		}
	}
}