/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

public class ManualElementTable extends SpecialManualElements
{
	private ITextComponent[][] table;
	private int[] bars;
	private boolean horizontalBars;
	private OptionalInt height = OptionalInt.empty();
	private int[] textOff;

	@Deprecated
	public ManualElementTable(ManualInstance manual, ITextComponent[][] table, boolean horizontalBars)
	{
		this(manual, Arrays.stream(table)
						.map(a -> Arrays.stream(a)
								.map(StringTextComponent::new)
								.toArray(ITextComponent[]::new)
						)
						.toArray(ITextComponent[][]::new),
				horizontalBars
		);
	}

	public ManualElementTable(ManualInstance manual, ITextComponent[][] table, boolean horizontalBars)
	{
		super(manual);
		Preconditions.checkNotNull(table);
		this.table = table;
		this.horizontalBars = horizontalBars;
	}

	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons)
	{
		super.onOpened(gui, x, y, pageButtons);
		try
		{
			recalculateLayout();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void render(MatrixStack transform, ManualScreen gui, int x, int y, int mx, int my)
	{
		if(table!=null)
		{
			int col = manual.getHighlightColour()|0xff000000;
			AbstractGui.fill(transform, x, y-2, x+120, y-1, col);

			int yOff = 0;
			for(ITextComponent[] line : table)
				if(line!=null)
				{
					int height = 0;
					for(int j = 0; j < line.length; j++)
						if(line[j]!=null)
						{
							int xx = textOff.length > 0&&j > 0?textOff[j-1]: x;
							int w = Math.max(10, 120-(j > 0?textOff[j-1]-x: 0));
							String lineText = line[j].getFormattedText();
							manual.fontRenderer().drawSplitString(lineText, xx, y+yOff, w, manual.getTextColour());
							int lines = manual.fontRenderer().listFormattedStringToWidth(lineText, w).size();
							if(lines > height)
								height = lines;
						}

					if(horizontalBars)
					{
						float scale = .5f;
						transform.scale(1, scale, 1);
						int barHeight = (int)((y+yOff+height*manual.fontRenderer().FONT_HEIGHT)/scale);
						AbstractGui.fill(transform, x, barHeight, x+120, barHeight+1,
								manual.getTextColour()|0xff000000);
						transform.scale(1, 1/scale, 1);
					}

					yOff += height*(manual.fontRenderer().FONT_HEIGHT+1);
				}

			if(bars!=null)
				for(int i = 0; i < bars.length; i++)
					AbstractGui.fill(transform, textOff[i]-4, y-4, textOff[i]-3, y+yOff, col);
		}
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	private void recalculateLayout()
	{
		bars = new int[1];
		for(ITextComponent[] tableLine : table)
		{
			if(tableLine.length-1 > bars.length)
				bars = Arrays.copyOf(bars, tableLine.length-1);
			for(int j = 0; j < tableLine.length-1; j++)
			{
				int fl = manual.fontRenderer().getStringWidth(tableLine[j].getFormattedText());
				if(fl > bars[j])
					bars[j] = fl;
			}
		}
		textOff = new int[bars.length];
		int xx = 0;
		for(int i = 0; i < bars.length; i++)
		{
			xx += bars[i]+8;
			textOff[i] = xx;
		}
		int yOff = 0;
		for(ITextComponent[] tableLine : table)
			if(tableLine!=null)
				for(int j = 0; j < tableLine.length; j++)
					if(tableLine[j]!=null)
					{
						int w = Math.max(10, 120-(j > 0?textOff[j-1]: 0));
						int l = manual.fontRenderer().listFormattedStringToWidth(tableLine[j].getFormattedText(), w).size();
						if(j!=0)
							yOff += l*(manual.fontRenderer().FONT_HEIGHT+1);
					}
		height = OptionalInt.of(yOff);
	}

	@Override
	public int getPixelsTaken()
	{
		if(!height.isPresent())
			recalculateLayout();
		return height.getAsInt();
	}

	@Override
	public boolean isAbove()
	{
		return false;
	}
}
