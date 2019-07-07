/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class GuiButtonItem extends Button
{
	public boolean state;
	ItemStack item;

	public GuiButtonItem(int buttonId, int x, int y, ItemStack stack, boolean state)
	{
		super(buttonId, x, y, 18, 18, "");
		this.state = state;
		this.item = stack;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.5F);
			this.hovered = mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(770, 771);
			this.drawTexturedModalRect(x, y, 24+(state?18: 0), 128, width, height);
			//TODO this.mouseDragged(mc, mouseX, mouseY);

			if(!item.isEmpty())
			{
				Minecraft mc = Minecraft.getInstance();
				this.zLevel = 200.0F;
				ItemRenderer itemRender = mc.getItemRenderer();
				itemRender.zLevel = 200.0F;
				FontRenderer font = item.getItem().getFontRenderer(item);
				if(font==null)
					font = mc.fontRenderer;
				itemRender.renderItemAndEffectIntoGUI(item, x+1, y+1);
				this.zLevel = 0.0F;
				itemRender.zLevel = 0.0F;

				if(!state)
				{
					RenderHelper.enableStandardItemLighting();
					GlStateManager.disableLighting();
					GlStateManager.disableDepthTest();
					ClientUtils.drawColouredRect(x+1, y+1, 16, 16, 0x77444444);
					GlStateManager.enableLighting();
					GlStateManager.enableDepthTest();
				}
				RenderHelper.disableStandardItemLighting();
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean b = super.mouseClicked(mouseX, mouseY, button);
		if(b)
			this.state = !state;
		return b;
	}
}
