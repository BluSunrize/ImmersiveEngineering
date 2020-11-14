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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Function;

public class GuiReactiveList extends Button
{
	private final Screen gui;
	private String[] entries;
	private int[] padding = {0, 0, 0, 0};
	private boolean needsSlider = false;
	private int perPage;
	private Function<String, String> translationFunction;
	private int scrollMode = 0;
	private float textScale = 1;
	
	private int textFGColor = 0xE0E0E0;
	private int textFGColorHover = Lib.COLOUR_I_ImmersiveOrange;

	private int offset;
	private int maxOffset;

	private long prevWheelNano = 0;
	private int targetEntry = -1;
	private int hoverTimer = 0;

	public GuiReactiveList(Screen gui, int x, int y, int w, int h, IPressable handler, String... entries)
	{
		super(x, y, w, h, StringTextComponent.EMPTY, handler);
		this.gui = gui;
		this.entries = entries;
		recalculateEntries();
	}

	private void recalculateEntries()
	{
		perPage = (int)((this.height-padding[0]-padding[1])/(ClientUtils.mc().fontRenderer.FONT_HEIGHT*textScale));
		if(perPage < entries.length)
		{
			needsSlider = true;
			maxOffset = entries.length-perPage;
		}
		else
			needsSlider = false;
	}

	public GuiReactiveList setPadding(int up, int down, int left, int right)
	{
		this.padding[0] = up;
		this.padding[1] = down;
		this.padding[2] = left;
		this.padding[3] = right;
		recalculateEntries();
		return this;
	}

	public GuiReactiveList setTranslationFunc(Function<String, String> func)
	{
		this.translationFunction = func;
		return this;
	}

	/**
	 * @param mode 0: No scrolling<br>1: Scroll when hovered<br>2: Scroll all
	 */
	public GuiReactiveList setScrollMode(int mode)
	{
		this.scrollMode = mode;
		return this;
	}

	public GuiReactiveList setFormatting(float textScale)
	{
		this.textScale = textScale;
		this.recalculateEntries();
		return this;
	}

	/**
	 * Sets the text color for the list entries
	 */
	public GuiReactiveList setTextColor(int textColor)
	{
		this.textFGColor = textColor;
		return this;
	}

	/**
	 * Sets the text color, when hovered over, for the list entries 
	 */
	public GuiReactiveList setTextHoverColor(int textHoverColor)
	{
		this.textFGColorHover = textHoverColor;
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

	public int getTextColor()
	{
		return this.textFGColor;
	}

	public int getTextHoverColor()
	{
		return this.textFGColorHover;
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partialTicks)
	{
		FontRenderer fr = ClientUtils.mc().fontRenderer;

		int mmY = my-this.y;
		int strWidth = width-padding[2]-padding[3]-(needsSlider?6: 0);
		if(needsSlider)
		{
			ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
			this.blit(transform, x+width-6, y, 16, 136, 6, 4);
			this.blit(transform, x+width-6, y+height-4, 16, 144, 6, 4);
			for(int i = 0; i < height-8; i += 2)
				this.blit(transform, x+width-6, y+4+i, 16, 141, 6, 2);

			int sliderSize = Math.max(6, height-maxOffset*fr.FONT_HEIGHT);
			float silderShift = (height-sliderSize)/(float)maxOffset*offset;

			this.blit(transform, x+width-5, (int)(y+silderShift+1), 20, 129, 4, 2);
			this.blit(transform, x+width-5, (int)(y+silderShift+sliderSize-4), 20, 132, 4, 3);
			for(int i = 0; i < sliderSize-7; i++)
				this.blit(transform, x+width-5, (int)(y+silderShift+3+i), 20, 131, 4, 1);
		}

		transform.scale(textScale, textScale, 1);
		this.isHovered = mx >= x&&mx < x+width&&my >= y&&my < y+height;
		boolean hasTarget = false;
		for(int i = 0; i < Math.min(perPage, entries.length); i++)
		{
			int j = offset+i;
			int col = this.getTextColor();
			boolean selectionHover = isHovered&&mmY >= i*fr.FONT_HEIGHT&&mmY < (i+1)*fr.FONT_HEIGHT;
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
				col = this.getTextHoverColor();
			}
			if(j > entries.length-1)
				j = entries.length-1;
			String s = translationFunction!=null?translationFunction.apply(entries[j]): entries[j];
			int overLength = s.length()-fr.func_238412_a_(s, strWidth).length();
			if(overLength > 0)//String is too long
			{
				if(selectionHover&&hoverTimer > 20)
				{
					int textOffset = (hoverTimer/10)%(s.length());
					s = s.substring(textOffset)+" "+s.substring(0, textOffset);
				}
				s = fr.func_238412_a_(s, strWidth);
			}
			float tx = ((x+padding[2])/textScale);
			float ty = ((y+padding[0]+(fr.FONT_HEIGHT*i))/textScale);
			transform.translate(tx, ty, 0);
			fr.drawString(transform, s, 0, 0, col);
			transform.translate(-tx, -ty, 0);
		}
		transform.scale(1/textScale, 1/textScale, 1);
		if(!hasTarget)
		{
			targetEntry = -1;
			hoverTimer = 0;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseWheel, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
	{
		if(mouseWheel!=0&&maxOffset > 0)
		{
			if(mouseWheel < 0&&offset < maxOffset)
				offset++;
			if(mouseWheel > 0&&offset > 0)
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
		if (this.active && this.visible)
			if (this.isValidClickButton(key) && this.clicked(mx,my))
			{

				FontRenderer fr = ClientUtils.mc().fontRenderer;
				double mmY = my-this.y;
				for(int i = 0; i < Math.min(perPage, entries.length); i++)
					if(mmY >= i*fr.FONT_HEIGHT&&mmY < (i+1)*fr.FONT_HEIGHT)
						selectedOption = offset+i;
			}
		super.mouseClicked(mx, my, key);
		return selectedOption!=-1;
	}
}
