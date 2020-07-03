/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class GuiButtonItem extends Button
{
	public boolean state;
	ItemStack item;

	public GuiButtonItem(int x, int y, ItemStack stack, boolean state, IPressable handler)
	{
		super(x, y, 18, 18, StringTextComponent.field_240750_d_, handler);
		this.state = state;
		this.item = stack;
	}

	@Override
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.5F);
			this.isHovered = mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);
			this.blit(transform, x, y, 24+(state?18: 0), 128, width, height);
			//TODO this.mouseDragged(mc, mouseX, mouseY);

			if(!item.isEmpty())
			{
				Minecraft mc = Minecraft.getInstance();
				ItemRenderer itemRender = mc.getItemRenderer();
				FontRenderer font = item.getItem().getFontRenderer(item);
				if(font==null)
					font = mc.fontRenderer;
				itemRender.renderItemAndEffectIntoGUI(item, x+1, y+1);

				if(!state)
				{
					RenderHelper.enableStandardItemLighting();
					RenderSystem.disableLighting();
					RenderSystem.disableDepthTest();
					ClientUtils.drawColouredRect(x+1, y+1, 16, 16, 0x77444444);
					RenderSystem.enableLighting();
					RenderSystem.enableDepthTest();
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
