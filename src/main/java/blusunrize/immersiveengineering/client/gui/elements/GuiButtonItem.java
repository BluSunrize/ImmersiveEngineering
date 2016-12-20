package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

public class GuiButtonItem extends GuiButton
{
	public boolean state;
	ItemStack item;
	public GuiButtonItem(int buttonId, int x, int y, ItemStack stack, boolean state)
	{
		super(buttonId, x, y, 18,18, "");
		this.state = state;
		this.item = stack;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture("immersiveengineering:textures/gui/hudElements.png");
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(770, 771);
			this.drawTexturedModalRect(xPosition,yPosition, 24+(state?18:0),128, width,height);
			this.mouseDragged(mc, mouseX, mouseY);

			if(item!=null)
			{
				this.zLevel = 200.0F;
				RenderItem itemRender = mc.getRenderItem();
				itemRender.zLevel = 200.0F;
				FontRenderer font = item.getItem().getFontRenderer(item);
				if(font==null)
					font = mc.fontRendererObj;
				itemRender.renderItemAndEffectIntoGUI(item, xPosition+1, yPosition+1);
				this.zLevel = 0.0F;
				itemRender.zLevel = 0.0F;

				if(!state)
				{
					RenderHelper.enableStandardItemLighting();
					GlStateManager.disableLighting();
					GlStateManager.disableDepth();
					ClientUtils.drawColouredRect(xPosition+1, yPosition+1, 16, 16, 0x77444444);
					GlStateManager.enableLighting();
					GlStateManager.enableDepth();
				}
				RenderHelper.disableStandardItemLighting();
			}
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
