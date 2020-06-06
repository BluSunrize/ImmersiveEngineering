/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;

public class GuiButtonIE extends Button
{
	protected final String texture;
	protected final int texU;
	protected final int texV;

	public GuiButtonIE(int x, int y, int w, int h, String name, String texture, int u, int v, IIEPressable handler)
	{
		super(x, y, w, h, name, handler);
		this.texture = texture;
		this.texU = u;
		this.texV = v;
	}

	int[] hoverOffset;

	public GuiButtonIE setHoverOffset(int x, int y)
	{
		this.hoverOffset = new int[]{x, y};
		return this;
	}

	private boolean isPressable(double mouseX, double mouseY)
	{
		return this.active&&this.visible&&mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			Minecraft mc = Minecraft.getInstance();
			ClientUtils.bindTexture(texture);
			FontRenderer fontrenderer = mc.fontRenderer;
			GlStateManager.color3f(1.0F, 1.0F, 1.0F);
			this.isHovered = isPressable(mouseX, mouseY);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
			GlStateManager.blendFunc(770, 771);
			if(hoverOffset!=null&&this.isHovered)
				this.blit(x, y, texU+hoverOffset[0], texV+hoverOffset[1], width, height);
			else
				this.blit(x, y, texU, texV, width, height);
			if(!getMessage().isEmpty())
			{
				int txtCol = 0xE0E0E0;
				if(!this.active)
					txtCol = 0xA0A0A0;
				else if(this.isHovered)
					txtCol = Lib.COLOUR_I_ImmersiveOrange;
				this.drawCenteredString(fontrenderer, getMessage(), this.x+this.width/2, this.y+(this.height-8)/2, txtCol);
			}
		}
	}

	@Override
	public void onPress()
	{
		this.onPress.onPress(this);
	}

	public interface IIEPressable<B extends GuiButtonIE> extends IPressable
	{
		void onIEPress(B var1);

		@Override
		default void onPress(Button var1)
		{
			Preconditions.checkArgument(var1 instanceof GuiButtonIE);
			this.onIEPress((B)var1);
		}
	}
}
