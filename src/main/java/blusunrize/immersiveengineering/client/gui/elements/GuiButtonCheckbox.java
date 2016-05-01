package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonCheckbox extends GuiButton
{
	public boolean state;
	public GuiButtonCheckbox(int buttonId, int x, int y, String name, boolean state)
	{
		super(buttonId, x, y, 8,8, name);
		this.state = state;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture("immersiveengineering:textures/gui/hudElements.png");
			FontRenderer fontrenderer = mc.fontRendererObj;
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(770, 771);
			this.drawTexturedModalRect(xPosition,yPosition, 0,128, width,height);
			this.mouseDragged(mc, mouseX, mouseY);
			int j = 14737632;
			if(!this.enabled)
				j = 10526880;
			else if(this.hovered)
				j = 16777120;
			this.drawString(fontrenderer, displayString, xPosition+width+1, yPosition+height/2-3, j);
			if(state)
				this.drawCenteredString(fontrenderer, "\u2714", xPosition+width/2, yPosition-2, j);
		}
	}

	@Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
		boolean b = super.mousePressed(mc, mouseX, mouseY);
		if(b)
			this.state = !state;
		return b;
    }
}
