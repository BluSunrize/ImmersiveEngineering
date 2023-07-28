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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

public class ManualElementTable extends SpecialManualElements
{
	private final Component[][] table;
	private int[] bars;
	private final boolean horizontalBars;
	private OptionalInt height = OptionalInt.empty();
	private int[] textOff;

	@Deprecated
	public ManualElementTable(ManualInstance manual, String[][] table, boolean horizontalBars)
	{
		this(manual, Arrays.stream(table)
						.map(a -> Arrays.stream(a)
								.map(Component::literal)
								.toArray(Component[]::new)
						)
						.toArray(Component[][]::new),
				horizontalBars
		);
	}

	public ManualElementTable(ManualInstance manual, Component[][] table, boolean horizontalBars)
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
	public void render(GuiGraphics graphics, ManualScreen gui, int x, int y, int mx, int my)
	{
		if(table!=null)
		{
			int col = manual.getHighlightColour()|0xff000000;
			graphics.fill(x, y-2, x+120, y-1, col);

			final int lineHeight = manual.fontRenderer().lineHeight;
			int yOff = 0;
			for(Component[] line : table)
				if(line!=null)
				{
					int height = 0;
					for(int j = 0; j < line.length; j++)
						if(line[j]!=null)
						{
							int xx = textOff.length > 0&&j > 0?textOff[j-1]: x;
							int w = Math.max(10, 120-(j > 0?textOff[j-1]-x: 0));
							Component lineText = line[j];
							List<FormattedCharSequence> lines = manual.fontRenderer().split(lineText, w);
							for(int i = 0; i < lines.size(); i++)
								graphics.drawString(
										manual.fontRenderer(), lines.get(i), xx, y+yOff+i*lineHeight, manual.getTextColour(), false
								);
							if(lines.size() > height)
								height = lines.size();
						}

					if(horizontalBars)
					{
						float scale = .5f;
						graphics.pose().scale(1, scale, 1);
						int barHeight = (int)((y+yOff+height*lineHeight)/scale);
						graphics.fill(x, barHeight, x+120, barHeight+1, manual.getTextColour()|0xff000000);
						graphics.pose().scale(1, 1/scale, 1);
					}

					yOff += height*(lineHeight+1);
				}

			if(bars!=null)
				for(int i = 0; i < bars.length; i++)
					graphics.fill(textOff[i]-4, y-4, textOff[i]-3, y+yOff, col);
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
		for(Component[] tableLine : table)
		{
			if(tableLine.length-1 > bars.length)
				bars = Arrays.copyOf(bars, tableLine.length-1);
			for(int j = 0; j < tableLine.length-1; j++)
			{
				int fl = manual.fontRenderer().width(tableLine[j]);
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
		for(Component[] tableLine : table)
			if(tableLine!=null)
				for(int j = 0; j < tableLine.length; j++)
					if(tableLine[j]!=null)
					{
						int w = Math.max(10, 120-(j > 0?textOff[j-1]: 0));
						int l = manual.fontRenderer().split(tableLine[j], w).size();
						if(j!=0)
							yOff += l*(manual.fontRenderer().lineHeight+1);
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
