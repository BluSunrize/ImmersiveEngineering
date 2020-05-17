/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class ManualElementTable extends SpecialManualElements
{
	private String[][] table;
	private String[][] localizedTable;
	private int[] bars;
	private boolean horizontalBars = false;
	private int height;
	private int[] textOff;

	public ManualElementTable(ManualInstance manual, String[][] table, boolean horizontalBars)
	{
		super(manual);
		this.table = table;
		this.horizontalBars = horizontalBars;
	}

	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons)
	{
		super.onOpened(gui, x, y, pageButtons);
		try
		{
			if(table!=null)
			{
				localizedTable = new String[table.length][];

				bars = new int[1];
				for(int i = 0; i < table.length; i++)
				{
					localizedTable[i] = new String[table[i].length];
					for(int j = 0; j < table[i].length; j++)
						if(table[i][j]!=null)
							localizedTable[i][j] = I18n.format(table[i][j]);

					if(table[i].length-1 > bars.length)
					{
						int[] newBars = new int[table[i].length-1];
						System.arraycopy(bars, 0, newBars, 0, bars.length);
						bars = newBars;
					}
					for(int j = 0; j < table[i].length-1; j++)
					{
						int fl = manual.fontRenderer().getStringWidth(localizedTable[i][j]);
						if(fl > bars[j])
							bars[j] = fl;
					}
				}
				textOff = new int[bars!=null?bars.length: 0];
				if(bars!=null)
				{
					int xx = x;
					for(int i = 0; i < bars.length; i++)
					{
						xx += bars[i]+8;
						textOff[i] = xx;
					}
				}

				int yOff = 0;
				for(int i = 0; i < localizedTable.length; i++)
					if(localizedTable[i]!=null)
						for(int j = 0; j < localizedTable[i].length; j++)
							if(localizedTable[i][j]!=null)
							{
								int w = Math.max(10, 120-(j > 0?textOff[j-1]-x: 0));
								int l = manual.fontRenderer().listFormattedStringToWidth(localizedTable[i][j], w).size();
								if(j!=0)
									yOff += l*(manual.fontRenderer().FONT_HEIGHT+1);
							}
				height = yOff;
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void render(ManualScreen gui, int x, int y, int mx, int my)
	{
		if(localizedTable!=null)
		{
			int col = manual.getHighlightColour()|0xff000000;
			AbstractGui.fill(x, y-2, x+120, y-1, col);

			int yOff = 0;
			for(String[] line : localizedTable)
				if(line!=null)
				{
					int height = 0;
					for(int j = 0; j < line.length; j++)
						if(line[j]!=null)
						{
							int xx = textOff.length > 0&&j > 0?textOff[j-1]: x;
							int w = Math.max(10, 120-(j > 0?textOff[j-1]-x: 0));
							manual.fontRenderer().drawSplitString(line[j], xx, y+yOff, w, manual.getTextColour());
							int lines = manual.fontRenderer().listFormattedStringToWidth(line[j], w).size();
							if(lines > height)
								height = lines;
						}

					if(horizontalBars)
					{
						float scale = .5f;
						RenderSystem.scalef(1, scale, 1);
						int barHeight = (int)((y+yOff+height*manual.fontRenderer().FONT_HEIGHT)/scale);
						AbstractGui.fill(x, barHeight, x+120, barHeight+1,
								manual.getTextColour()|0xff000000);
						RenderSystem.scalef(1, 1/scale, 1);
					}

					yOff += height*(manual.fontRenderer().FONT_HEIGHT+1);
				}

			if(bars!=null)
				for(int i = 0; i < bars.length; i++)
					AbstractGui.fill(textOff[i]-4, y-4, textOff[i]-3, y+yOff, col);
		}
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		return height;
	}

	@Override
	public boolean isAbove()
	{
		return false;
	}
}
