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
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class GuiReactiveList extends Button
{
	static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("hud_elements");

	protected Supplier<List<String>> entries;
	private final int[] padding = {0, 0, 0, 0};
	private boolean needsSlider = false;
	protected int perPage;
	private final float textScale = 1;

	protected int offset;
	private int maxOffset;

	private int targetEntry = -1;
	private int hoverTimer = 0;

	public GuiReactiveList(int x, int y, int w, int h, IIEPressable<? extends GuiReactiveList> handler, Supplier<List<String>> entries)
	{
		super(x, y, w, h, Component.empty(), handler, DEFAULT_NARRATION);
		this.entries = entries;
	}

	private void recalculateEntries()
	{
		final int length = this.entries.get().size();
		perPage = (int)((this.height-padding[0]-padding[1])/(ClientUtils.mc().font.lineHeight*textScale));
		if(perPage < length)
		{
			needsSlider = true;
			maxOffset = length-perPage;
			this.offset = Math.min(this.offset, maxOffset);
		}
		else
		{
			needsSlider = false;
			this.maxOffset = this.offset = 0;
		}
	}

	public GuiReactiveList setPadding(int up, int down, int left, int right)
	{
		this.padding[0] = up;
		this.padding[1] = down;
		this.padding[2] = left;
		this.padding[3] = right;
		return this;
	}

	public int getOffset()
	{
		return this.offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public int getMaxOffset()
	{
		return this.maxOffset;
	}

	@Override
	public void render(GuiGraphics graphics, int mx, int my, float partialTicks)
	{
		recalculateEntries();
		final List<String> entries = this.entries.get();
		Font fr = ClientUtils.mc().font;

		int mmY = my-this.getY();
		int strWidth = width-padding[2]-padding[3]-(needsSlider?6: 0);
		if(needsSlider)
		{
			graphics.blit(TEXTURE, getX()+width-6, getY(), 16, 136, 6, 4);
			graphics.blit(TEXTURE, getX()+width-6, getY()+height-4, 16, 144, 6, 4);
			for(int i = 0; i < height-8; i += 2)
				graphics.blit(TEXTURE, getX()+width-6, getY()+4+i, 16, 141, 6, 2);

			int sliderSize = Math.max(6, height-maxOffset*fr.lineHeight);
			float silderShift = (height-sliderSize)/(float)maxOffset*offset;

			graphics.blit(TEXTURE, getX()+width-5, (int)(getY()+silderShift+1), 20, 129, 4, 2);
			graphics.blit(TEXTURE, getX()+width-5, (int)(getY()+silderShift+sliderSize-4), 20, 132, 4, 3);
			for(int i = 0; i < sliderSize-7; i++)
				graphics.blit(TEXTURE, getX()+width-5, (int)(getY()+silderShift+3+i), 20, 131, 4, 1);
		}

		graphics.pose().scale(textScale, textScale, 1);
		this.isHovered = active&&mx >= getX()&&mx < getX()+width&&my >= getY()&&my < getY()+height;
		boolean hasTarget = false;
		for(int i = 0; i < Math.min(perPage, entries.size()); i++)
		{
			int j = offset+i;
			int col = 0xE0E0E0;
			boolean selectionHover = isHovered&&mmY >= i*fr.lineHeight&&mmY < (i+1)*fr.lineHeight;
			if(selectionHover)
			{
				hasTarget = true;
				if(targetEntry!=j)
				{
					targetEntry = j;
					hoverTimer = 0;
				}
				else
					hoverTimer++;
				col = Lib.COLOUR_I_ImmersiveOrange;
			}
			if(j > entries.size()-1)
				j = entries.size()-1;
			String s = entries.get(j);
			int overLength = s.length()-fr.plainSubstrByWidth(s, strWidth).length();
			if(overLength > 0)//String is too long
			{
				if(selectionHover&&hoverTimer > 20)
				{
					int textOffset = (hoverTimer/10)%(s.length());
					s = s.substring(textOffset)+" "+s.substring(0, textOffset);
				}
				s = fr.plainSubstrByWidth(s, strWidth);
			}
			float tx = ((getX()+padding[2])/textScale);
			float ty = ((getY()+padding[0]+(fr.lineHeight*i))/textScale);
			graphics.pose().translate(tx, ty, 0);
			graphics.drawString(fr, s, 0, 0, col);
			graphics.pose().translate(-tx, -ty, 0);
		}
		graphics.pose().scale(1/textScale, 1/textScale, 1);
		if(!hasTarget)
		{
			targetEntry = -1;
			hoverTimer = 0;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if(delta!=0&&maxOffset > 0)
		{
			if(delta < 0&&offset < maxOffset)
				offset++;
			if(delta > 0&&offset > 0)
				offset--;
			return true;
		}
		else
			return false;
	}

	public int selectedOption = -1;

	@Override
	public boolean mouseClicked(double mx, double my, int key)
	{
		selectedOption = -1;
		if(this.active&&this.visible)
			if(this.isValidClickButton(key)&&this.clicked(mx, my))
			{
				Font fr = ClientUtils.mc().font;
				double mmY = my-this.getY();
				for(int i = 0; i < Math.min(perPage, entries.get().size()); i++)
					if(mmY >= i*fr.lineHeight&&mmY < (i+1)*fr.lineHeight)
						selectedOption = offset+i;
			}
		super.mouseClicked(mx, my, key);
		return selectedOption!=-1;
	}
}