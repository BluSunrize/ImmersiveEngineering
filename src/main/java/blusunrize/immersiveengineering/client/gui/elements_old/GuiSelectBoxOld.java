/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements_old;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.MachineInterfaceScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class GuiSelectBoxOld<E> extends GuiButtonStateOld<E>
{
	private static final int WIDTH_LEFT = 8;
	private static final int WIDTH_MIDDLE = 4;
	private static final int WIDTH_RIGHT = 12;
	private static final int WIDTH_STATIC = WIDTH_LEFT+WIDTH_RIGHT;
	private static final int WIDTH_BUTTON = 10;
	private static final int HEIGHT_BASE = 16;
	private static final int OPEN_OFFSET = 8;
	private static final int TEXT_INDENT = 4;

	private final Supplier<E[]> optionGetter;
	private final Function<E, Component> messageGetter;
	private final int minWidth;
	private boolean opened = false;
	private int openedHeight;

	private int selectedState = -1;

	public GuiSelectBoxOld(
			int x, int y, int minWidth, Supplier<E[]> optionGetter, IntSupplier selectedOption,
			Function<E, Component> messageGetter, IIEPressable<GuiSelectBoxOld<E>> handler
	)
	{
		super(x, y, 64, 16, Component.empty(), optionGetter.get(), selectedOption,
				MachineInterfaceScreen.TEXTURE, 166, 18, -1, btn -> handler.onIEPress((GuiSelectBoxOld<E>)btn));
		this.optionGetter = optionGetter;
		this.messageGetter = messageGetter;
		this.minWidth = minWidth;
		this.recalculateOptionsAndSize();
	}

	public void recalculateOptionsAndSize()
	{
		this.states = optionGetter.get();
		// set width based on widest text
		this.width = Math.max(
				WIDTH_STATIC+minWidth,
				WIDTH_STATIC+Arrays.stream(this.states).mapToInt(value -> mc().font.width(messageGetter.apply(value))).max().orElse(this.width)
		);
		this.openedHeight = mc().font.lineHeight*this.states.length;

	}

	public int getClickedState()
	{
		return selectedState!=-1?selectedState: this.getStateAsInt();
	}

	@Override
	public Component getMessage()
	{
		return this.messageGetter.apply(this.getState());
	}

	@Override
	protected int getTextColor(boolean highlighted)
	{
		if(highlighted)
			return Lib.COLOUR_I_ImmersiveOrange;
		return 0x555555;
	}

	@Override
	public void onClick(double mouseX, double mouseY)
	{
		if(!opened)
		{
			this.opened = true;
			this.height = HEIGHT_BASE+openedHeight;
		}
		else
		{
			int sel = getHighlightedIndex(mouseX, mouseY);
			if(sel!=-1)
			{
				this.selectedState = sel;
				this.onPress.onPress(this);
			}
			this.opened = false;
			this.height = HEIGHT_BASE;
		}
	}

	private int getHighlightedIndex(double mouseX, double mouseY)
	{
		if(mouseX > getX()+(width-WIDTH_BUTTON))
			return -1;
		int calc = (int)((mouseY-getY()-8)/mc().font.lineHeight);
		return calc >= 0&&calc < states.length?calc: -1;
	}


	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(this.visible)
		{
			Font fontrenderer = mc.font;
			this.isHovered = mouseX >= this.getX()&&mouseY >= this.getY()&&mouseX < this.getX()+this.width&&mouseY < this.getY()+this.height;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);

			// basic field
			graphics.blit(texture, getX(), getY(), texU, texV, WIDTH_LEFT, HEIGHT_BASE);
			for(int i = 0; i < width-WIDTH_STATIC; i += WIDTH_MIDDLE)
				graphics.blit(texture, getX()+WIDTH_LEFT+i, getY(), texU+WIDTH_LEFT+1, texV, WIDTH_MIDDLE, HEIGHT_BASE);
			graphics.blit(texture, getX()+width-WIDTH_RIGHT, getY(), texU+WIDTH_LEFT+WIDTH_MIDDLE+2, texV, WIDTH_RIGHT, HEIGHT_BASE);

			if(!this.opened)
			{
				// text
				Component text = getMessage();
				int textX = getX()+TEXT_INDENT;
				int textY = getY()+HEIGHT_BASE/2-fontrenderer.lineHeight/2;
				graphics.drawString(fontrenderer, text, textX, textY, getTextColor(this.isHovered), false);
			}
			else
			{
				graphics.pose().pushPose();
				graphics.pose().translate(0, 0, 2);
				// background

				int openV = texV+17;
				int borderV = texV+20;
				for(int j = 0; j < openedHeight; j += 2)
				{
					graphics.blit(texture, getX(), getY()+OPEN_OFFSET+j, texU, openV, WIDTH_LEFT, 2);
					for(int i = 0; i < width-WIDTH_STATIC; i += WIDTH_MIDDLE)
						graphics.blit(texture, getX()+WIDTH_LEFT+i, getY()+OPEN_OFFSET+j, texU+WIDTH_LEFT+1, openV, WIDTH_MIDDLE, 2);
					graphics.blit(texture, getX()+width-WIDTH_RIGHT, getY()+OPEN_OFFSET+j, texU+WIDTH_LEFT+WIDTH_MIDDLE+2, openV, WIDTH_RIGHT, 2);
				}
				graphics.blit(texture, getX(), getY()+8+openedHeight, texU, borderV, WIDTH_LEFT, 2);
				for(int i = 0; i < width-WIDTH_STATIC; i += WIDTH_MIDDLE)
					graphics.blit(texture, getX()+WIDTH_LEFT+i, getY()+8+openedHeight, texU+WIDTH_LEFT+1, borderV, WIDTH_MIDDLE, 2);
				graphics.blit(texture, getX()+width-WIDTH_RIGHT, getY()+8+openedHeight, texU+WIDTH_LEFT+WIDTH_MIDDLE+2, borderV, WIDTH_RIGHT, 2);

				// text
				for(int j = 0; j < states.length; j++)
				{
					Component text = messageGetter.apply(states[j]);
					int textX = getX()+TEXT_INDENT;
					int textY = getY()+OPEN_OFFSET+j*fontrenderer.lineHeight;
					boolean highlighted = isHovered&&getHighlightedIndex(mouseX, mouseY)==j;
					graphics.drawString(fontrenderer, text, textX, textY, getTextColor(highlighted), false);
				}
				graphics.pose().popPose();
			}

		}
	}
}
