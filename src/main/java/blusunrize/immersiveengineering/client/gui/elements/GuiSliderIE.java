package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiSliderIE extends GuiSlider
{
	public GuiSliderIE(int buttonId, int x, int y, int width, String name, float value)
	{
		super(buttonId, x, y, width,8, name+" ","%", 0,100, 100*value, false,true);
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
			this.drawTexturedModalRect(xPosition,yPosition, 8,128, 4,height);
			this.drawTexturedModalRect(xPosition+width-4,yPosition, 16,128, 4,height);
			for(int i=0;i<width-8; i+=2)
				this.drawTexturedModalRect(xPosition+4+i,yPosition, 13,128, 2,height);
			this.mouseDragged(mc, mouseX, mouseY);
			int j = 14737632;
			if(!this.enabled)
				j = 10526880;
			else if(this.hovered)
				j = 16777120;
			this.drawCenteredString(fontrenderer, displayString, xPosition+width/2, yPosition-10+height/2-3, j);
		}
	}
	@Override
	protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3)
	{
		if (this.visible)
		{
			if (this.dragging)
			{
				this.sliderValue = (par2 - (this.xPosition + 4)) / (float)(this.width - 8);
				updateSlider();
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.xPosition+2+(int)(this.sliderValue*(float)(this.width-2))-2, this.yPosition, 20,128, 4,8);
		}
	}
}
