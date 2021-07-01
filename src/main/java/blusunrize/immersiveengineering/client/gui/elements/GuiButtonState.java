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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.function.BiConsumer;

public class GuiButtonState<E> extends GuiButtonIE implements ITooltipWidget
{
	public E[] states;
	private int state;
	protected final int offsetDir;
	private final BiConsumer<List<ITextComponent>, E> tooltip;
	public int[] textOffset = {0, 0};

	public GuiButtonState(int x, int y, int w, int h, ITextComponent name, E[] states, int initialState, ResourceLocation texture, int u,
						  int v, int offsetDir, IIEPressable<GuiButtonState<E>> handler)
	{
		this(x, y, w, h, name, states, initialState, texture, u, v, offsetDir, handler, (a, b) -> {});
	}

	public GuiButtonState(int x, int y, int w, int h, ITextComponent name, E[] states, int initialState, ResourceLocation texture, int u,
						  int v, int offsetDir, IIEPressable<GuiButtonState<E>> handler, BiConsumer<List<ITextComponent>, E> tooltip)
	{
		super(x, y, w, h, name, texture, u, v, handler);
		this.states = states;
		this.state = initialState;
		this.offsetDir = offsetDir;
		this.tooltip = tooltip;
		textOffset = new int[]{width+1, height/2-3};
	}

	protected int getNextStateInt()
	{
		return (state+1)%states.length;
	}

	public E getNextState()
	{
		return this.states[getNextStateInt()];
	}

	public void setStateByInt(int state)
	{
		this.state = state;
	}

	public E getState()
	{
		return this.states[this.state];
	}

	protected int getStateAsInt()
	{
		return this.state;
	}

	public int[] getTextOffset(FontRenderer fontrenderer)
	{
		return this.textOffset;
	}

	@Override
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(this.visible)
		{
			ClientUtils.bindTexture(texture);
			FontRenderer fontrenderer = mc.fontRenderer;
			this.isHovered = mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);
			int u = texU+(offsetDir==0?width: offsetDir==2?-width: 0)*state;
			int v = texV+(offsetDir==1?height: offsetDir==3?-height: 0)*state;
			this.blit(transform, x, y, u, v, width, height);
			if(!getMessage().getString().isEmpty())
			{
				int txtCol = 0xE0E0E0;
				if(!this.active)
					txtCol = 0xA0A0A0;
				else if(this.isHovered)
					txtCol = Lib.COLOUR_I_ImmersiveOrange;
				int[] offset = getTextOffset(fontrenderer);
				this.drawString(transform, fontrenderer, getMessage(), x+offset[0], y+offset[1], txtCol);
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int key)
	{
		boolean b = super.mouseClicked(mouseX, mouseY, key);
		if(b)
			this.state = getNextStateInt();
		return b;
	}

	@Override
	public void gatherTooltip(int mouseX, int mouseY, List<ITextComponent> tooltip)
	{
		this.tooltip.accept(tooltip, getState());
	}
}
