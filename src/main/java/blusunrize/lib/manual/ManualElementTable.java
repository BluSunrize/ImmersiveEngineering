/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;

import java.util.List;

public class ManualElementTable extends SpecialManualElements
{
	private ITextComponent[][] table;
	private int[] bars;
	private boolean horizontalBars = false;
	private int height;
	private int[] textOff;

	public ManualElementTable(ManualInstance manual, ITextComponent[][] table, boolean horizontalBars)
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
				bars = new int[1];
				for(ITextComponent[] line : table)
				{
					if(line.length-1 > bars.length)
					{
						int[] newBars = new int[line.length-1];
						System.arraycopy(bars, 0, newBars, 0, bars.length);
						bars = newBars;
					}
					for(int j = 0; j < line.length-1; j++)
					{
						int fl = manual.fontRenderer().func_238414_a_(line[j]);
						if(fl > bars[j])
							bars[j] = fl;
					}
				}
				textOff = new int[bars.length];
				int xx = x;
				for(int i = 0; i < bars.length; i++)
				{
					xx += bars[i]+8;
					textOff[i] = xx;
				}

				int yOff = 0;
				for(ITextComponent[] line : table)
					if(line!=null)
						for(int j = 0; j < line.length; j++)
							if(line[j]!=null)
							{
								int w = Math.max(10, 120-(j > 0?textOff[j-1]-x: 0));
								int l = manual.fontRenderer().func_238425_b_(line[j], w).size();
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
							List<ITextProperties> lines = manual.fontRenderer().func_238425_b_(line[j], w);
							for(int i = 0; i < lines.size(); i++)
							{
								ITextProperties l = lines.get(i);
								float yForLine = y+yOff+i*manual.fontRenderer().FONT_HEIGHT;
								manual.fontRenderer().func_238422_b_(transform, l, xx, yForLine, manual.getTextColour());
							}
							if(lines.size() > height)
								height = lines.size();
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
