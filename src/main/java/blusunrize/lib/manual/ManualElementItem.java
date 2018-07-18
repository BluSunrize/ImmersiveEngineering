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
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Locale;

public class ManualElementItem extends SpecialManualElements
{
	NonNullList<ItemStack> stacks;

	static NonNullList<ItemStack> parseArray(ItemStack... stacks)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(stacks.length, ItemStack.EMPTY);
		for(int i = 0; i < stacks.length; i++)
			list.set(i, stacks[i]);
		return list;
	}

	public ManualElementItem(ManualInstance manual, ItemStack... stacks)
	{
		this(manual, parseArray(stacks));
	}

	public ManualElementItem(ManualInstance manual, NonNullList<ItemStack> stacks)
	{
		super(manual);
		this.stacks = stacks;
	}

	@Override
	public void onOpened(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		int length = stacks.size();
		int yOffset = 0;
		if(length > 0)
		{
			float scale = length > 7?1f: length > 4?1.5f: 2f;
			int line0 = (int)(8/scale);
			int line1 = line0-1;
			int lineSum = line0+line1;
			int lines = (length/lineSum*2)+(length%lineSum/line0)+(length%lineSum%line0 > 0?1: 0);
			float equalPerLine = length/(float)lines;
			line1 = (int)Math.floor(equalPerLine);
			line0 = MathHelper.ceil(equalPerLine);
			lineSum = line0+line1;
			yOffset = lines*(int)(18*scale);
		}
		super.onOpened(gui, x, y+yOffset, pageButtons);
	}

	@Override
	public void render(GuiManual gui, int x, int y, int mx, int my)
	{
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		highlighted = ItemStack.EMPTY;
		int yOffset = 0;
		int length = stacks.size();
		if(length > 0)
		{
			float scale = length > 8?1f: length > 3?1.5f: 2f;
			int line0 = (int)(7.5/scale);
			int line1 = line0-1;
			int lineSum = line0+line1;
			int lines = (length/lineSum*2)+(length%lineSum/line0)+(length%lineSum%line0 > 0?1: 0);
			float equalPerLine = length/(float)lines;
			line1 = (int)Math.floor(equalPerLine);
			line0 = MathHelper.ceil(equalPerLine);
			lineSum = line0+line1;
			int lastLines = length%lineSum;
			int lastLine = lastLines==line0?line0: lastLines==0?line1: lastLines%line0;
			GlStateManager.scale(scale, scale, scale);
			/*
			 RenderItem.getInstance().renderWithColor=true;
			 */
			yOffset = lines*(int)(18*scale);
			for(int line = 0; line < lines; line++)
			{
				int perLine = line==lines-1?lastLine: line%2==0?line0: line1;
				if(line==0&&perLine > length)
					perLine = length;
				int w2 = perLine*(int)(18*scale)/2;
				for(int i = 0; i < perLine; i++)
				{
					int item = line/2*lineSum+line%2*line0+i;
					if(item >= length)
						break;
					int xx = x+60-w2+(int)(i*18*scale);
					int yy = y+(lines < 2?4: 0)+line*(int)(18*scale);
					ManualUtils.renderItem().renderItemAndEffectIntoGUI(stacks.get(item), (int)(xx/scale), (int)(yy/scale));
					if(mx >= xx&&mx < xx+(16*scale)&&my >= yy&&my < yy+(16*scale))
						highlighted = stacks.get(item);
				}
			}
			GlStateManager.scale(1/scale, 1/scale, 1/scale);
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableBlend();

		manual.fontRenderer.setUnicodeFlag(false);
		if(!highlighted.isEmpty())
			gui.renderToolTip(highlighted, mx, my);
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		for(ItemStack stack : stacks)
			if(stack.getDisplayName().toLowerCase(Locale.ENGLISH).contains(searchTag))
				return true;
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		return 0;//TODO
	}
}
