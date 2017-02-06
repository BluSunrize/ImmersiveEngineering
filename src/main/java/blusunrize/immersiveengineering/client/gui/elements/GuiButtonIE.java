package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonIE extends GuiButton
{
	protected final String texture;
	protected final int texU;
	protected final int texV;
	public GuiButtonIE(int buttonId, int x, int y, int w, int h, String name, String texture, int u, int v)
	{
		super(buttonId, x, y, w,h, name);
		this.texture = texture;
		this.texU = u;
		this.texV = v;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture(texture);
			FontRenderer fontrenderer = mc.fontRendererObj;
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(770, 771);
			this.drawTexturedModalRect(xPosition,yPosition, texU,texV, width,height);
			this.mouseDragged(mc, mouseX, mouseY);
			if(displayString!=null && !displayString.isEmpty())
			{
				int txtCol = 0xE0E0E0;
				if(!this.enabled)
					txtCol = 0xA0A0A0;
				else if(this.hovered)
					txtCol = Lib.COLOUR_I_ImmersiveOrange;
				this.drawCenteredString(fontrenderer, this.displayString, this.xPosition+this.width/2, this.yPosition+(this.height-8)/ 2, txtCol);
			}
		}
	}
}
