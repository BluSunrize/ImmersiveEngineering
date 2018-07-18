/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiManual;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ManualElementTable extends SpecialManualElements
{
	private String[][] table;
	private String[][] localizedTable;
	private int[] bars;
	//		int[] barsH;
	private boolean horizontalBars = false;
	private int tableLines;
	private int[] textOff;

	public ManualElementTable(ManualInstance manual, String[][] table, boolean horizontalBars)
	{
		super(manual);
		this.table = table;
		this.horizontalBars = horizontalBars;
	}

	@Override
	public void onOpened(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		super.onOpened(gui, x, y, pageButtons);
		manual.fontRenderer.setUnicodeFlag(true);
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
						int fl = manual.fontRenderer.getStringWidth(localizedTable[i][j]);
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
								int l = manual.fontRenderer.listFormattedStringToWidth(localizedTable[i][j], w).size();
								if(j!=0)
									yOff += l*(manual.fontRenderer.FONT_HEIGHT+1);
							}
				tableLines = MathHelper.ceil(yOff/(double)manual.fontRenderer.FONT_HEIGHT);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		manual.fontRenderer.setUnicodeFlag(false);
	}

	@Override
	public void render(GuiManual gui, int x, int y, int mx, int my)
	{
		if(localizedTable!=null)
		{
			int col = manual.getHighlightColour()|0xff000000;
			gui.drawGradientRect(x, y-2, x+120, y-1, col, col);

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
							ManualUtils.drawSplitString(manual.fontRenderer, line[j], xx, y+yOff, w, manual.getTextColour());
							//							manual.fontRenderer.drawSplitString(localizedTable[i][j], xx,y+textHeight+yOff, w, manual.getTextColour());
							int l = manual.fontRenderer.listFormattedStringToWidth(line[j], w).size();
							if(l > height)
								height = l;
						}

					if(horizontalBars)
					{
						float scale = .5f;
						GlStateManager.scale(1, scale, 1);
						int barHeight = (int)((y+yOff+height*manual.fontRenderer.FONT_HEIGHT)/scale);
						gui.drawGradientRect(x, barHeight, x+120, barHeight+1,
								manual.getTextColour()|0xff000000, manual.getTextColour()|0xff000000);
						GlStateManager.scale(1, 1/scale, 1);
					}

					yOff += height*(manual.fontRenderer.FONT_HEIGHT+1);
				}

			if(bars!=null)
				for(int i = 0; i < bars.length; i++)
					gui.drawGradientRect(textOff[i]-4, y-4, textOff[i]-3, y+yOff, col, col);
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
		return tableLines*manual.fontRenderer.FONT_HEIGHT;
	}
}
