/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

public class GuiButtonState<E> extends GuiButtonIE implements ITooltipWidget
{
	public E[] states;
	private final IntSupplier state;
	protected final int offsetDir;
	private final BiConsumer<List<Component>, E> tooltip;
	public int[] textOffset;

	public GuiButtonState(int x, int y, int w, int h, Component name, E[] states, IntSupplier state, ResourceLocation texture, int u,
						  int v, int offsetDir, IIEPressable<GuiButtonState<E>> handler)
	{
		this(x, y, w, h, name, states, state, texture, u, v, offsetDir, handler, (a, b) -> {
		});
	}

	public GuiButtonState(int x, int y, int w, int h, Component name, E[] states, IntSupplier state, ResourceLocation texture, int u,
						  int v, int offsetDir, IIEPressable<GuiButtonState<E>> handler, BiConsumer<List<Component>, E> tooltip)
	{
		super(x, y, w, h, name, texture, u, v, handler);
		this.states = states;
		this.state = state;
		this.offsetDir = offsetDir;
		this.tooltip = tooltip;
		textOffset = new int[]{width+1, height/2-3};
	}

	protected int getNextStateInt()
	{
		return (state.getAsInt()+1)%states.length;
	}

	public E getNextState()
	{
		return this.states[getNextStateInt()];
	}

	public E getState()
	{
		return this.states[this.state.getAsInt()];
	}

	protected int getStateAsInt()
	{
		return this.state.getAsInt();
	}

	public int[] getTextOffset(Font fontrenderer)
	{
		return this.textOffset;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(this.visible)
		{
			Font fontrenderer = mc.font;
			this.isHovered = mouseX >= this.getX()&&mouseY >= this.getY()&&mouseX < this.getX()+this.width&&mouseY < this.getY()+this.height;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);
			int u = texU+(offsetDir==0?width: offsetDir==2?-width: 0)*state.getAsInt();
			int v = texV+(offsetDir==1?height: offsetDir==3?-height: 0)*state.getAsInt();
			graphics.blit(texture, getX(), getY(), u, v, width, height);
			if(!getMessage().getString().isEmpty())
			{
				int txtCol = 0xE0E0E0;
				if(!this.active)
					txtCol = 0xA0A0A0;
				else if(this.isHovered)
					txtCol = Lib.COLOUR_I_ImmersiveOrange;
				int[] offset = getTextOffset(fontrenderer);
				graphics.drawString(fontrenderer, getMessage(), getX()+offset[0], getY()+offset[1], txtCol, false);
			}
		}
	}

	@Override
	public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
	{
		this.tooltip.accept(tooltip, getState());
	}
}
